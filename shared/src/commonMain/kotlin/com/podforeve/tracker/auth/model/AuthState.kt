package com.podforeve.tracker.auth.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val characterId: Long) : AuthState()

    // Exploring the app with static sample data — no EVE SSO session, no ESI/DB access.
    // See wiki: [[ADR-022 - Demo Mode]].
    data object Demo : AuthState()
    data class Error(val message: String) : AuthState()
}
