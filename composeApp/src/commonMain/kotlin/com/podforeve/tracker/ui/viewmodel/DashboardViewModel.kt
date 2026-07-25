@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.data.repository.CharacterRepository
import com.podforeve.tracker.data.repository.SkillQueueRepository
import com.podforeve.tracker.demo.DemoData
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.model.WalletJournalEntry
import com.podforeve.tracker.domain.model.activeSkill
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class DashboardData(
    val name: String,
    val portraitUrl: String,
    val iskBalance: Double,
    val securityStatus: Double,
    val corporationName: String,
    val totalSp: Long,
    val activeSkill: SkillQueueEntry?,
    val walletJournal: List<WalletJournalEntry>,
)

class DashboardViewModel(
    private val characterRepository: CharacterRepository,
    private val skillQueueRepository: SkillQueueRepository,
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = refreshTrigger
        .flatMapLatest {
            val characterId = when (val state = authRepository.authState.value) {
                is AuthState.Demo -> return@flatMapLatest flowOf(UiState.Success(DemoData.dashboard))
                is AuthState.Authenticated -> state.characterId
                else -> return@flatMapLatest flowOf(UiState.Error("Not authenticated"))
            }
            combine(
                characterRepository.observeCharacter(characterId),
                skillQueueRepository.observeSkillQueue(characterId),
                characterRepository.observeWalletJournal(characterId),
            ) { charState, queueState, journal ->
                when {
                    charState is UiState.Error -> charState
                    charState is UiState.Success -> {
                        val char = charState.data
                        val now = Clock.System.now().epochSeconds
                        val active = (queueState as? UiState.Success)?.data?.activeSkill(now)
                        UiState.Success(
                            DashboardData(
                                name = char.name,
                                portraitUrl = char.portraitUrl,
                                iskBalance = char.iskBalance,
                                securityStatus = char.securityStatus,
                                corporationName = char.corporationName,
                                totalSp = char.totalSp,
                                activeSkill = active,
                                walletJournal = journal,
                            ),
                        )
                    }
                    else -> UiState.Loading
                }
            }
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun refresh() = refreshTrigger.update { it + 1 }

    fun logout() {
        screenModelScope.launch { authRepository.logout() }
    }
}
