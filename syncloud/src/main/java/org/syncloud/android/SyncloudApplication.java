package org.syncloud.android;

import android.app.Application;

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
        put("remote_access", Remote_Access.class);
        put("insider", DeviceActivateActivity.class);
        put("owncloud", Owncloud.class);
    }};
    private Db db;
    private UserService userService;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new Db(getApplicationContext());
        userService = new UserService(getResources().getString(R.string.api_url));
    }

    public Db getDb() {
        return db;
    }

    public UserService getUserService() {
        return userService;
    }
}
