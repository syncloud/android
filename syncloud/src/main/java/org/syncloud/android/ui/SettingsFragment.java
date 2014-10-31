package org.syncloud.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import org.acra.ACRA;
import org.syncloud.android.Preferences;
import org.syncloud.android.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference removeAccountPref;
    private Preference feedbackPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        removeAccountPref = findPreference(Preferences.KEY_PREF_ACCOUNT_REMOVE);
        removeAccountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Preferences.KEY_PREF_EMAIL, null);
                editor.putString(Preferences.KEY_PREF_PASSWORD, null);
                editor.apply();
                updateSummary(preferences, Preferences.KEY_PREF_EMAIL);

                Intent intent = new Intent(SettingsFragment.this.getActivity(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                return true;
            }
        });

        feedbackPref = findPreference(Preferences.KEY_PREF_FEEDBACK_SEND);
        feedbackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ACRA.getErrorReporter().handleException(null);
                return true;
            }
        });

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        updateSummary(preferences, Preferences.KEY_PREF_API_URL);
        updateSummary(preferences, Preferences.KEY_PREF_EMAIL);
        updateSummary(preferences, Preferences.KEY_PREF_DISCOVERY_LIBRARY);
        updateRemoveAccountPref(preferences);
    }

    private void updateRemoveAccountPref(SharedPreferences sharedPreferences) {
        String email = sharedPreferences.getString(Preferences.KEY_PREF_EMAIL, null);
        removeAccountPref.setEnabled(email != null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.KEY_PREF_EMAIL)) {
            updateRemoveAccountPref(sharedPreferences);
        }

        if (key.equals(Preferences.KEY_PREF_API_URL)) {
            updateSummary(sharedPreferences, key);
        }

        if (key.equals(Preferences.KEY_PREF_DISCOVERY_LIBRARY)) {
            updateSummary(sharedPreferences, key);
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        String summary = getSummary(sharedPreferences, key);
        findPreference(key).setSummary(summary);
    }

    private String getSummary(SharedPreferences sharedPreferences, String key) {
        String summary = sharedPreferences.getString(key, null);
        if (summary != null)
            return summary;
        if (key.equals(Preferences.KEY_PREF_EMAIL))
            return "Not specified yet";
        return "None";
    }
}
