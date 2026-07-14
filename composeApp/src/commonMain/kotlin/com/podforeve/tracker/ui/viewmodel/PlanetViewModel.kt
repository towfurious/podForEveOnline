package com.podforeve.tracker.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.data.repository.PlanetRepository
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PlanetViewModel(private val repository: PlanetRepository, private val authRepository: AuthRepository) : ScreenModel {

    private val refreshTrigger = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = refreshTrigger
        .flatMapLatest { loadPlanets() }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )

    fun refresh() {
        _isRefreshing.value = true
        refreshTrigger.update { it + 1 }
    }

    private fun loadPlanets(): Flow<UiState<List<Planet>>> {
        val characterId = (authRepository.authState.value as? AuthState.Authenticated)?.characterId
        val source: Flow<UiState<List<Planet>>> = if (characterId != null)
            repository.observePlanets(characterId)
        else
            flowOf(UiState.Error("Not authenticated"))
        return source.onCompletion { cause -> if (cause == null) _isRefreshing.value = false }
    }
}
