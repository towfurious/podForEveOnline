@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.PlanetEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.util.EsiErrorMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate. Planet names are stable; cached names are reused across refreshes.
// See wiki: [[Stale-While-Revalidate Cache]], [[Planet]]
class PlanetRepository(private val esiApi: PlanetEsiApi, private val db: AppDatabase) {
    fun observePlanets(characterId: Long): Flow<UiState<List<Planet>>> = flow {
        val cached = db.appDatabaseQueries.getPlanets(characterId).executeAsList()
        if (cached.isNotEmpty()) {
            emit(UiState.Success(cached.map { it.toDomain() }))
        } else {
            emit(UiState.Loading)
        }

        try {
            val dtos = esiApi.fetchPlanets(characterId)
            val now = Clock.System.now().epochSeconds

            // Reuse cached names to avoid redundant API calls; only fetch for new planets.
            val cachedNames = cached.associate { it.planet_id to it.planet_name }
            val names: Map<Int, String> = coroutineScope {
                dtos.map { dto ->
                    async {
                        val cached = cachedNames[dto.planetId.toLong()]
                        dto.planetId to (cached ?: esiApi.fetchPlanetName(dto.planetId).name)
                    }
                }.associate { it.await() }
            }

            db.transaction {
                db.appDatabaseQueries.clearPlanets(characterId)
                dtos.forEach { dto ->
                    db.appDatabaseQueries.upsertPlanet(
                        planet_id = dto.planetId.toLong(),
                        character_id = characterId,
                        planet_name = names[dto.planetId] ?: "Planet ${dto.planetId}",
                        planet_type = dto.planetType,
                        last_update = Instant.parse(dto.lastUpdate).epochSeconds,
                        upgrade_level = dto.upgradeLevel.toLong(),
                        cached_at = now,
                    )
                }
            }

            val fresh = db.appDatabaseQueries.getPlanets(characterId).executeAsList()
            emit(UiState.Success(fresh.map { it.toDomain() }))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(UiState.Error(EsiErrorMapper.userMessage(e)))
        }
    }
}

private fun com.podforeve.tracker.db.Planet.toDomain() = Planet(
    planetId = planet_id.toInt(),
    characterId = character_id,
    planetName = planet_name,
    planetType = planet_type,
    lastUpdateEpochSeconds = last_update,
    upgradeLevel = upgrade_level.toInt(),
)
