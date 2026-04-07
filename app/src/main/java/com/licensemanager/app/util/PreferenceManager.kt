package com.licensemanager.app.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(Constants.KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? = prefs.getString(Constants.KEY_API_KEY, null)

    fun saveUserName(name: String) {
        prefs.edit().putString(Constants.KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String? = prefs.getString(Constants.KEY_USER_NAME, null)

    fun saveUserPhone(phone: String) {
        prefs.edit().putString(Constants.KEY_USER_PHONE, phone).apply()
    }

    fun getUserPhone(): String? = prefs.getString(Constants.KEY_USER_PHONE, null)

    val isLoggedIn: Boolean get() = !getApiKey().isNullOrEmpty()

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
