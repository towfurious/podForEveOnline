package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiWalletJournalEntryDto(
    @SerialName("id") val id: Long,
    @SerialName("date") val date: String,
    @SerialName("ref_type") val refType: String,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("description") val description: String = "",
)
