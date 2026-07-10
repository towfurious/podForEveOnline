package com.podforeve.tracker.data.remote.esi.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EsiCharacterSkillsDto(@SerialName("total_sp") val totalSp: Long)
