package com.podforeve.tracker.data.remote.esi

import com.podforeve.tracker.data.remote.esi.dto.EsiCharacterPublicInfoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val ESI_BASE   = "https://esi.evetech.net"
private const val IMAGE_BASE = "https://images.evetech.net"

class CharacterEsiApi(
    private val esiClient: HttpClient,    // auth-bearing; used for wallet
    private val publicClient: HttpClient, // no auth; used for public character info
) {
    // GET /v4/characters/{id}/ — public, no auth
    suspend fun fetchPublicInfo(characterId: Long): EsiCharacterPublicInfoDto =
        publicClient.get("$ESI_BASE/v4/characters/$characterId/").body()

    // GET /v1/characters/{id}/wallet/ — requires esi-wallet.read_character_wallet.v1
    suspend fun fetchWalletBalance(characterId: Long): Double =
        esiClient.get("$ESI_BASE/v1/characters/$characterId/wallet/").body()

    fun portraitUrl(characterId: Long, size: Int = 128): String =
        "$IMAGE_BASE/characters/$characterId/portrait?size=$size"
}
