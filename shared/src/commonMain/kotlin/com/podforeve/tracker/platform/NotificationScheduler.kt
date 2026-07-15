package com.podforeve.tracker.platform

// See wiki: [[ADR-015 - Unified Completion Notifications]]
enum class NotificationSource(val idPrefix: String, val channelId: String, val channelLabel: String) {
    SKILL("skill_", "skill_training", "Skill Training"),
    INDUSTRY_JOB("job_", "industry_jobs", "Industry Jobs"),
    PI_EXTRACTOR("extractor_", "pi_extractors", "PI Extractors"),
}

data class ScheduledCompletion(val id: String, val epochSeconds: Long, val title: String, val body: String)

// Cancels everything previously scheduled for `source` and reschedules fresh from `items`
// (past-due items are dropped internally). Cancel-all-then-reschedule-all is fine at this
// scale — one skill, a handful of jobs/planets — no incremental diffing needed.
expect class NotificationScheduler {
    fun reconcile(source: NotificationSource, items: List<ScheduledCompletion>)
}
