@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.platform.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
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
                NotificationManagerCompat.from(this@SkillTrainingService)
                    .notify(SKILL_LIVE_NOTIFICATION_ID, buildTickingNotification(title, targetEpochSeconds))
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

    private fun postCompletionNotification(title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("Training complete")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(SKILL_LIVE_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        tickJob?.cancel()
        scope?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
