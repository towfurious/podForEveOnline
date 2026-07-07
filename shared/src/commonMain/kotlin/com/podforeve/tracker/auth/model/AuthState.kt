package com.podforeve.tracker.auth.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val characterId: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}
