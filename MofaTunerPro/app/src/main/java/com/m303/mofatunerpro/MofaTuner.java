package com.m303.mofatunerpro;

import android.app.Application;

/**
 * Created by mley on 17.06.16.
 */
public class MofaTuner extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothLogger.createInstance(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        BluetoothLogger.instance().disconnect();
    }
}
