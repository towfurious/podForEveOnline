package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiColonyDto(val pins: List<EsiColonyPinDto>)

@Serializable
data class EsiColonyPinDto(
    @SerialName("pin_id") val pinId: Long,
    @SerialName("type_id") val typeId: Int,
    @SerialName("expiry_time") val expiryTime: String? = null,
    @SerialName("extractor_details") val extractorDetails: EsiExtractorDetailsDto? = null,
    @SerialName("schematic_id") val schematicId: Int? = null,
    @SerialName("contents") val contents: List<EsiPinContentDto>? = null,
)

@Serializable
data class EsiExtractorDetailsDto(@SerialName("cycle_time") val cycleTime: Int, @SerialName("product_type_id") val productTypeId: Int)

@Serializable
data class EsiPinContentDto(@SerialName("type_id") val typeId: Int, @SerialName("amount") val amount: Long)
