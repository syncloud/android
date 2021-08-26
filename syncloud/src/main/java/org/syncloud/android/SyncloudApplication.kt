package org.syncloud.android

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import com.google.common.collect.Lists.newArrayList
import org.acra.ACRA
import org.acra.ReportField
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.data.StringFormat
import org.apache.log4j.Logger
import org.syncloud.android.ConfigureLog4J.configure
import org.syncloud.android.core.redirect.IUserService
import org.syncloud.android.core.redirect.RedirectService
import org.syncloud.android.core.redirect.UserCachedService
import org.syncloud.android.core.redirect.UserStorage
import org.syncloud.android.core.redirect.model.User
import java.io.File

@AcraDialog(
    resText = R.string.crash_dialog_text,
    resIcon = R.drawable.ic_launcher,
    resTitle = R.string.crash_dialog_title
)
@AcraCore(
    buildConfigClass = BuildConfig::class,
    reportContent = [ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.STACK_TRACE, ReportField.LOGCAT],
    logcatArguments = ["-t", "500", "-v", "long", "*:D"],
    logcatFilterByPid = false,
    reportSenderFactoryClasses = [AcraLogEmailerFactory::class],
    reportFormat = StringFormat.KEY_VALUE_LIST
)
class SyncloudApplication : Application() {
    private lateinit var _preferences: Preferences
    private lateinit var _userStorage: UserStorage
    private lateinit var _userService : IUserService

    val preferences : Preferences get () = _preferences
    val userServiceCached : IUserService get() = _userService
    val isWifiConnected: Boolean
        get() {
            val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return mWifi!!.isConnected
        }

    override fun onCreate() {
        configure()
        val logger = Logger.getLogger(SyncloudApplication::class.java)
        logger.info("Starting Syncloud App")
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        _preferences = Preferences(sharedPreferences)
        _userStorage = UserStorage(File(applicationContext.filesDir, "user.json"))
        _userService = webServiceAuthWithFileBackedCache()

        // used for testing without proper ssl certificates and valid login
        //_userService = bypassLoginHack()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

    private fun webServiceAuthWithFileBackedCache(): UserCachedService {
        val redirectService = RedirectService(RedirectService.getApiUrl(_preferences.mainDomain))
        return UserCachedService(redirectService, _userStorage)
    }

    fun reportError() = ACRA.getErrorReporter().handleSilentException(null)
}

fun bypassLoginHack() : IUserService {
    return object : IUserService {
        override fun getUser(email: String?, password: String?): User? {
            val user = User()
            user.active = true
            user.email = "fakeBypassService@fake.com"
            user.domains = newArrayList()
            return user
        }

        override fun createUser(email: String?, password: String?): User? {
            throw NotImplementedError("This is not implemented as it is irrelevant")
        }
    }
}