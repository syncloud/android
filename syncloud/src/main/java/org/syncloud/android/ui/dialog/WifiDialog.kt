package org.syncloud.android.ui.dialog

import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import org.syncloud.android.ui.dialog.WifiDialog

class WifiDialog : DialogFragment() {
    private var message = ""
    fun setMessage(message: String) {
        this.message = message
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Activity? = activity
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Wi-Fi Connection")
        builder.setMessage("You are not connected to Wi-Fi network. $message")
            .setCancelable(false)
            .setPositiveButton("Wi-Fi Settings") { dialog: DialogInterface?, id: Int -> openWiFiSettings() }
            .setNegativeButton("Cancel") { dialog: DialogInterface?, id: Int -> context!!.finish() }
        return builder.create()
    }

    fun openWiFiSettings() {
        val context: Activity? = activity
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        context!!.startActivityForResult(intent, WIFI_SETTINGS)
    }

    companion object {
        @JvmField
        var WIFI_SETTINGS = 3
    }
}