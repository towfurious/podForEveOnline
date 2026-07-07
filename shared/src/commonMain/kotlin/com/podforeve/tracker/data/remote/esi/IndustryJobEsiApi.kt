package com.podforeve.tracker.data.remote.esi

import com.podforeve.tracker.data.remote.esi.dto.EsiIndustryJobDto
import com.podforeve.tracker.data.remote.esi.dto.EsiUniverseTypeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val ESI_BASE = "https://esi.evetech.net"

class IndustryJobEsiApi(
    private val esiClient: HttpClient,    // auth; for character jobs
    private val publicClient: HttpClient, // no auth; for blueprint type names
) {
    // GET /v1/characters/{id}/industry/jobs/ — requires esi-industry.read_character_jobs.v1
    suspend fun fetchActiveJobs(characterId: Long): List<EsiIndustryJobDto> =
        esiClient.get("$ESI_BASE/v1/characters/$characterId/industry/jobs/?include_completed=false").body()

    // GET /v3/universe/types/{type_id}/ — public; resolves blueprint type_id → name
    suspend fun fetchTypeName(typeId: Int): EsiUniverseTypeDto =
        publicClient.get("$ESI_BASE/v3/universe/types/$typeId/").body()
}
