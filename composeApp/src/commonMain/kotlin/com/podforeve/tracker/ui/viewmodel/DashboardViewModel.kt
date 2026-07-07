package com.podforeve.tracker.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.data.repository.CharacterRepository
import com.podforeve.tracker.data.repository.SkillQueueRepository
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class DashboardData(
    val name: String,
    val portraitUrl: String,
    val iskBalance: Double,
    val activeSkill: SkillQueueEntry?, // null = no skill currently training
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
            val characterId =
                (authRepository.authState.value as? AuthState.Authenticated)?.characterId
                    ?: return@flatMapLatest flowOf(UiState.Error("Not authenticated"))
            combine(
                characterRepository.observeCharacter(characterId),
                skillQueueRepository.observeSkillQueue(characterId),
            ) { charState, queueState ->
                when {
                    charState is UiState.Error -> charState
                    charState is UiState.Success -> {
                        val char = charState.data
                        val now = Clock.System.now().epochSeconds
                        val activeSkill = (queueState as? UiState.Success)
                            ?.data
                            ?.firstOrNull { it.isTraining && !it.hasFinished(now) }
                        UiState.Success(
                            DashboardData(
                                name        = char.name,
                                portraitUrl = char.portraitUrl,
                                iskBalance  = char.iskBalance,
                                activeSkill = activeSkill,
                            )
                        )
                    }
                    else -> UiState.Loading
                }
            }
        }
        .stateIn(
            scope        = screenModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun refresh() = refreshTrigger.update { it + 1 }
}
