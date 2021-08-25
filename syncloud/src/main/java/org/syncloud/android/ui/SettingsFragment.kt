package org.syncloud.android.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import com.google.common.collect.Sets
import org.apache.log4j.Logger
import org.syncloud.android.*
import org.syncloud.android.ui.AuthActivity
import org.syncloud.android.ui.SettingsFragment

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    private lateinit var removeAccountPref: Preference
    private lateinit var feedbackPref: Preference
    private lateinit var application: SyncloudApplication
    private val summaryUpdatable: Set<String> = Sets.newHashSet(PreferencesConstants.KEY_PREF_MAIN_DOMAIN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = activity.application as SyncloudApplication
        addPreferencesFromResource(R.xml.preferences)
        removeAccountPref = findPreference(PreferencesConstants.KEY_PREF_ACCOUNT_REMOVE)
        removeAccountPref.setOnPreferenceClickListener(OnPreferenceClickListener {
            val preferences = preferenceScreen.sharedPreferences
            val editor = preferences.edit()
            editor.putString(PreferencesConstants.KEY_PREF_EMAIL, null)
            editor.putString(PreferencesConstants.KEY_PREF_PASSWORD, null)
            editor.apply()
            updateSummary(preferences, PreferencesConstants.KEY_PREF_EMAIL)
            val intent = Intent(this@SettingsFragment.activity, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            true
        })
        feedbackPref = findPreference(PreferencesConstants.KEY_PREF_FEEDBACK_SEND)
        feedbackPref.setOnPreferenceClickListener(OnPreferenceClickListener {
            application.reportError()
            true
        })
        val preferences = preferenceScreen.sharedPreferences
        preferences.registerOnSharedPreferenceChangeListener(this)
        for (pref in summaryUpdatable) {
            updateSummary(preferences, pref)
        }
        updateSummary(preferences, PreferencesConstants.KEY_PREF_EMAIL)
        updateRemoveAccountPref(preferences)
    }

    private fun updateRemoveAccountPref(sharedPreferences: SharedPreferences) {
        val email = sharedPreferences.getString(PreferencesConstants.KEY_PREF_EMAIL, null)
        removeAccountPref.isEnabled = email != null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PreferencesConstants.KEY_PREF_EMAIL) {
            updateRemoveAccountPref(sharedPreferences)
        } else if (summaryUpdatable.contains(key)) {
            updateSummary(sharedPreferences, key)
        }
    }

    private fun updateSummary(sharedPreferences: SharedPreferences, key: String) {
        logger.debug("updating: $key")
        val summary = getSummary(sharedPreferences, key)
        logger.debug("summary: $summary")
        findPreference(key).summary = summary
    }

    private fun getSummary(sharedPreferences: SharedPreferences, key: String): String {
        val summary = sharedPreferences.getString(key, null)
        if (summary != null) return summary
        return if (key == PreferencesConstants.KEY_PREF_EMAIL) "Not specified yet" else "None"
    }

    companion object {
        private val logger = Logger.getLogger(SettingsFragment::class.java.name)
    }
}