package com.rnkotlincompose.perse.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_NAME = "perse_secure_prefs"
    private const val KEY_TOKEN = "gorest_token"

    fun getToken(context: Context): String {
        val prefs = securePrefs(context)
        return prefs.getString(KEY_TOKEN, "") ?: ""
    }

    fun saveToken(context: Context, token: String) {
        val prefs = securePrefs(context)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken(context: Context) {
        val prefs = securePrefs(context)
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
