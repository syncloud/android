package org.syncloud.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.syncloud.android.db.KeysStorage;
import org.syncloud.android.log.ConfigureLog4J;
import org.syncloud.android.ui.apps.GitBucket;
import org.syncloud.android.ui.apps.Owncloud;
import org.syncloud.redirect.IUserService;
import org.syncloud.redirect.RedirectService;
import org.syncloud.redirect.UserCachedService;
import org.syncloud.redirect.UserStorage;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.EndpointSelector;
import org.syncloud.platform.ssh.SelectorConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.Device;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.acra.ReportField.*;

@ReportsCrashes(
        formKey = "", // will not be used
        mailTo = "support@syncloud.it",
        mode = ReportingInteractionMode.DIALOG,
        customReportContent = {USER_COMMENT, APP_VERSION_CODE, ANDROID_VERSION, PHONE_MODEL, CUSTOM_DATA, STACK_TRACE, LOGCAT },
        resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = R.drawable.ic_launcher, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogOkToast = R.string.crash_dialog_ok_toast, // optional. displays a Toast message when the user accepts to send a report.
        logcatArguments = { "-t", "500", "-v", "long", "*:D"},
        logcatFilterByPid = false
)
public class SyncloudApplication extends Application {

    private String TAG = SyncloudApplication.class.getSimpleName();
    public static String DOMAIN = "domain";

    public static String DEVICE_ENDPOINT = "device_endpoint";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_HOST = "device_host";

    public static Map<String, Class> appRegistry = new HashMap<String, Class>() {{
        put("syncloud-owncloud", Owncloud.class);
        put("syncloud-gitbucket", GitBucket.class);
    }};
    private KeysStorage keysStorage;
    private Preferences preferences;
    private UserStorage userStorage;

    @Override
    public void onCreate() {

        ACRA.init(this);
        ACRA.getErrorReporter().addReportSender(new ReportSender() {
            @Override
            public void send(CrashReportData errorContent) throws ReportSenderException {
                Log.e(TAG, errorContent.getProperty(STACK_TRACE));
            }
        });

        ConfigureLog4J.configure();

        super.onCreate();
        keysStorage = new KeysStorage(getApplicationContext());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = new Preferences(sharedPreferences);
        userStorage = new UserStorage(new File(getApplicationContext().getFilesDir(), "user.json"));
    }

    public KeysStorage keysStorage() { return keysStorage; }

    public IUserService userServiceCached() {
        RedirectService redirectService = new RedirectService(preferences.getApiUrl());
        UserCachedService userService = new UserCachedService(redirectService, userStorage);
        return userService;
    }

    public RedirectService redirectService() {
        RedirectService redirectService = new RedirectService(preferences.getApiUrl());
        return redirectService;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public ConnectionPointProvider connectionPoint(Device device) {
        return new SelectorConnectionPointProvider(
                new SshRunner(),
                new EndpointSelector(preferences),
                getPreferences(),
                device);
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
