package com.podforeve.tracker.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.data.repository.SkillQueueRepository
import com.podforeve.tracker.demo.DemoData
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// ScreenModel (Voyager) for the Skills screen.
// Uses pull-refresh trigger pattern so pull-to-refresh re-runs the full SWR flow.
// See wiki: [[Screen - Skills]], [[Stale-While-Revalidate Cache]]
class SkillQueueViewModel(private val repository: SkillQueueRepository, private val authRepository: AuthRepository) : ScreenModel {

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = refreshTrigger
        .flatMapLatest {
            val characterId = when (val state = authRepository.authState.value) {
                is AuthState.Demo -> return@flatMapLatest flowOf(UiState.Success(DemoData.skillQueue))
                is AuthState.Authenticated -> state.characterId
                else -> return@flatMapLatest flowOf(
                    UiState.Error("Not authenticated — please log in."),
                )
            }
            repository.observeSkillQueue(characterId)
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun refresh() = refreshTrigger.update { it + 1 }
}
