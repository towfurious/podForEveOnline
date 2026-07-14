package com.podforeve.tracker.platform

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

actual class SecureStorage {
    @Suppress("SwallowedException")
    private val prefs by lazy {
        val context = AppContext.instance
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
        prefs.edit().putString(key, value).apply()
    }

    actual fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }
}
