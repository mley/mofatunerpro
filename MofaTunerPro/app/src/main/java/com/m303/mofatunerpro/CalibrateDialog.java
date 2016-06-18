package com.m303.mofatunerpro;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by mley on 17.06.16.
 */
public class CalibrateDialog extends DialogFragment {

    private BluetoothLogger btLogger;

    public CalibrateDialog(BluetoothLogger btLogger) {
        this.btLogger = btLogger;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Slowly open and close throttle completly several times.").setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                btLogger.calibrate(false);

                Toast.makeText(getContext(), "calibrated: "+btLogger.getThrottleValues(), Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();

    }
}
