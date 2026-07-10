package com.podforeve.tracker.data.remote.esi

import com.podforeve.tracker.data.remote.esi.dto.EsiSkillQueueEntryDto
import com.podforeve.tracker.data.remote.esi.dto.EsiUniverseTypeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val ESI_BASE = "https://esi.evetech.net"

class SkillQueueEsiApi(
    private val esiClient: HttpClient, // auth-bearing client
    private val publicClient: HttpClient, // no auth; used for public universe endpoints
) {
    // GET /v2/characters/{character_id}/skillqueue/ — requires esi-skills.read_skillqueue.v1
    suspend fun fetchSkillQueue(characterId: Long): List<EsiSkillQueueEntryDto> =
        esiClient.get("$ESI_BASE/v2/characters/$characterId/skillqueue/").body()

    // GET /v3/universe/types/{type_id}/ — public, no auth
    suspend fun fetchTypeName(typeId: Int): EsiUniverseTypeDto = publicClient.get("$ESI_BASE/v3/universe/types/$typeId/").body()
}
