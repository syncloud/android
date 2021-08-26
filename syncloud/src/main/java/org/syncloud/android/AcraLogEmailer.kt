package org.syncloud.android

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import kotlin.Throws
import org.acra.sender.ReportSenderException
import org.acra.data.CrashReportData
import android.content.Intent
import android.net.Uri
import org.acra.ReportField
import org.acra.collections.ImmutableSet
import java.lang.StringBuilder

class AcraLogEmailer(private val mContext: Context, private val config: CoreConfiguration) :
    ReportSender {
    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData) {
        val mailTo = "support@syncloud.it"
        val subject = "Syncloud Android Report"
        val body = buildBodyText(errorContent)
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.fromParts("mailto", mailTo, null)
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        mContext.startActivity(emailIntent)
    }

    private fun buildBodyText(errorContent: CrashReportData): String {
        var fields = config.reportContent()
        if (fields.isEmpty()) {
            fields = ImmutableSet(
                ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_NAME,
                ReportField.BRAND,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE
            )
        }
        val builder = StringBuilder()
        for (field in fields) {
            if (field != ReportField.LOGCAT) {
                builder.append(field.toString()).append("=")
                builder.append(errorContent.getString(field))
                builder.append('\n')
            }
        }
        builder.append("LOGCAT")
        builder.append('\n')
        builder.append(errorContent.getString(ReportField.LOGCAT))
        return builder.toString()
    }
}