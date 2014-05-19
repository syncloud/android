package org.syncloud.android.config;

import org.syncloud.android.activity.app.Insider;
import org.syncloud.android.activity.app.Owncloud;
import org.syncloud.android.activity.app.Remote_Access;

import java.util.HashMap;
import java.util.Map;

public class AppRegistry {
    public static Map<String, Class> registry = new HashMap<String, Class>() {{
       put("remote_access", Remote_Access.class);
       put("insider", Insider.class);
       put("owncloud", Owncloud.class);
    }};
}
