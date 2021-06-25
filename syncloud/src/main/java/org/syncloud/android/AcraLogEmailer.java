package org.syncloud.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;

import org.acra.ReportField;
import org.acra.collections.ImmutableSet;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

public class AcraLogEmailer implements ReportSender {

    private final Context mContext;
    private final CoreConfiguration config;

    public AcraLogEmailer(Context ctx, CoreConfiguration config) {
        this.mContext = ctx;
        this.config = config;
    }

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData errorContent) throws ReportSenderException {
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
        ImmutableSet<ReportField> fields = config.reportContent();
        if(fields.isEmpty()) {
            fields = new ImmutableSet<>(
                    ANDROID_VERSION,
                    APP_VERSION_NAME,
                    BRAND,
                    PHONE_MODEL,
                    STACK_TRACE
            );
        }

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            if (field != ReportField.LOGCAT) {
                builder.append(field.toString()).append("=");
                builder.append(errorContent.getString(field));
                builder.append('\n');
            }
        }
        builder.append("LOGCAT");
        builder.append('\n');
        builder.append(errorContent.getString(ReportField.LOGCAT));
        return builder.toString();
    }
}
