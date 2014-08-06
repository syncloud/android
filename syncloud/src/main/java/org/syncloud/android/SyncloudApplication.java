package org.syncloud.android;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.syncloud.android.activity.DeviceActivateActivity;
import org.syncloud.android.activity.app.Owncloud;
import org.syncloud.android.activity.app.Remote_Access;
import org.syncloud.android.db.Db;
import org.syncloud.redirect.UserService;

import java.util.HashMap;
import java.util.Map;

public class SyncloudApplication extends Application {

    public static String DEVICE = "device";

    public static Map<String, Class> appRegistry = new HashMap<String, Class>() {{
        put("remote", Remote_Access.class);
        put("insider", DeviceActivateActivity.class);
        put("owncloud", Owncloud.class);
    }};
    private Db db;
    private Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new Db(getApplicationContext());
        preferences = new Preferences(PreferenceManager.getDefaultSharedPreferences(this));
    }

    public Db getDb() {
        return db;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
