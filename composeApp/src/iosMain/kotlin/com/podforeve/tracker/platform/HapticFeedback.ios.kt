package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberHapticFeedback(): () -> Unit = {
    UISelectionFeedbackGenerator().selectionChanged()
}
