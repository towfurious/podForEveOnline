package com.podforeve.tracker.auth

// EVE SSO constants.
// CLIENT_ID is injected at build time from local.properties (Android) / xcconfig (iOS).
// See wiki: [[OAuth2 PKCE]], [[ADR-008 - OAuth2 PKCE via System Browser]], [[ESI Scopes MVP]]
object EsiConfig {
    val CLIENT_ID: String get() = esiClientId()
    const val REDIRECT_URI = "eveauth-podforeve://callback"
    const val AUTH_URL = "https://login.eveonline.com/v2/oauth/authorize"
    const val TOKEN_URL = "https://login.eveonline.com/v2/oauth/token"

    val SCOPES = listOf(
        "publicData",
        "esi-skills.read_skills.v1",
        "esi-skills.read_skillqueue.v1",
        "esi-wallet.read_character_wallet.v1",
        "esi-planets.manage_planets.v1",
        "esi-industry.read_character_jobs.v1",
    )
}

internal expect fun esiClientId(): String
