package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable

// iOS has no hardware/gesture back button that exits the app the way Android's system back
// does — nothing to intercept.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
