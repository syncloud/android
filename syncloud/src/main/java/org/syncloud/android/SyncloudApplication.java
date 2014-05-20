package org.syncloud.android;

import android.app.Application;

import org.syncloud.android.activity.app.Insider;
import org.syncloud.android.activity.app.Owncloud;
import org.syncloud.android.activity.app.Remote_Access;
import org.syncloud.android.adapter.DevicesAdapter;

import java.util.HashMap;
import java.util.Map;

public class SyncloudApplication extends Application {

    public static String DEVICE = "device";
    public static String DEVICE_ADAPTER = "device_adapter";

    public static Map<String, Class> appRegistry = new HashMap<String, Class>() {{
        put("remote_access", Remote_Access.class);
        put("insider", Insider.class);
        put("owncloud", Owncloud.class);
    }};
    private DevicesAdapter devicesAdapter;

    public DevicesAdapter getDevicesAdapter() {
        return devicesAdapter;
    }

    public void setDevicesAdapter(DevicesAdapter devicesAdapter) {
        this.devicesAdapter = devicesAdapter;
    }
}
