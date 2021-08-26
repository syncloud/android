package org.syncloud.android.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment

const val WIFI_SETTINGS = 3

class WifiDialog(val message: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Activity? = activity
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Wi-Fi Connection")
        builder.setMessage("You are not connected to Wi-Fi network. $message")
            .setCancelable(false)
            .setPositiveButton("Wi-Fi Settings") { _: DialogInterface?, _: Int -> openWiFiSettings() }
            .setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> context!!.finish() }
        return builder.create()
    }

    private fun openWiFiSettings() {
        val context: Activity? = activity
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context!!.startActivityForResult(intent, WIFI_SETTINGS)
    }
}