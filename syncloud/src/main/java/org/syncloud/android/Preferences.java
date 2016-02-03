package org.syncloud.android;

import android.content.SharedPreferences;

public class Preferences {

    public static final String KEY_PREF_MAIN_DOMAIN = "pref_main_domain";
    public static final String KEY_PREF_ACCOUNT_REMOVE = "pref_account_remove";
    public static final String KEY_PREF_EMAIL = "pref_email";
    public static final String KEY_PREF_PASSWORD = "pref_password";
    public static final String KEY_PREF_FEEDBACK_SEND= "pref_feedback_send";

    private SharedPreferences preferences;

    public Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getMainDomain() {
        return preferences.getString(KEY_PREF_MAIN_DOMAIN, "syncloud.it");
    }

    public void setCredentials(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PREF_EMAIL, email);
        editor.putString(KEY_PREF_PASSWORD, password);
        editor.apply();
    }

    public String getEmail() {
        return preferences.getString(KEY_PREF_EMAIL, null);
    }

    public String getPassword() {
        return preferences.getString(KEY_PREF_PASSWORD, null);
    }

    public boolean hasCredentials() {
        return getEmail() != null;
    }
}
