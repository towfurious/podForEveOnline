package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Minimal shape from GET /v3/universe/types/{type_id}/ — public endpoint, no auth.
@Serializable
data class EsiUniverseTypeDto(@SerialName("type_id") val typeId: Int, @SerialName("name") val name: String)
