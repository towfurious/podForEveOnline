package com.podforeve.tracker.auth

import com.podforeve.tracker.auth.model.OAuthTokens
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

// Handles token exchange and refresh against EVE SSO.
// See wiki: [[OAuth2 PKCE]], [[ADR-008 - OAuth2 PKCE via System Browser]]
class EsiAuthService(private val httpClient: HttpClient) {

    // Exchange authorization code + PKCE verifier for tokens.
    // EVE SSO uses Basic auth with client_id + empty password (no secret = public client).
    suspend fun exchangeCode(
        code: String,
        codeVerifier: String,
        clientId: String = EsiConfig.CLIENT_ID,
        redirectUri: String = EsiConfig.REDIRECT_URI,
    ): OAuthTokens = httpClient.submitForm(
        url = EsiConfig.TOKEN_URL,
        formParameters = Parameters.build {
            append("grant_type", "authorization_code")
            append("code", code)
            append("code_verifier", codeVerifier)
            append("redirect_uri", redirectUri)
            append("client_id", clientId)
        },
    ).body()

    // Refresh an expired access token using the stored refresh token.
    suspend fun refreshToken(
        refreshToken: String,
        clientId: String = EsiConfig.CLIENT_ID,
    ): OAuthTokens = httpClient.submitForm(
        url = EsiConfig.TOKEN_URL,
        formParameters = Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
            append("client_id", clientId)
        },
    ).body()

    // Extract character ID from the JWT access token sub claim.
    // EVE sub format: "CHARACTER:EVE:<character_id>"
    fun extractCharacterId(accessToken: String): Long {
        val payload = accessToken.split(".").getOrNull(1) ?: error("Invalid JWT")
        @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
        val json = kotlin.io.encoding.Base64.UrlSafe.decode(
            payload.padEnd((payload.length + 3) / 4 * 4, '=')
        ).decodeToString()
        val sub = Regex(""""sub"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)
            ?: error("Missing sub claim in JWT")
        return sub.split(":").last().toLong()
    }
}
