package org.syncloud.android

import android.content.SharedPreferences

class Preferences(private val preferences: SharedPreferences) {
    val mainDomain: String
        get() = preferences.getString(KEY_PREF_MAIN_DOMAIN, "syncloud.it")!!
    val redirectEmail: String
        get() = preferences.getString(KEY_PREF_EMAIL, null)!!
    val redirectPassword: String
        get() = preferences.getString(KEY_PREF_PASSWORD, null)!!

    fun hasCredentials(): Boolean = redirectEmail != null

    fun setCredentials(email: String?, password: String?) {
        val editor = preferences.edit()
        editor.putString(KEY_PREF_EMAIL, email)
        editor.putString(KEY_PREF_PASSWORD, password)
        editor.apply()
    }

    companion object {
        const val KEY_PREF_MAIN_DOMAIN = "pref_main_domain"
        const val KEY_PREF_ACCOUNT_REMOVE = "pref_account_remove"
        const val KEY_PREF_EMAIL = "pref_email"
        const val KEY_PREF_PASSWORD = "pref_password"
        const val KEY_PREF_FEEDBACK_SEND = "pref_feedback_send"
    }
}