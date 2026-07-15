@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.platform

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock

// See wiki: [[ADR-015 - Unified Completion Notifications]], [[ADR-007 - iOS Local Notifications]]
actual class NotificationScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun reconcile(source: NotificationSource, items: List<ScheduledCompletion>) {
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val staleIds = requests
                .orEmpty()
                .filterIsInstance<UNNotificationRequest>()
                .map { it.identifier }
                .filter { it.startsWith(source.idPrefix) }

            if (staleIds.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(staleIds)
            }

            val now = Clock.System.now().epochSeconds
            items.filter { it.epochSeconds > now }.forEach { item ->
                val content = UNMutableNotificationContent().apply {
                    setTitle(item.title)
                    setBody(item.body)
                    setSound(UNNotificationSound.defaultSound())
                }
                val interval = (item.epochSeconds - now).toDouble().coerceAtLeast(1.0)
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                    timeInterval = interval,
                    repeats = false,
                )
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = item.id,
                    content = content,
                    trigger = trigger,
                )
                center.addNotificationRequest(request, withCompletionHandler = null)
            }
        }
    }
}
