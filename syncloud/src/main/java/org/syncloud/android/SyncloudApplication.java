package org.syncloud.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.data.StringFormat;
import org.apache.log4j.Logger;
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.RedirectService;
import org.syncloud.android.core.redirect.UserCachedService;
import org.syncloud.android.core.redirect.UserStorage;

import java.io.File;

import static org.acra.ReportField.*;
import static org.syncloud.android.core.redirect.RedirectService.getApiUrl;

@AcraDialog(
        resText = R.string.crash_dialog_text,
        resIcon = R.drawable.ic_launcher, //optional. default is a warning sign
        resTitle = R.string.crash_dialog_title // optional. default is your application name
)
@AcraCore(
        buildConfigClass = BuildConfig.class,
        reportContent = { APP_VERSION_CODE, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT },

        logcatArguments = { "-t", "500", "-v", "long", "*:D"},
        logcatFilterByPid = false,

        reportSenderFactoryClasses = { AcraLogEmailerFactory.class },
        reportFormat = StringFormat.KEY_VALUE_LIST

)
public class SyncloudApplication extends Application {

    private Preferences preferences;
    private UserStorage userStorage;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {

        ConfigureLog4J.configure();

        Logger logger = Logger.getLogger(SyncloudApplication.class);
        logger.info("Starting Syncloud App");

        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = new Preferences(sharedPreferences);
        userStorage = new UserStorage(new File(getApplicationContext().getFilesDir(), "user.json"));
    }

    public IUserService userServiceCached() {
        RedirectService redirectService = new RedirectService(getApiUrl(preferences.getMainDomain()));
        UserCachedService userService = new UserCachedService(redirectService, userStorage);
        return userService;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void reportError() {
        ACRA.getErrorReporter().handleSilentException(null);
    }

    public boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}
