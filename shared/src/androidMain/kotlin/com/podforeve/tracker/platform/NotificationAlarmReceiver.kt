package com.podforeve.tracker.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.podforeve.tracker.shared.R

// Fires a one-shot completion notification for job/extractor alarms, and for skill training
// when the ForegroundService (SkillTrainingService) has been killed before it could self-detect
// completion. See wiki: [[ADR-015 - Unified Completion Notifications]]
class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.hasExtra(EXTRA_NOTIFICATION_ID)) return
        ensureNotificationChannels(context)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY) ?: return
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
