package org.syncloud.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.config.ACRAConfig;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.ENVIRONMENT;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

public class AcraLogEmailer implements ReportSender {

    private final Context mContext;
    private final ACRAConfig config;

    public AcraLogEmailer(Context ctx, ACRAConfig config) {
        this.mContext = ctx;
        this.config = config;
    }

    @Override
    public void send(Context context, CrashReportData errorContent) throws ReportSenderException {
        final String mailTo = "support@syncloud.it";
        final String subject = "Syncloud Android Report";
        final String body = buildBodyText(errorContent);

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.fromParts("mailto", mailTo, null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        mContext.startActivity(emailIntent);
    }

    private String buildBodyText(CrashReportData errorContent) {
        ReportField[] fields = config.customReportContent();
        if(fields.length == 0) {
            fields = new ReportField[] {
                ANDROID_VERSION,
                APP_VERSION_NAME,
                BRAND,
                PHONE_MODEL,
                STACK_TRACE
            };
        }

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            if (field != ReportField.LOGCAT) {
                builder.append(field.toString()).append("=");
                builder.append(errorContent.get(field));
                builder.append('\n');
            }
        }
        builder.append("LOGCAT");
        builder.append('\n');
        builder.append(errorContent.get(ReportField.LOGCAT));
        return builder.toString();
    }
}
