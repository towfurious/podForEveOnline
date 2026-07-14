package com.podforeve.tracker.data.remote.esi

import com.podforeve.tracker.data.remote.esi.dto.EsiColonyDto
import com.podforeve.tracker.data.remote.esi.dto.EsiPlanetDto
import com.podforeve.tracker.data.remote.esi.dto.EsiUniversePlanetDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val ESI_BASE = "https://esi.evetech.net"

class PlanetEsiApi(
    private val esiClient: HttpClient,
    private val publicClient: HttpClient,
) {
    // GET /v3/characters/{id}/planets/ — requires esi-planets.manage_planets.v1
    suspend fun fetchPlanets(characterId: Long): List<EsiPlanetDto> =
        esiClient.get("$ESI_BASE/v3/characters/$characterId/planets/").body()

    // GET /v5/characters/{id}/planets/{planet_id}/ — requires esi-planets.manage_planets.v1
    suspend fun fetchColony(characterId: Long, planetId: Int): EsiColonyDto =
        esiClient.get("$ESI_BASE/v5/characters/$characterId/planets/$planetId/").body()

    // GET /v4/universe/planets/{planet_id}/ — public
    suspend fun fetchPlanetName(planetId: Int): EsiUniversePlanetDto =
        publicClient.get("$ESI_BASE/v4/universe/planets/$planetId/").body()
}
