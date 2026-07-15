@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.platform

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.podforeve.tracker.platform.service.SkillTrainingService
import kotlin.time.Clock

private const val TAG = "NotificationScheduler"

internal const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
internal const val EXTRA_TITLE = "extra_title"
internal const val EXTRA_BODY = "extra_body"
internal const val EXTRA_CHANNEL_ID = "extra_channel_id"
internal const val EXTRA_TARGET_EPOCH_SECONDS = "extra_target_epoch_seconds"

// Shared by the Service's live ticker, Service.startForeground()'s anchor post, and the backup
// alarm, so all three ever post to the exact same notification slot instead of leaving orphaned
// duplicates. ServiceCompat.startForeground() only accepts a plain Int id (no tag), which is why
// this is a fixed Int rather than the per-item String id used for job/extractor.
internal const val SKILL_LIVE_NOTIFICATION_ID = 1001

private const val PREFS_NAME = "notification_scheduler_prefs"

// Idempotent — safe to call from multiple entry points. Channels only need to exist before a
// notification targeting them is posted; NotificationScheduler.init{} covers the normal
// reconcile()-driven path, but SkillTrainingService also calls this defensively in case it's
// ever started through a path that doesn't go through NotificationScheduler's constructor first.
internal fun ensureNotificationChannels(context: Context) {
    val manager = NotificationManagerCompat.from(context)
    NotificationSource.entries.forEach { source ->
        manager.createNotificationChannel(
            NotificationChannel(source.channelId, source.channelLabel, NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
}

// See wiki: [[ADR-015 - Unified Completion Notifications]]
actual class NotificationScheduler(private val context: Context) {
    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    init {
        ensureNotificationChannels(context)
    }

    // Callers (repositories) must never have a notification-scheduling failure break their own
    // fetch pipeline, so every failure — whatever its type — is caught and logged here rather
    // than propagated (a SecurityException from a background-FGS-start restriction, an
    // IllegalStateException from the OS, etc. are all equally "not the caller's problem").
    @Suppress("TooGenericExceptionCaught")
    actual fun reconcile(source: NotificationSource, items: List<ScheduledCompletion>) {
        try {
            val now = Clock.System.now().epochSeconds
            items.forEach { Log.d(TAG, "  item id=${it.id} epochSeconds=${it.epochSeconds} now=$now diff=${it.epochSeconds - now}") }
            val future = items.filter { it.epochSeconds > now }
            Log.d(TAG, "reconcile($source, ${items.size} items, ${future.size} future)")

            if (source == NotificationSource.SKILL) {
                reconcileSkillService(future.firstOrNull())
            }
            reconcileAlarms(source, future)
        } catch (e: Exception) {
            Log.e(TAG, "reconcile($source) failed", e)
        }
    }

    private fun reconcileSkillService(item: ScheduledCompletion?) {
        val intent = Intent(context, SkillTrainingService::class.java)
        if (item == null) {
            Log.d(TAG, "reconcileSkillService: no active skill, stopping service")
            context.stopService(intent)
            return
        }
        intent.putExtra(EXTRA_TITLE, item.title)
        intent.putExtra(EXTRA_TARGET_EPOCH_SECONDS, item.epochSeconds)
        Log.d(TAG, "reconcileSkillService: starting service for '${item.title}' at ${item.epochSeconds}")
        ContextCompat.startForegroundService(context, intent)
    }

    // Also used as SKILL's backup alarm — fires the completion notification if the
    // ForegroundService gets killed by the OS before it can detect completion itself.
    private fun reconcileAlarms(source: NotificationSource, items: List<ScheduledCompletion>) {
        val prefsKey = "scheduled_ids_${source.name}"
        prefs.getStringSet(prefsKey, emptySet()).orEmpty().forEach { cancelAlarm(it) }

        items.forEach { item -> scheduleAlarm(source, item) }

        prefs.edit { putStringSet(prefsKey, items.map { it.id }.toSet()) }
    }

    // canScheduleExact below already gates the exact-alarm call on canScheduleExactAlarms();
    // lint's static check can't see across that branch and flags the call anyway.
    @SuppressLint("MissingPermission")
    private fun scheduleAlarm(source: NotificationSource, item: ScheduledCompletion) {
        val notificationId = if (source == NotificationSource.SKILL) SKILL_LIVE_NOTIFICATION_ID else item.id.hashCode()
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TITLE, item.title)
            putExtra(EXTRA_BODY, item.body)
            putExtra(EXTRA_CHANNEL_ID, source.channelId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        val triggerAtMillis = item.epochSeconds * 1000
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            // Permission not granted — degrade to inexact delivery rather than crash.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(id: String) {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
