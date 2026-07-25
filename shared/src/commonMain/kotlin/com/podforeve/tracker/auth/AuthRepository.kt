@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.auth

import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.db.AppDatabase
import com.podforeve.tracker.platform.SecureStorage
import com.podforeve.tracker.platform.SecureStorageKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

// Orchestrates the OAuth2 PKCE login flow.
//
// Usage pattern:
//   1. Call startLogin() → open the returned URL in the system browser.
//   2. When the OS delivers the callback URI, call completeLogin(callbackUrl).
//   3. Call getValidAccessToken() before every ESI API call — refreshes automatically.
//
// See wiki: [[OAuth2 PKCE]], [[ADR-008 - OAuth2 PKCE via System Browser]]
class AuthRepository(
    private val pkceHelper: OAuthPkceHelper,
    private val authService: EsiAuthService,
    private val secureStorage: SecureStorage,
    private val db: AppDatabase,
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // In-memory token store; never persisted to disk.
    private var accessToken: String? = null
    private var accessTokenExpiryEpochSeconds: Long = 0L

    // Ephemeral PKCE session state — valid only during a login attempt.
    private var pendingVerifier: String? = null
    private var pendingState: String? = null

    init {
        // Restore session if a refresh token was stored from a previous run.
        val storedRefresh = secureStorage.read(SecureStorageKeys.REFRESH_TOKEN)
        if (storedRefresh != null) {
            // Actual refresh happens lazily on the first getValidAccessToken() call.
            _authState.value = AuthState.Loading
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Step 1: Generate PKCE + state, return the EVE SSO URL to open in the browser.
    fun startLogin(): String {
        val verifier = pkceHelper.generateVerifier()
        val state = pkceHelper.generateState()
        pendingVerifier = verifier
        pendingState = state
        _authState.value = AuthState.Loading
        return pkceHelper.buildAuthUrl(verifier, state)
    }

    // Step 2: Called by the platform layer when eve-tracker://callback arrives.
    suspend fun completeLogin(callbackUrl: String) {
        try {
            val params = parseQueryParams(callbackUrl)
            val code = params["code"] ?: error("Missing code in callback")
            val returnedState = params["state"] ?: error("Missing state in callback")

            check(returnedState == pendingState) { "CSRF state mismatch — possible replay attack" }

            val verifier = pendingVerifier ?: error("No pending PKCE verifier")
            val tokens = authService.exchangeCode(code, verifier)
            storeTokens(tokens)
        } catch (e: Exception) {
            pendingVerifier = null
            pendingState = null
            _authState.value = AuthState.Error(e.message ?: "Login failed")
        }
    }

    // Returns a valid access token, refreshing proactively if within 60 s of expiry.
    suspend fun getValidAccessToken(): String? {
        val now = Clock.System.now().epochSeconds
        if (accessToken != null && now < accessTokenExpiryEpochSeconds - 60) {
            return accessToken
        }
        val refreshToken = secureStorage.read(SecureStorageKeys.REFRESH_TOKEN) ?: run {
            _authState.value = AuthState.Unauthenticated
            return null
        }
        return try {
            val tokens = authService.refreshToken(refreshToken)
            storeTokens(tokens)
            accessToken
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Token refresh failed: ${e.message}")
            null
        }
    }

    // Clears the session AND the local character-data cache, so no ESI data outlives the login
    // that fetched it on this device. skill_type (universe type-id → name) is deliberately left
    // alone: it's shared reference data, not data about the user's own character.
    fun logout() {
        secureStorage.delete(SecureStorageKeys.REFRESH_TOKEN)
        accessToken = null
        accessTokenExpiryEpochSeconds = 0L
        db.transaction {
            db.appDatabaseQueries.clearAllCharacters()
            db.appDatabaseQueries.clearAllSkillQueue()
            db.appDatabaseQueries.clearAllPlanets()
            db.appDatabaseQueries.clearAllIndustryJobs()
        }
        _authState.value = AuthState.Unauthenticated
    }

    // Enters Demo Mode: static sample data, no SSO session, no ESI/DB access at all —
    // see [[ADR-022 - Demo Mode]]. Never touches secureStorage or db, so exitDemo() is
    // a plain state flip, unlike logout()'s cache wipe.
    fun enterDemoMode() {
        _authState.value = AuthState.Demo
    }

    fun exitDemo() {
        _authState.value = AuthState.Unauthenticated
    }

    private fun storeTokens(tokens: com.podforeve.tracker.auth.model.OAuthTokens) {
        secureStorage.write(SecureStorageKeys.REFRESH_TOKEN, tokens.refreshToken)
        accessToken = tokens.accessToken
        accessTokenExpiryEpochSeconds =
            Clock.System.now().epochSeconds + tokens.expiresIn
        val characterId = authService.extractCharacterId(tokens.accessToken)
        pendingVerifier = null
        pendingState = null
        _authState.value = AuthState.Authenticated(characterId)
    }

    private fun parseQueryParams(url: String): Map<String, String> {
        val query = url.substringAfter('?', "")
        return query.split('&').mapNotNull { pair ->
            val eq = pair.indexOf('=')
            if (eq < 0) null else pair.substring(0, eq) to pair.substring(eq + 1)
        }.toMap()
    }
}
