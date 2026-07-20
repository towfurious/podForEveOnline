package com.podforeve.tracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.podforeve.tracker.platform.SecureStorage
import com.podforeve.tracker.platform.SecureStorageKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.mp.KoinPlatform.getKoin

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

// Android Studio's @Preview renderer never runs the app's real startup code (no
// startKoin{}), so a bare `getKoin().get()` throws "KoinApplication has not been
// started" the moment any Preview reaches a composable that calls this. LocalInspectionMode
// is true specifically inside that preview sandbox (and nowhere else), so guarding on it here
// keeps every real call site — device, emulator, actual app — completely unaffected.
@Composable
fun rememberThemeRepositoryOrNull(): ThemeRepository? = if (LocalInspectionMode.current) null else remember { getKoin().get() }
