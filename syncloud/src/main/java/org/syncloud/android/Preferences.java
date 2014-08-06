package org.syncloud.android;

import android.content.SharedPreferences;

import java.net.MalformedURLException;
import java.net.URL;

import static org.syncloud.android.fragment.SettingsFragment.KEY_PREF_API_URL;

public class Preferences {
    private SharedPreferences preferences;

    public Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getApiUrl() {
        return preferences.getString(KEY_PREF_API_URL, "");
    }

    public String getDomain() {
        try {
            return new URL(getApiUrl()).getHost().split("api\\.")[0];
        } catch (MalformedURLException e) {
            return "syncloud.it";
        }
    }
}
