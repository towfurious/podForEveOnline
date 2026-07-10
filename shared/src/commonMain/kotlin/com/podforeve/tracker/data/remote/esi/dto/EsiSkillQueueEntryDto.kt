package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ESI response shape for GET /v2/characters/{character_id}/skillqueue/
// Dates and SP fields are absent when the queue is paused or the entry has no active window.
@Serializable
data class EsiSkillQueueEntryDto(
    @SerialName("queue_position") val queuePosition: Int,
    @SerialName("skill_id") val skillId: Int,
    @SerialName("finished_level") val finishedLevel: Int,
    @SerialName("level_start_sp") val levelStartSp: Int? = null, // SP at start of this level
    @SerialName("level_end_sp") val levelEndSp: Int? = null, // SP needed to finish this level
    @SerialName("training_start_sp") val trainingStartSp: Int? = null, // SP when training session began
    @SerialName("start_date") val startDate: String? = null, // ISO 8601 UTC
    @SerialName("finish_date") val finishDate: String? = null, // ISO 8601 UTC
)
