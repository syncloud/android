package org.syncloud.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.syncloud.android.log.ConfigureLog4J;
import org.syncloud.android.ui.DeviceActivateActivity;
import org.syncloud.android.ui.apps.Owncloud;
import org.syncloud.android.db.Db;

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
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
        resDialogOkToast = R.string.crash_dialog_ok_toast, // optional. displays a Toast message when the user accepts to send a report.
        logcatArguments = { "-t", "200", "-v", "long", "*:D"},
        logcatFilterByPid = false
)
public class SyncloudApplication extends Application {

    public static String DEVICE = "device";
    public static String DEVICE_ENDPOINT = "device_endpoint";

    public static Map<String, Class> appRegistry = new HashMap<String, Class>() {{
//        put("remote", Remote_Access.class);
        put("insider", DeviceActivateActivity.class);
        put("owncloud", Owncloud.class);
    }};
    private Db db;
    private Preferences preferences;

    @Override
    public void onCreate() {

        ACRA.init(this);
        ConfigureLog4J.configure();

        super.onCreate();
        db = new Db(getApplicationContext());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = new Preferences(sharedPreferences);
    }

    public Db getDb() {
        return db;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
