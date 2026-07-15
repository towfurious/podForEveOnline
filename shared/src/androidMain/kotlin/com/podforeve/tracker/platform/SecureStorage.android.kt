package com.podforeve.tracker.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

actual class SecureStorage(private val context: Context) {
    @Suppress("SwallowedException")
    private val prefs by lazy {
        fun build(): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                "eve_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

        // Keystore key invalidated after reinstall or biometric change —
        // wipe the stale encrypted file and start fresh (user must re-auth).
        fun rebuild(): SharedPreferences {
            context.deleteSharedPreferences("eve_secure_prefs")
            return build()
        }
        try {
            build()
        } catch (e: GeneralSecurityException) {
            rebuild()
        } catch (e: IOException) {
            rebuild()
        }
    }

    actual fun read(key: String): String? = prefs.getString(key, null)

    actual fun write(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    actual fun delete(key: String) {
        prefs.edit { remove(key) }
    }
}
