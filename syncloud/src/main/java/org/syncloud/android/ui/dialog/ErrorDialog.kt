package org.syncloud.android.ui.dialog

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.syncloud.android.R
import org.syncloud.android.SyncloudApplication

class ErrorDialog(private val context: Activity, message: String) : AlertDialog(
    context
) {
    private val application: SyncloudApplication
    private val message: String
    override fun onCreate(savedInstanceState: Bundle) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_error, null)
        val viewMessage = view.findViewById<View>(R.id.view_message) as TextView
        viewMessage.text = message
        val btnReport = view.findViewById<View>(R.id.btn_report) as Button
        btnReport.setOnClickListener { reportError() }
        setView(view)
        super.onCreate(savedInstanceState)
    }

    fun reportError() {
        application.reportError()
    }

    init {
        application = context.application as SyncloudApplication
        this.message = message
        setCancelable(true)
    }
}