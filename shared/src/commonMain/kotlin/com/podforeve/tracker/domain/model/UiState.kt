package com.podforeve.tracker.domain.model

// Shared UI state contract used by all screens.
// See wiki: [[UiState]], [[ADR-009 - UiState Sealed Class with Shimmer]]
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
}
