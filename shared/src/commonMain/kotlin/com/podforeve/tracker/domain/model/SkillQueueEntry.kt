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
    val startDate: Long?, // epoch seconds; null when queue is paused
    val finishDate: Long?, // epoch seconds; null when queue is paused
) {
    val isTraining: Boolean get() = startDate != null && finishDate != null

    // Checks if this entry is already done relative to a given clock.
    fun hasFinished(nowEpochSeconds: Long): Boolean = finishDate != null && finishDate <= nowEpochSeconds
}

// The skill actually being trained right now. NOT queuePosition == 0 — ESI doesn't immediately
// rotate a finished entry out of position 0, so the real head can sit one or more slots behind
// it. See wiki: [[Skill Queue]] business rules.
fun List<SkillQueueEntry>.activeSkill(nowEpochSeconds: Long): SkillQueueEntry? =
    firstOrNull { it.isTraining && !it.hasFinished(nowEpochSeconds) }
