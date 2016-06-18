package com.m303.mofatunerpro;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLoggerFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Logger extends Fragment {

    private OnLoggerFragmentInteractionListener mListener;

    private BluetoothLogger btLogger;

    private TextView status;

    private Button connect;
    private Button logButton;
    private Button calibrate;

    private Spinner spinner;

    public Logger() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btLogger = BluetoothLogger.instance();
        btLogger.setLogger(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logger, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        status = (TextView) view.findViewById(R.id.statusTextView);


        connect = (Button) view.findViewById(R.id.connectButton);

        connect.setText(btLogger.connected() ? "Disconnect" : "Connect");
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean connected = btLogger.connected();
                if (connected) {
                    btLogger.disconnect();
                } else {
                    btLogger.connect();
                }
            }
        });

        calibrate = (Button) view.findViewById(R.id.calibrateButton);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btLogger.calibrate(true);
                CalibrateDialog cd = new CalibrateDialog(btLogger);
                cd.show(getActivity().getSupportFragmentManager(), "calibrate");
            }
        });


        calibrate.setEnabled(btLogger.connected());
        calibrate.setClickable(btLogger.connected());

        logButton = (Button) view.findViewById(R.id.startLogButton);
        logButton.setText(btLogger.isLogging() ? "Stop Log" : "Start Log");
        logButton.setEnabled(btLogger.connected());
        logButton.setClickable(btLogger.connected());

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btLogger.isLogging()) {
                    String logEntryName = btLogger.stop();
                    if (logEntryName != null) {
                        mListener.onLoggerFragmentInteraction(logEntryName);
                    }
                } else {
                    btLogger.startLogging(spinner.getSelectedItem().toString());

                }
            }
        });

        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.cylinders, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoggerFragmentInteractionListener) {
            mListener = (OnLoggerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        btLogger.stop();
        mListener = null;
    }


    public void updateState(final BluetoothLogger.BtState state, final String info) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(info);
                switch (state) {

                    case Disconnected:
                        connect.setEnabled(true);
                        connect.setClickable(true);
                        connect.setText("Connect");

                        logButton.setEnabled(false);
                        logButton.setClickable(false);

                        calibrate.setEnabled(false);
                        calibrate.setClickable(false);

                        break;
                    case Connecting:
                        connect.setEnabled(false);
                        connect.setClickable(false);

                        logButton.setEnabled(false);
                        logButton.setClickable(false);
                        break;
                    case Connected:
                        btLogger.readData();
                        connect.setEnabled(true);
                        connect.setClickable(true);

                        calibrate.setEnabled(true);
                        calibrate.setClickable(true);

                        logButton.setEnabled(true);
                        logButton.setClickable(true);

                        connect.setText("Disconnect");

                        break;
                    case Logging:
                        logButton.setText("Stop log");

                        calibrate.setEnabled(false);
                        calibrate.setClickable(false);

                        break;
                    case StoppedLogging:
                        logButton.setText("Start log");

                        calibrate.setEnabled(true);
                        calibrate.setClickable(true);

                        break;
                    case Calibrating:
                        break;
                }
            }
        });

    }

    final NumberFormat nf = new DecimalFormat("0.000");

    public void updateValues(final LogValue value) {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (value != null) {

                        if(getView() == null) {
                            return;
                        }

                        ProgressBar throttleBar = (ProgressBar) getView().findViewById(R.id.throttleBar);
                        ProgressBar lambdaBar = (ProgressBar) getView().findViewById(R.id.lambdaBar);
                        ProgressBar rpmBar = (ProgressBar) getView().findViewById(R.id.rpmBar);

                        TextView throttleValue = (TextView) getView().findViewById(R.id.throttleValue);
                        TextView lambdaValue = (TextView) getView().findViewById(R.id.lambdaValue);
                        TextView rpmValue = (TextView) getView().findViewById(R.id.rpmValue);

                        throttleValue.setText("TPS " + nf.format(value.throttle));
                        lambdaValue.setText("λ " + nf.format(value.lambda));
                        rpmValue.setText("RPM " + value.rpm);

                        throttleBar.setProgress(value.getThrottlePercent());
                        lambdaBar.setProgress(value.getLambdaPercent());
                        rpmBar.setProgress(value.getRpmPercent());
                    }
                }
            });
        }
    }

    public interface OnLoggerFragmentInteractionListener {
        void onLoggerFragmentInteraction(String newLogEntry);
    }
}
