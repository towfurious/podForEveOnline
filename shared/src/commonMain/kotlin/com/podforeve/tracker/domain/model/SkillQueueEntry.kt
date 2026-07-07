package com.podforeve.tracker.domain.model

// Domain model for one entry in the EVE skill training queue.
// See wiki: [[Skill Queue]], [[ADR-005 - Math-Based Skill Progress]]
data class SkillQueueEntry(
    val queuePosition: Int,
    val characterId: Long,
    val skillId: Int,
    val skillName: String,
    val finishedLevel: Int,
    val startSp: Int,
    val finishSp: Int,
    val startDate: Long?,   // epoch seconds; null when queue is paused
    val finishDate: Long?,  // epoch seconds; null when queue is paused
) {
    val isTraining: Boolean get() = startDate != null && finishDate != null

    // Checks if this entry is already done relative to a given clock.
    fun hasFinished(nowEpochSeconds: Long): Boolean =
        finishDate != null && finishDate <= nowEpochSeconds
}
