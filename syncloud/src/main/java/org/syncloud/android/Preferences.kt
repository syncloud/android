package org.syncloud.android

import android.content.SharedPreferences
import org.syncloud.android.ui.PreferencesConstants

class Preferences(private val preferences: SharedPreferences) {
    val mainDomain: String get() = preferences.getString(PreferencesConstants.KEY_PREF_MAIN_DOMAIN, "syncloud.it")!!
    val redirectEmail: String? get() = preferences.getString(PreferencesConstants.KEY_PREF_EMAIL, null)
    val redirectPassword: String? get() = preferences.getString(PreferencesConstants.KEY_PREF_PASSWORD, null)

    fun hasCredentials(): Boolean = redirectEmail != null
    fun setCredentials(email: String?, password: String?) {
        val editor = preferences.edit()
        editor.putString(PreferencesConstants.KEY_PREF_EMAIL, email)
        editor.putString(PreferencesConstants.KEY_PREF_PASSWORD, password)
        editor.apply()
    }
}