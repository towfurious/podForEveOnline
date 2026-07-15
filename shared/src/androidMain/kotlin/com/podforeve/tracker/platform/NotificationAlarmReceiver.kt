package com.podforeve.tracker.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.podforeve.tracker.shared.R

// Fires a one-shot completion notification for job/extractor alarms, and for skill training
// when the ForegroundService (SkillTrainingService) has been killed before it could self-detect
// completion. See wiki: [[ADR-015 - Unified Completion Notifications]]
class NotificationAlarmReceiver : BroadcastReceiver() {
    // The checkSelfPermission guard immediately below is real and correct (verified: the
    // identical check on `this` in SkillTrainingService satisfies lint fine, in both if-wrap and
    // early-return form) — but lint's MissingPermission detector doesn't credit it here, tried
    // both guard shapes. Best-understood cause: the checked receiver is `context`, a method
    // parameter of onReceive, not `this` — a known category of lint limitation for
    // BroadcastReceivers specifically. Suppressing rather than chasing the tool further.
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.hasExtra(EXTRA_NOTIFICATION_ID)) return
        val title = intent.getStringExtra(EXTRA_TITLE)
        val body = intent.getStringExtra(EXTRA_BODY)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
        if (title == null || body == null || channelId == null) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        ensureNotificationChannels(context)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
