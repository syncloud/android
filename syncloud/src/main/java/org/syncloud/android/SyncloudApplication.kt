package org.syncloud.android

import android.app.Application
import android.content.Context
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.ReportField
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.syncloud.android.core.common.WebService
import org.syncloud.android.core.common.http.HttpClient
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.RedirectService
import org.syncloud.android.core.redirect.UserCachedService
import org.syncloud.android.core.redirect.UserStorage
import java.io.File

class SyncloudApplication : Application() {
    private lateinit var _userStorage: UserStorage
    lateinit var preferences: Preferences
    lateinit var userServiceCached: IUserService


    override fun onCreate() {
        val logger = Logger.getLogger(SyncloudApplication::class.java)
        logger.info("Starting Syncloud App")
        super.onCreate()
        preferences = Preferences(getSharedPreferences("${packageName}_preferences", MODE_PRIVATE))
        _userStorage = UserStorage(File(applicationContext.filesDir, "user.json"))
        userServiceCached = webServiceAuthWithFileBackedCache()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportContent = listOf(
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
            )
            logcatArguments = listOf("-t", "500", "-v", "long", "*:D")
            logcatFilterByPid = false
            reportFormat = StringFormat.KEY_VALUE_LIST
            dialog {
                text = getString(R.string.crash_dialog_text)
                resIcon = R.drawable.ic_launcher
                title = getString(R.string.crash_dialog_title)
            }
            mailSender {
                mailTo = "support@syncloud.it"
                subject = "Syncloud Android Report"
            }
        }
    }

    private fun webServiceAuthWithFileBackedCache(): UserCachedService {
        val redirectService = RedirectService(preferences.mainDomain, WebService(HttpClient()))
        return UserCachedService(redirectService, _userStorage)
    }

    fun reportError() = ACRA.errorReporter.handleSilentException(null)
}