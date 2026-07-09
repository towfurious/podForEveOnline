@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.data.repository

import com.podforeve.tracker.data.remote.esi.CharacterEsiApi
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.domain.model.CharacterInfo
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.model.WalletJournalEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Instant

// Stale-While-Revalidate: emit cached character immediately, then refresh from ESI.
// See wiki: [[Stale-While-Revalidate Cache]], [[Character]]
class CharacterRepository(
    private val esiApi: CharacterEsiApi,
    private val db: AppDatabase,
) {
    fun observeCharacter(characterId: Long): Flow<UiState<CharacterInfo>> = flow {
        // 1. Serve stale cache immediately (new fields default to 0/"").
        val cached = db.appDatabaseQueries.getCharacter(characterId).executeAsOneOrNull()
        if (cached != null) {
            emit(UiState.Success(cached.toDomain()))
        } else {
            emit(UiState.Loading)
        }

        // 2. Refresh from ESI — public info, wallet, and skills in parallel;
        //    corp name waits on the corp id returned by public info.
        try {
            coroutineScope {
                val infoDeferred   = async { esiApi.fetchPublicInfo(characterId) }
                val walletDeferred = async { esiApi.fetchWalletBalance(characterId) }
                val skillsDeferred = async { esiApi.fetchCharacterSkills(characterId) }
                val info   = infoDeferred.await()
                val corpDeferred = async { esiApi.fetchCorporationInfo(info.corporationId) }
                val wallet = walletDeferred.await()
                val skills = skillsDeferred.await()
                val corp   = corpDeferred.await()

                db.appDatabaseQueries.upsertCharacter(
                    character_id = characterId,
                    name         = info.name,
                    portrait_url = esiApi.portraitUrl(characterId),
                    isk_balance  = wallet,
                    cached_at    = Clock.System.now().epochSeconds,
                )
                emit(UiState.Success(CharacterInfo(
                    characterId     = characterId,
                    name            = info.name,
                    portraitUrl     = esiApi.portraitUrl(characterId),
                    iskBalance      = wallet,
                    securityStatus  = info.securityStatus,
                    corporationName = corp.name,
                    totalSp         = skills.totalSp,
                )))
            }
        } catch (e: Exception) {
            if (cached == null) emit(UiState.Error(e.message ?: "Failed to load character"))
        }
    }

    // Never cached — always fetches fresh; emits empty immediately so combine doesn't block.
    fun observeWalletJournal(characterId: Long): Flow<List<WalletJournalEntry>> = flow {
        emit(emptyList())
        try {
            val entries = esiApi.fetchWalletJournal(characterId)
                .sortedByDescending { it.date }
                .take(3)
                .map { dto ->
                    WalletJournalEntry(
                        id               = dto.id,
                        refType          = dto.refType,
                        amount           = dto.amount,
                        dateEpochSeconds = Instant.parse(dto.date).epochSeconds,
                    )
                }
            emit(entries)
        } catch (e: Exception) {
            // keep the empty list already emitted
        }
    }
}

private fun com.podforeve.tracker.db.Character.toDomain() = CharacterInfo(
    characterId = character_id,
    name        = name,
    portraitUrl = portrait_url,
    iskBalance  = isk_balance,
)
