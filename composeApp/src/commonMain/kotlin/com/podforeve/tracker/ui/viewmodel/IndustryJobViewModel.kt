package com.podforeve.tracker.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.data.repository.IndustryJobRepository
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class IndustryJobViewModel(private val repository: IndustryJobRepository, private val authRepository: AuthRepository) : ScreenModel {

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = refreshTrigger
        .flatMapLatest {
            val characterId =
                (authRepository.authState.value as? AuthState.Authenticated)?.characterId
                    ?: return@flatMapLatest flowOf(UiState.Error("Not authenticated"))
            repository.observeJobs(characterId)
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun refresh() = refreshTrigger.update { it + 1 }
}
