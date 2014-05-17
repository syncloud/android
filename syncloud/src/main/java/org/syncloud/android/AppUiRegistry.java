package org.syncloud.android;

import org.syncloud.android.app.Insider;
import org.syncloud.android.app.Remote_Access;

import java.util.HashMap;
import java.util.Map;

public class AppUiRegistry {
    public static Map<String, Class> registry = new HashMap<String, Class>() {{
       put("remote_access", Remote_Access.class);
       put("insider", Insider.class);
    }};
}
