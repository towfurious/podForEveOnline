package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiCharacterPublicInfoDto(
    @SerialName("name")            val name: String,
    @SerialName("corporation_id")  val corporationId: Int,
    @SerialName("security_status") val securityStatus: Double = 0.0,
)
