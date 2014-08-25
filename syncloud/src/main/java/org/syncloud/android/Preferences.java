package org.syncloud.android;

import android.content.SharedPreferences;

import java.net.MalformedURLException;
import java.net.URL;

import static org.syncloud.android.fragment.SettingsFragment.KEY_PREF_API_URL;
import static org.syncloud.android.fragment.SettingsFragment.KEY_PREF_DEBUG_MODE;

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
            return new URL(getApiUrl()).getHost().replace("api.", "");
        } catch (MalformedURLException e) {
            return "syncloud.it";
        }
    }

    public Boolean isDebug() {
        return preferences.getBoolean(KEY_PREF_DEBUG_MODE, false);
    }
}
