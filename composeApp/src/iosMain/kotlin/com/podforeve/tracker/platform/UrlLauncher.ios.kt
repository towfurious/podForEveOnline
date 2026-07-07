package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// Opens the EVE SSO URL in Safari. The `onOpenURL` handler in iOSApp.swift
// catches the eve-tracker://callback and routes it to handleAuthCallback().
@Composable
actual fun rememberUrlLauncher(): (String) -> Unit {
    return remember {
        { url ->
            val nsUrl = NSURL.URLWithString(url) ?: return@remember
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null,
            )
        }
    }
}
