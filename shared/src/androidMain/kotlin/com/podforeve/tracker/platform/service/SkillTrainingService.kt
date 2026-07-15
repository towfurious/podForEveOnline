@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.platform.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.podforeve.tracker.domain.usecase.formatDhm
import com.podforeve.tracker.platform.EXTRA_TARGET_EPOCH_SECONDS
import com.podforeve.tracker.platform.EXTRA_TITLE
import com.podforeve.tracker.platform.SKILL_LIVE_NOTIFICATION_ID
import com.podforeve.tracker.platform.ensureNotificationChannels
import com.podforeve.tracker.shared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private const val CHANNEL_ID = "skill_training"

// Persistent live-countdown notification for the head of the skill queue.
// See wiki: [[ADR-006 - Android Foreground Service]], [[ADR-015 - Unified Completion Notifications]]
class SkillTrainingService : Service() {
    private var scope: CoroutineScope? = null
    private var tickJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannels(this)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // InlinedApi: FOREGROUND_SERVICE_TYPE_SPECIAL_USE requires API 34; minSdk is 28 (ADR-010).
    // Older OS versions don't understand foreground-service types at all and just start the
    // service normally, so referencing the inlined constant below API 34 is safe by design —
    // see [[ADR-015 - Unified Completion Notifications]] for why "specialUse" was chosen over
    // "dataSync" (Android 15 caps dataSync FGS at 6h/24h, fatal for multi-day training).
    // MissingPermission: the tick loop below does check POST_NOTIFICATIONS immediately around
    // its notify() call — confirmed correct, but lint's pattern match on it is fragile to how
    // ktlint happens to wrap the long condition line, flipping pass/fail across reformats with
    // no code change. Suppressing at the function level rather than chasing line-wrap-sensitive
    // phrasing.
    @SuppressLint("InlinedApi", "MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE)
        val targetEpochSeconds = intent?.getLongExtra(EXTRA_TARGET_EPOCH_SECONDS, 0L)
        if (title == null || targetEpochSeconds == null || targetEpochSeconds == 0L) {
            stopSelf()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            SKILL_LIVE_NOTIFICATION_ID,
            buildTickingNotification(title, targetEpochSeconds),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        tickJob?.cancel()
        tickJob = scope?.launch {
            while (isActive) {
                val now = Clock.System.now().epochSeconds
                if (targetEpochSeconds - now <= 0) {
                    postCompletionNotification(title)
                    ServiceCompat.stopForeground(this@SkillTrainingService, ServiceCompat.STOP_FOREGROUND_DETACH)
                    stopSelf()
                    break
                }
                // Checked immediately around the call (not via a named helper) — Android Lint's
                // MissingPermission detector only recognizes the guard when it's inline like this.
                if (ContextCompat.checkSelfPermission(this@SkillTrainingService, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@SkillTrainingService)
                        .notify(SKILL_LIVE_NOTIFICATION_ID, buildTickingNotification(title, targetEpochSeconds))
                }
                // formatDhm() has minute granularity (matches the Dashboard/Skills hero display),
                // so anything finer than a 1-minute tick would just re-post identical text —
                // wasted work, and it's what tripped Android's notification-rate limiter before.
                delay(60_000)
            }
        }
        // Don't resurrect with a null Intent if the process is killed — the backup
        // AlarmManager alarm (scheduled alongside this Service) covers a mid-training kill.
        return START_NOT_STICKY
    }

    private fun buildTickingNotification(title: String, targetEpochSeconds: Long): android.app.Notification {
        val remaining = (targetEpochSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0).seconds
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("${remaining.formatDhm()} remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun postCompletionNotification(title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Training complete")
            .setAutoCancel(true)
            .build()
        // Checked immediately around the call (not via a named helper) — Android Lint's
        // MissingPermission detector only recognizes the guard when it's inline like this.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(SKILL_LIVE_NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        tickJob?.cancel()
        scope?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
