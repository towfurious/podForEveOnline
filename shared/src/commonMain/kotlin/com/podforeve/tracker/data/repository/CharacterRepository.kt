@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.CharacterEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.CharacterInfo
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

// Stale-While-Revalidate: emit cached character + wallet immediately, then refresh from ESI.
// See wiki: [[Stale-While-Revalidate Cache]], [[Character]]
class CharacterRepository(
    private val esiApi: CharacterEsiApi,
    private val db: AppDatabase,
) {
    fun observeCharacter(characterId: Long): Flow<UiState<CharacterInfo>> = flow {
        // 1. Serve stale cache immediately.
        val cached = db.appDatabaseQueries.getCharacter(characterId).executeAsOneOrNull()
        if (cached != null) {
            emit(UiState.Success(cached.toDomain()))
        } else {
            emit(UiState.Loading)
        }

        // 2. Refresh from ESI — public info + wallet in parallel.
        try {
            val (info, wallet) = coroutineScope {
                val infoDeferred   = async { esiApi.fetchPublicInfo(characterId) }
                val walletDeferred = async { esiApi.fetchWalletBalance(characterId) }
                Pair(infoDeferred.await(), walletDeferred.await())
            }
            db.appDatabaseQueries.upsertCharacter(
                character_id = characterId,
                name         = info.name,
                portrait_url = esiApi.portraitUrl(characterId),
                isk_balance  = wallet,
                cached_at    = Clock.System.now().epochSeconds,
            )
            val fresh = db.appDatabaseQueries.getCharacter(characterId).executeAsOneOrNull()
            if (fresh != null) emit(UiState.Success(fresh.toDomain()))
        } catch (e: Exception) {
            if (cached == null) emit(UiState.Error(e.message ?: "Failed to load character"))
        }
    }
}

private fun com.podforeve.tracker.db.Character.toDomain() = CharacterInfo(
    characterId = character_id,
    name        = name,
    portraitUrl = portrait_url,
    iskBalance  = isk_balance,
)
