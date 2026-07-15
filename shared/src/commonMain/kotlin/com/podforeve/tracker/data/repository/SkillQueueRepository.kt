@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.SkillQueueEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.platform.NotificationScheduler
import com.podforeve.tracker.platform.NotificationSource
import com.podforeve.tracker.platform.ScheduledCompletion
import com.podforeve.tracker.util.EsiErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate: emit cached rows immediately, then refresh from ESI.
// See wiki: [[Stale-While-Revalidate Cache]], [[Skill Queue]], [[ADR-005 - Math-Based Skill Progress]],
// [[ADR-015 - Unified Completion Notifications]]
class SkillQueueRepository(
    private val esiApi: SkillQueueEsiApi,
    private val db: AppDatabase,
    private val notificationScheduler: NotificationScheduler,
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
                        character_id = characterId,
                        skill_id = dto.skillId.toLong(),
                        skill_name = name,
                        finished_level = dto.finishedLevel.toLong(),
                        start_sp = (dto.levelStartSp ?: 0).toLong(),
                        finish_sp = (dto.levelEndSp ?: 0).toLong(),
                        start_date = dto.startDate?.toEpochSeconds(),
                        finish_date = dto.finishDate?.toEpochSeconds(),
                        cached_at = now,
                    )
                }
            }

            val fresh = db.appDatabaseQueries.getSkillQueue(characterId).executeAsList()
            val freshEntries = fresh.map { it.toDomain() }

            // ESI doesn't immediately rotate a finished entry out of the response, so the actual
            // head can sit behind one or more already-finished rows at queuePosition 0 — mirrors
            // DashboardViewModel's activeSkill selection, not a bare queuePosition == 0 check.
            // See wiki: [[Skill Queue]] business rules.
            val head = freshEntries.firstOrNull { it.isTraining && !it.hasFinished(now) }
            notificationScheduler.reconcile(
                NotificationSource.SKILL,
                listOfNotNull(
                    head?.let {
                        ScheduledCompletion(
                            id = "${NotificationSource.SKILL.idPrefix}head",
                            epochSeconds = it.finishDate!!,
                            title = "${it.skillName} → Level ${it.finishedLevel}",
                            body = "Training complete",
                        )
                    },
                ),
            )

            emit(UiState.Success(freshEntries))
        } catch (e: Exception) {
            // Don't overwrite a successful stale emit with an error.
            if (cached.isEmpty()) {
                emit(UiState.Error(EsiErrorMapper.userMessage(e)))
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
                type_id = typeId.toLong(),
                name = type.name,
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
    characterId = character_id,
    skillId = skill_id.toInt(),
    skillName = skill_name,
    finishedLevel = finished_level.toInt(),
    startSp = start_sp.toInt(),
    finishSp = finish_sp.toInt(),
    startDate = start_date,
    finishDate = finish_date,
)

private fun String.toEpochSeconds(): Long = Instant.parse(this).epochSeconds
