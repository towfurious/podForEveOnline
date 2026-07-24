@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.activeSkill
import org.koin.mp.KoinPlatform.getKoin
import kotlin.time.Clock

private const val TAG = "BootCompletedReceiver"

// Android clears ForegroundService state and every AlarmManager alarm on reboot — nothing
// re-arms them without this. Re-derives notification state from whatever's already cached in
// SQLDelight (no network call, no login required): the skill queue's head-of-queue and any
// active industry jobs.
//
// PI extractor notifications are deliberately NOT re-armed here: colony/extractor data is never
// persisted to SQLDelight (see wiki: [[Planet]] — "scheduled directly from the in-memory colony
// snapshot at fetch time"), so there's nothing cached to reconstruct from. Those come back the
// next time the app is opened and PlanetRepository does a real fetch.
//
// Note: Android only delivers BOOT_COMPLETED to apps the user has already launched at least once
// (apps in the "stopped" state right after install are excluded) — standard OS behavior.
// See wiki: [[ADR-020 - Notification Reboot Survival]], [[ADR-015 - Unified Completion Notifications]].
class BootCompletedReceiver : BroadcastReceiver() {
    @Suppress("TooGenericExceptionCaught")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        try {
            val db = getKoin().get<AppDatabase>()
            val scheduler = getKoin().get<NotificationScheduler>()
            val now = Clock.System.now().epochSeconds

            reconcileSkillQueue(db, scheduler, now)
            reconcileIndustryJobs(db, scheduler)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reconcile notifications after boot", e)
        }
    }

    private fun reconcileSkillQueue(db: AppDatabase, scheduler: NotificationScheduler, now: Long) {
        val entries = db.appDatabaseQueries.getAllSkillQueueEntries().executeAsList().map {
            SkillQueueEntry(
                queuePosition = it.queue_position.toInt(),
                characterId = it.character_id,
                skillId = it.skill_id.toInt(),
                skillName = it.skill_name,
                finishedLevel = it.finished_level.toInt(),
                startSp = it.start_sp.toInt(),
                finishSp = it.finish_sp.toInt(),
                startDate = it.start_date,
                finishDate = it.finish_date,
            )
        }
        val head = entries.activeSkill(now)
        scheduler.reconcile(
            NotificationSource.SKILL,
            listOfNotNull(
                head?.let {
                    ScheduledCompletion(
                        id = "${NotificationSource.SKILL.idPrefix}head",
                        epochSeconds = it.finishDate!!,
                        title = "${it.skillName} → Level ${it.finishedLevel}",
                        body = "Training complete",
                    )
                },
            ),
        )
    }

    private fun reconcileIndustryJobs(db: AppDatabase, scheduler: NotificationScheduler) {
        val jobs = db.appDatabaseQueries.getAllActiveIndustryJobs().executeAsList().map {
            IndustryJob(
                jobId = it.job_id.toInt(),
                characterId = it.character_id,
                activityId = it.activity_id.toInt(),
                blueprintName = it.blueprint_name,
                runs = it.runs.toInt(),
                startDateEpochSeconds = it.start_date,
                endDateEpochSeconds = it.end_date,
                status = it.status,
            )
        }
        scheduler.reconcile(
            NotificationSource.INDUSTRY_JOB,
            jobs.map {
                ScheduledCompletion(
                    id = "${NotificationSource.INDUSTRY_JOB.idPrefix}${it.jobId}",
                    epochSeconds = it.endDateEpochSeconds,
                    title = it.blueprintName,
                    body = "${it.activityName} complete",
                )
            },
        )
    }
}
