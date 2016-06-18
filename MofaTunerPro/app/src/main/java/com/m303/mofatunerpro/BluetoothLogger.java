package com.m303.mofatunerpro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by mley on 15.06.16.
 */
public class BluetoothLogger {


    private static BluetoothLogger instance;

    public enum BtState {
        Disconnected,
        Connecting,
        Connected,
        Logging,
        StoppedLogging,
        Calibrating;
    }

    private static final String T = "BTLogger";

    private BluetoothAdapter btAdapter;

    private SharedPreferences prefs;

    private volatile boolean doLogging = false;
    private volatile boolean connected = false;
    private volatile boolean calibrating = false;
    private BluetoothSocket socket;
    private Thread loggerThread;
    private Logger logger;

    private Storage storage;

    private List<LogValue> log;
    private Date logStartDate;
    private String logConfig;
    private long logStart;

    private int minThrottle = 0;
    private int maxThrottle = 1024;

    public static BluetoothLogger instance() {
        return instance;
    }

    public static void createInstance(Context context) {
        instance = new BluetoothLogger(context);
    }

    private BluetoothLogger(Context context) {
        prefs = context.getSharedPreferences("mofatunerpro", Context.MODE_PRIVATE);
        this.storage = Storage.instance();
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        minThrottle = prefs.getInt("minThrottle", 0);
        maxThrottle = prefs.getInt("maxThrottle", 1024);

    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public boolean connected() {
        return socket != null;
    }

    public boolean isReadingData() {
        return loggerThread != null && loggerThread.isAlive();
    }


    public void calibrate(boolean b) {
        calibrating = b;
        if (calibrating) {
            minThrottle = 1024;
            maxThrottle = 0;
        } else {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putInt("minThrottle", minThrottle);
            edit.putInt("maxThrottle", maxThrottle);
            edit.commit();
        }
    }

    public String getThrottleValues() {
        return "min: " + minThrottle + " max: " + maxThrottle;
    }


    public void connect() {
        new Thread() {
            public void run() {
                _connect();
            }

        }.start();
    }


    private void _connect() {
        Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
        for (int i = 0; i < 3; i++) {
            updateState(BtState.Connecting, "Connecting " + (i + 1) + "/3");
            for (BluetoothDevice device : bondedDevices) {

                if ("HC-05".equals(device.getName())) { // find HC-05 BT adapter
                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        socket.connect();


                        connected = true;
                        updateState(BtState.Connected, "Connected.");
                        return;
                    } catch (IOException e) {
                        Log.e(T, "error connecting", e);
                    }
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
        updateState(BtState.Disconnected, "Connection failed.");
    }

    private void updateState(BtState state, String status) {
        if (logger != null) {
            logger.updateState(state, status);
        }
    }

    public void reconnect() {
        updateState(BtState.Disconnected, "Connection lost");
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(T, "error closing socket while reconnecting", e);
        }

        _connect();
    }

    public void disconnect() {
        connected = false;
        stop();
        if (loggerThread != null) {
            try {
                loggerThread.join();
            } catch (InterruptedException e) {
                Log.e(T, "interrupted while waiting for logger thread to terminate", e);
            }
            loggerThread = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(T, "error disconnecting", e);
            }
            socket = null;
        }

        updateState(BtState.Disconnected, "Connection closed.");
    }

    public boolean isLogging() {
        return doLogging;
    }

    public void readData() {
        if (loggerThread != null) {
            return;
        }

        loggerThread = new Thread("BTLoggerThread") {
            public void run() {


                BufferedReader br = null;
                try {
                    InputStream is = socket.getInputStream();
                    int a;
                    while ((a = is.available()) > 0) {
                        is.skip(a);
                    }
                    br = new BufferedReader(new InputStreamReader(is));

                } catch (IOException e) {
                    Log.e(T, "error getting input stream from bt socket", e);
                    reconnect();
                }
                while (connected) {
                    try {
                        String line = br.readLine();
                        if (!doLogging && calibrating) {
                            calibrateThrottle(line);
                        }
                        try {
                            LogValue value = new LogValue((int) (System.currentTimeMillis() - logStart), line, minThrottle, maxThrottle);

                            Log.i(T, "BT:" + line);
                            if (doLogging) {
                                log.add(value);
                            }

                            if(logger != null) {
                                logger.updateValues(value);
                            }
                        } catch (NumberFormatException nfe) {
                            Log.w(T, "nfe: " + line, nfe);
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            Log.w(T, "aioobe: " + line, aioobe);
                        }


                    } catch (IOException e) {
                        Log.e(T, "error reading data from bt socket", e);
                        reconnect();
                    }
                }


            }
        };
        loggerThread.start();
    }

    private void calibrateThrottle(String line) {
        try {
            int t = Integer.parseInt(line.split(",")[0]);
            minThrottle = Math.min(minThrottle, t);
            maxThrottle = Math.max(maxThrottle, t);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            Log.w(T, "error calibrating " + line, e);
        }
    }

    public void startLogging(String config) {
        if (doLogging) {
            return;
        }
        logStart = System.currentTimeMillis();
        log = new ArrayList<>();
        logStartDate = new Date();
        logConfig = config;
        doLogging = true;
        updateState(BtState.Logging, "Logging data.");
    }

    public String stop() {
        if (!doLogging) {
            return null;
        }
        doLogging = false;
        String logEntryName = Storage.DF.format(logStartDate) + "_" + logConfig;
        storage.saveDataSet(logEntryName, log);
        updateState(BtState.StoppedLogging, "Logging stopped.");
        return logEntryName;
    }

}
