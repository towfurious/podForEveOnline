package com.podforeve.tracker.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// See spec: OAuthPkceTest in [[Source - 2026-04-24 - EVE Online KMP Design Spec]]
class OAuthPkceTest {
    private val helper = OAuthPkceHelper()

    @Test
    fun verifierIsBase64UrlNoPadding() {
        val verifier = helper.generateVerifier()
        assertTrue(verifier.length >= 43, "RFC 7636 §4.1: verifier must be ≥43 chars, got ${verifier.length}")
        assertTrue(verifier.none { it == '=' }, "Verifier must not contain padding")
        assertTrue(verifier.all { it.isLetterOrDigit() || it in "-_" }, "Verifier must be base64url chars")
    }

    @Test
    fun challengeIsBase64UrlSha256OfVerifier() {
        val verifier = helper.generateVerifier()
        val challenge = helper.deriveChallenge(verifier)
        // SHA-256 of 32 bytes → 32 bytes → base64url no-pad → 43 chars
        assertEquals(43, challenge.length, "SHA-256 base64url no-pad must be 43 chars")
        assertTrue(challenge.none { it == '=' }, "Challenge must not contain padding")
    }

    @Test
    fun eachVerifierIsUnique() {
        val v1 = helper.generateVerifier()
        val v2 = helper.generateVerifier()
        assertNotEquals(v1, v2, "Verifiers must be randomly unique")
    }

    @Test
    fun stateMustBeHexAndUnique() {
        val s1 = helper.generateState()
        val s2 = helper.generateState()
        assertTrue(s1.all { it.isDigit() || it in 'a'..'f' }, "State must be lowercase hex")
        assertEquals(32, s1.length, "16 bytes → 32 hex chars")
        assertNotEquals(s1, s2, "States must be randomly unique")
    }

    @Test
    fun authUrlContainsRequiredParams() {
        val verifier = helper.generateVerifier()
        val state = helper.generateState()
        val url = helper.buildAuthUrl(verifier, state, clientId = "test_client")
        assertTrue(url.contains("response_type=code"))
        assertTrue(url.contains("client_id=test_client"))
        assertTrue(url.contains("code_challenge_method=S256"))
        assertTrue(url.contains("state=$state"))
        assertTrue(url.contains("code_challenge="))
        assertTrue(url.contains("redirect_uri="))
    }
}
