@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.IndustryJobEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.util.EsiErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate. Blueprint names cached in skill_type table (generic type name cache).
// See wiki: [[Stale-While-Revalidate Cache]], [[Industry Job]]
class IndustryJobRepository(
    private val esiApi: IndustryJobEsiApi,
    private val db: AppDatabase,
) {
    fun observeJobs(characterId: Long): Flow<UiState<List<IndustryJob>>> = flow {
        val cached = db.appDatabaseQueries.getActiveIndustryJobs(characterId).executeAsList()
        if (cached.isNotEmpty()) {
            emit(UiState.Success(cached.map { it.toDomain() }))
        } else {
            emit(UiState.Loading)
        }

        try {
            val dtos = esiApi.fetchActiveJobs(characterId)
            val now = Clock.System.now().epochSeconds

            // Resolve blueprint names before transaction (suspend not allowed inside transaction).
            val names = dtos.associate { it.blueprintTypeId to resolveTypeName(it.blueprintTypeId) }

            db.transaction {
                db.appDatabaseQueries.clearActiveIndustryJobs(characterId)
                dtos.forEach { dto ->
                    db.appDatabaseQueries.upsertIndustryJob(
                        job_id         = dto.jobId.toLong(),
                        character_id   = characterId,
                        activity_id    = dto.activityId.toLong(),
                        blueprint_name = names[dto.blueprintTypeId] ?: "Blueprint ${dto.blueprintTypeId}",
                        runs           = dto.runs.toLong(),
                        start_date     = Instant.parse(dto.startDate).epochSeconds,
                        end_date       = Instant.parse(dto.endDate).epochSeconds,
                        status         = dto.status,
                        cached_at      = now,
                    )
                }
            }

            val fresh = db.appDatabaseQueries.getActiveIndustryJobs(characterId).executeAsList()
            emit(UiState.Success(fresh.map { it.toDomain() }))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(UiState.Error(EsiErrorMapper.userMessage(e)))
        }
    }

    private suspend fun resolveTypeName(typeId: Int): String {
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
            "Blueprint $typeId"
        }
    }
}

private fun com.podforeve.tracker.db.Industry_job.toDomain() = IndustryJob(
    jobId                  = job_id.toInt(),
    characterId            = character_id,
    activityId             = activity_id.toInt(),
    blueprintName          = blueprint_name,
    runs                   = runs.toInt(),
    startDateEpochSeconds  = start_date,
    endDateEpochSeconds    = end_date,
    status                 = status,
)
