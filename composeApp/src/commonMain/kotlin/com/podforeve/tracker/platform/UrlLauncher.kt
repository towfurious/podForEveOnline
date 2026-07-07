package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable

// Returns a lambda that opens the given URL in the platform system browser.
// Android: Chrome Custom Tabs. iOS: UIApplication.openURL.
// See wiki: [[ADR-008 - OAuth2 PKCE via System Browser]]
@Composable
expect fun rememberUrlLauncher(): (String) -> Unit
