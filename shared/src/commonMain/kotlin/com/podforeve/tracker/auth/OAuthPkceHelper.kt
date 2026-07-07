package com.podforeve.tracker.auth

import com.podforeve.tracker.platform.secureRandomBytes
import com.podforeve.tracker.platform.sha256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Pure-Kotlin PKCE implementation. See wiki: [[OAuth2 PKCE]]
// Generates code_verifier (32 random bytes → base64url, no padding)
// and code_challenge (base64url(SHA-256(verifier)) per RFC 7636 §4.2).
class OAuthPkceHelper {
    @OptIn(ExperimentalEncodingApi::class)
    fun generateVerifier(): String =
        Base64.UrlSafe.encode(secureRandomBytes(32)).trimEnd('=')

    @OptIn(ExperimentalEncodingApi::class)
    fun deriveChallenge(verifier: String): String =
        Base64.UrlSafe.encode(sha256(verifier.encodeToByteArray())).trimEnd('=')

    fun generateState(): String =
        secureRandomBytes(16).joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }

    fun buildAuthUrl(
        verifier: String,
        state: String,
        clientId: String = EsiConfig.CLIENT_ID,
        redirectUri: String = EsiConfig.REDIRECT_URI,
        scopes: List<String> = EsiConfig.SCOPES,
    ): String {
        val challenge = deriveChallenge(verifier)
        return buildString {
            append(EsiConfig.AUTH_URL)
            append("?response_type=code")
            append("&client_id=").append(clientId)
            append("&redirect_uri=").append(redirectUri.encodeUrl())
            append("&scope=").append(scopes.joinToString(" ").encodeUrl())
            append("&state=").append(state)
            append("&code_challenge=").append(challenge)
            append("&code_challenge_method=S256")
        }
    }

    private fun String.encodeUrl(): String =
        encodeToByteArray().joinToString("") { byte ->
            val c = byte.toInt().and(0xFF).toChar()
            if (c.isLetterOrDigit() || c in "-._~") c.toString()
            else "%${byte.toInt().and(0xFF).toString(16).uppercase().padStart(2, '0')}"
        }
}
