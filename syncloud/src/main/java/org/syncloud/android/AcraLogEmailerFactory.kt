package org.syncloud.android

import android.content.Context
import org.acra.sender.ReportSenderFactory
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import org.syncloud.android.AcraLogEmailer

class AcraLogEmailerFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender =
        AcraLogEmailer(context, config)
}