package org.syncloud.android.ui.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class WifiDialog extends DialogFragment {

    private String message = "";

    public void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Activity context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Wi-Fi Connection");
        builder.setMessage("You are not connected to Wi-Fi network. " + message)
                .setCancelable(false)
                .setPositiveButton("Wi-Fi Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        openWiFiSettings();
                        context.finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        context.finish();
                    }
                });

        return builder.create();
    }

    public void openWiFiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, 0);
    }

}
