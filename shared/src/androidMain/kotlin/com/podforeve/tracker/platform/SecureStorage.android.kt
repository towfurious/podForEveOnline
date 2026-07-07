package com.podforeve.tracker.platform

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class SecureStorage {
    private val prefs by lazy {
        val context = AppContext.instance
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "eve_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun read(key: String): String? = prefs.getString(key, null)

    actual fun write(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }
}
