package com.podforeve.tracker.domain.model

data class CharacterInfo(
    val characterId: Long,
    val name: String,
    val portraitUrl: String,
    val iskBalance: Double,
    val securityStatus: Double = 0.0,
    val corporationName: String = "",
    val totalSp: Long = 0L,
)
