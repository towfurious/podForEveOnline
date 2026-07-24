package com.podforeve.tracker.platform

import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Used only for the EVE SSO login URL (see LoginScreen.kt) — Custom Tabs normally share
// Chrome's own cookie jar, so without this, PodForEve's own logout (which correctly wipes its
// local refresh token + cache) can be silently bypassed: tapping login again just reuses
// Chrome's still-live EVE SSO session with no credential prompt. Ephemeral browsing scopes
// cookies/history/credentials to this one Custom Tab instance and discards them when it closes.
// Falls back to a normal (session-sharing) tab when unsupported. Requires Chrome 136+, but that
// alone isn't sufficient — device-verified 2026-07-24 that isEphemeralBrowsingSupported() can
// still return false on Chrome 150 (likely gated behind a server-side Chrome rollout flag, not
// purely a version check) — this is a graceful, forward-compatible fallback, not a guarantee.
// See wiki: [[OAuth2 PKCE]] "Logout" section, [[ADR-008 - OAuth2 PKCE via System Browser]].
private const val CHROME_PACKAGE = "com.android.chrome"

@Composable
actual fun rememberUrlLauncher(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            val ephemeral = CustomTabsClient.isEphemeralBrowsingSupported(context, CHROME_PACKAGE)
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setEphemeralBrowsingEnabled(ephemeral)
                .build()
                .launchUrl(context, Uri.parse(url))
        }
    }
}
