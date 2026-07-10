package com.podforeve.tracker.ui.theme

import com.podforeve.tracker.platform.SecureStorage
import com.podforeve.tracker.platform.SecureStorageKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeRepository(private val storage: SecureStorage) {

    private val _themeFlow = MutableStateFlow(load())
    val themeFlow: StateFlow<AppTheme> get() = _themeFlow

    var current: AppTheme
        get() = _themeFlow.value
        set(value) {
            _themeFlow.value = value
            storage.write(SecureStorageKeys.THEME, value.name)
        }

    private fun load(): AppTheme {
        val name = storage.read(SecureStorageKeys.THEME) ?: return AppTheme.EMBER
        return AppTheme.entries.find { it.name == name } ?: AppTheme.EMBER
    }
}
