package org.syncloud.android.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment

class WifiDialog(val message: String) : DialogFragment() {
    private lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Activity? = activity
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            context?.finish()
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Wi-Fi Connection")
        builder.setMessage("You are not connected to Wi-Fi network. $message")
                .setCancelable(false)
                .setPositiveButton("Wi-Fi Settings") { _, _ ->
                    listener.onDialogPositiveClick()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    listener.onDialogNegativeClick()
                }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }

}