package com.podforeve.tracker.domain.model

// See wiki: [[Industry Job]]
data class IndustryJob(
    val jobId: Int,
    val characterId: Long,
    val activityId: Int,
    val blueprintName: String,
    val runs: Int,
    val startDateEpochSeconds: Long,
    val endDateEpochSeconds: Long,
    val status: String,
) {
    val activityName: String get() = when (activityId) {
        1  -> "Manufacturing"
        3  -> "TE Research"
        4  -> "ME Research"
        5  -> "Copying"
        8  -> "Invention"
        9  -> "Reactions"
        else -> "Activity $activityId"
    }
}
