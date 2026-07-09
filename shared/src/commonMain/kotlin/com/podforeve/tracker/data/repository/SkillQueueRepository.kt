@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.SkillQueueEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate: emit cached rows immediately, then refresh from ESI.
// See wiki: [[Stale-While-Revalidate Cache]], [[Skill Queue]], [[ADR-005 - Math-Based Skill Progress]]
class SkillQueueRepository(
    private val esiApi: SkillQueueEsiApi,
    private val db: AppDatabase,
) {
    fun observeSkillQueue(characterId: Long): Flow<UiState<List<SkillQueueEntry>>> = flow {
        // 1. Serve stale cache immediately.
        val cached = db.appDatabaseQueries.getSkillQueue(characterId).executeAsList()
        if (cached.isNotEmpty()) {
            emit(UiState.Success(cached.map { it.toDomain() }))
        } else {
            emit(UiState.Loading)
        }

        // 2. Refresh from ESI in the same coroutine scope.
        try {
            val dtos = esiApi.fetchSkillQueue(characterId)
            val now = Clock.System.now().epochSeconds

            // Resolve names before the transaction — resolveSkillName is suspend,
            // SQLDelight transaction{} is a plain lambda.
            val names = dtos.associate { it.skillId to resolveSkillName(it.skillId) }

            db.transaction {
                db.appDatabaseQueries.clearSkillQueue(characterId)
                dtos.forEach { dto ->
                    val name = names[dto.skillId] ?: "Skill ${dto.skillId}"
                    db.appDatabaseQueries.upsertSkillQueueEntry(
                        queue_position = dto.queuePosition.toLong(),
                        character_id   = characterId,
                        skill_id       = dto.skillId.toLong(),
                        skill_name     = name,
                        finished_level = dto.finishedLevel.toLong(),
                        start_sp       = (dto.levelStartSp ?: 0).toLong(),
                        finish_sp      = (dto.levelEndSp ?: 0).toLong(),
                        start_date     = dto.startDate?.toEpochSeconds(),
                        finish_date    = dto.finishDate?.toEpochSeconds(),
                        cached_at      = now,
                    )
                }
            }

            val fresh = db.appDatabaseQueries.getSkillQueue(characterId).executeAsList()
            emit(UiState.Success(fresh.map { it.toDomain() }))
        } catch (e: Exception) {
            // Don't overwrite a successful stale emit with an error.
            if (cached.isEmpty()) {
                emit(UiState.Error(e.message ?: "Failed to load skill queue", e))
            }
        }
    }

    // Returns the cached name, or fetches from ESI universe types and caches it.
    private suspend fun resolveSkillName(typeId: Int): String {
        db.appDatabaseQueries.getSkillTypeName(typeId.toLong()).executeAsOneOrNull()
            ?.let { return it }
        return try {
            val type = esiApi.fetchTypeName(typeId)
            db.appDatabaseQueries.upsertSkillType(
                type_id   = typeId.toLong(),
                name      = type.name,
                cached_at = Clock.System.now().epochSeconds,
            )
            type.name
        } catch (_: Exception) {
            "Skill $typeId" // fallback when offline
        }
    }
}

// Extension: map SQLDelight generated row → domain model
private fun com.podforeve.tracker.db.Skill_queue_entry.toDomain() = SkillQueueEntry(
    queuePosition = queue_position.toInt(),
    characterId   = character_id,
    skillId       = skill_id.toInt(),
    skillName     = skill_name,
    finishedLevel = finished_level.toInt(),
    startSp       = start_sp.toInt(),
    finishSp      = finish_sp.toInt(),
    startDate     = start_date,
    finishDate    = finish_date,
)

private fun String.toEpochSeconds(): Long = Instant.parse(this).epochSeconds
