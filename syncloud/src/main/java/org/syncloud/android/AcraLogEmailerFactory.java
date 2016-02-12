package org.syncloud.android;

import android.content.Context;

import org.acra.config.ACRAConfig;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class AcraLogEmailerFactory implements ReportSenderFactory {
    public AcraLogEmailerFactory() {

    }

    @Override
    public ReportSender create(Context context, ACRAConfig config) {
        return new AcraLogEmailer(context, config);
    }
}
