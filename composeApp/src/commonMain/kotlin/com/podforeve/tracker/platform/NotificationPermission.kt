package com.podforeve.tracker.platform

import androidx.compose.runtime.Composable

// Requests the OS notification permission once. See wiki: [[ADR-015 - Unified Completion Notifications]]
@Composable
expect fun RequestNotificationPermissionEffect()
