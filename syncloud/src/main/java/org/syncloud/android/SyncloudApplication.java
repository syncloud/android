package org.syncloud.android;

import android.app.Application;
import android.preference.PreferenceManager;

import com.google.common.eventbus.EventBus;

import org.syncloud.android.discovery.EventCache;
import org.syncloud.android.ui.DeviceActivateActivity;
import org.syncloud.android.ui.apps.Owncloud;
import org.syncloud.android.db.Db;

import java.util.HashMap;
import java.util.Map;

public class SyncloudApplication extends Application {

    public static String DEVICE = "device";
    public static String DEVICE_ENDPOINT = "device_endpoint";
    public EventBus eventbus = new EventBus();
    public EventCache discoveryEventCache = new EventCache();

    public static Map<String, Class> appRegistry = new HashMap<String, Class>() {{
//        put("remote", Remote_Access.class);
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
        eventbus.register(discoveryEventCache);
    }

    public Db getDb() {
        return db;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
