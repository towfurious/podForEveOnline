package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable

// Intercepts the system back gesture/button. Voyager's TabNavigator deliberately passes
// onBackPressed = null to its internal Navigator (confirmed by reading voyager-tab-navigator's
// sources), so nothing pops or reroutes back while a bottom-nav tab is showing — it falls
// straight through to the OS, which exits the app to the launcher. See wiki: Guide - App Store
// Launch Readiness P2 "back button exits to launcher".
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
