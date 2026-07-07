package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiPlanetDto(
    @SerialName("planet_id")      val planetId: Int,
    @SerialName("planet_type")    val planetType: String,
    @SerialName("solar_system_id") val solarSystemId: Int,
    @SerialName("upgrade_level")  val upgradeLevel: Int,
    @SerialName("num_pins")       val numPins: Int,
    @SerialName("last_update")    val lastUpdate: String, // ISO 8601
)

@Serializable
data class EsiUniversePlanetDto(
    @SerialName("name")      val name: String,
    @SerialName("planet_id") val planetId: Int,
)
