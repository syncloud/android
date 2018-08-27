package org.syncloud.android;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.CoreConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class AcraLogEmailerFactory implements ReportSenderFactory {
    public AcraLogEmailerFactory() {

    }

    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        return new AcraLogEmailer(context, config);
    }
}
