package org.syncloud.android.ui.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class WifiDialog extends DialogFragment {

    public static int WIFI_SETTINGS = 3;

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
                .setPositiveButton("Wi-Fi Settings", (dialog, id) -> openWiFiSettings())
                .setNegativeButton("Cancel", (dialog, id) -> context.finish());

        return builder.create();
    }

    public void openWiFiSettings() {
        final Activity context = getActivity();
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        context.startActivityForResult(intent, WIFI_SETTINGS);
    }

}
