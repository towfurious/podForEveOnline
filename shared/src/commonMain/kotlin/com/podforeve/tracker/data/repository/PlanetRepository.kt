@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.PlanetEsiApi
import com.podforeve.tracker.data.remote.esi.dto.EsiColonyDto
import com.podforeve.tracker.data.remote.esi.dto.EsiColonyPinDto
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.ColonySummary
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.usecase.PiVolumeTable
import com.podforeve.tracker.platform.NotificationScheduler
import com.podforeve.tracker.platform.NotificationSource
import com.podforeve.tracker.platform.ScheduledCompletion
import com.podforeve.tracker.util.EsiErrorMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate. Planet metadata (name, type, level) is cached in DB and reused
// across refreshes. Colony details (extractors, factories, storage) are always fresh from ESI
// and never persisted — they change every cycle.
// See wiki: [[Stale-While-Revalidate Cache]], [[Planet]], [[ADR-015 - Unified Completion Notifications]]
class PlanetRepository(
    private val esiApi: PlanetEsiApi,
    private val db: AppDatabase,
    private val notificationScheduler: NotificationScheduler,
) {
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

            val cachedNames = cached.associate { it.planet_id to it.planet_name }

            // Resolve names and colony details in parallel across all planets.
            val (names, colonies) = coroutineScope {
                val nameJobs = dtos.map { dto ->
                    async {
                        dto.planetId to (
                            cachedNames[dto.planetId.toLong()]
                                ?: esiApi.fetchPlanetName(dto.planetId).name
                            )
                    }
                }
                val colonyJobs = dtos.map { dto ->
                    async {
                        dto.planetId to runCatching {
                            val body = esiApi.fetchColony(characterId, dto.planetId)
                            val fetchedAt = Clock.System.now().epochSeconds
                            body.toColonySummary(fetchedAt)
                        }.getOrNull()
                    }
                }
                nameJobs.associate { it.await() } to colonyJobs.associate { it.await() }
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
            val merged = fresh.map { row ->
                row.toDomain().copy(colony = colonies[row.planet_id.toInt()])
            }

            notificationScheduler.reconcile(
                NotificationSource.PI_EXTRACTOR,
                merged.mapNotNull { planet ->
                    val expiry = planet.colony?.extractorExpiryEpochSeconds ?: return@mapNotNull null
                    ScheduledCompletion(
                        id = "${NotificationSource.PI_EXTRACTOR.idPrefix}${planet.planetId}",
                        epochSeconds = expiry,
                        title = planet.planetName,
                        body = "Extractor depleted",
                    )
                },
            )

            emit(UiState.Success(merged))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(UiState.Error(EsiErrorMapper.userMessage(e)))
        }
    }
}

private fun EsiColonyDto.toColonySummary(fetchedAt: Long): ColonySummary {
    val extractorExpiry = pins
        .filter { it.extractorDetails != null }
        .mapNotNull { it.expiryTime?.let { t -> Instant.parse(t).epochSeconds } }
        .minOrNull()

    // schematic_id is present on ALL factory pins regardless of planet type —
    // the correct detector. Type IDs vary per planet type and are not reliable.
    val totalFactories = pins.count { it.schematicId != null }

    val sfPins = pins.filter { it.typeId in PiVolumeTable.storageFacilityTypeIds }
    val lpPins = pins.filter { it.typeId in PiVolumeTable.launchpadTypeIds }

    fun contentsM3(pinList: List<EsiColonyPinDto>) = pinList.sumOf { pin ->
        pin.contents.orEmpty().sumOf { c -> c.amount * PiVolumeTable.volumeOf(c.typeId) }
    }

    return ColonySummary(
        extractorExpiryEpochSeconds = extractorExpiry,
        runningFactories = totalFactories,
        totalFactories = totalFactories,
        sfCapacityM3 = sfPins.sumOf { PiVolumeTable.capacityOf(it.typeId) },
        sfUsedM3 = contentsM3(sfPins),
        lpCapacityM3 = lpPins.sumOf { PiVolumeTable.capacityOf(it.typeId) },
        lpUsedM3 = contentsM3(lpPins),
        dataFetchedAtEpochSeconds = fetchedAt,
    )
}

private fun com.podforeve.tracker.db.Planet.toDomain() = Planet(
    planetId = planet_id.toInt(),
    characterId = character_id,
    planetName = planet_name,
    planetType = planet_type,
    lastUpdateEpochSeconds = last_update,
    upgradeLevel = upgrade_level.toInt(),
)
