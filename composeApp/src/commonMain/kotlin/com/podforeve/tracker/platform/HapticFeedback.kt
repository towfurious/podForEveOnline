package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberHapticFeedback(): () -> Unit
