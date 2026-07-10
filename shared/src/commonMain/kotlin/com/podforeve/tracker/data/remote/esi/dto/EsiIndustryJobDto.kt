package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiIndustryJobDto(
    @SerialName("job_id") val jobId: Int,
    @SerialName("activity_id") val activityId: Int,
    @SerialName("blueprint_type_id") val blueprintTypeId: Int,
    @SerialName("runs") val runs: Int,
    @SerialName("start_date") val startDate: String, // ISO 8601
    @SerialName("end_date") val endDate: String, // ISO 8601
    @SerialName("status") val status: String,
)
