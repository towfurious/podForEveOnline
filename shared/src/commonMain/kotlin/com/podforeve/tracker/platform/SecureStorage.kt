package com.podforeve.tracker.platform

// expect/actual wrapper over Android Keystore (EncryptedSharedPreferences)
// and iOS Keychain (Security.framework). See wiki: [[SecureStorage]]
expect class SecureStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun delete(key: String)
}

// Key constants shared by all platforms
object SecureStorageKeys {
    const val REFRESH_TOKEN = "eve.refresh_token"
    const val THEME = "app.theme"
}
