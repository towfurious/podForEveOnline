package com.podforeve.tracker.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// isTimeout/isServerError/isNetworkError are private, so they're only reachable (and only need
// to be correct) through userMessage()'s end-to-end behavior — matches how the repositories that
// actually call this mapper only ever see the final message, never the individual predicates.
class EsiErrorMapperTest {

    @Test
    fun authErrorsAskToLogInAgain() {
        assertEquals("Session expired. Please log in again.", EsiErrorMapper.userMessage(Exception("HTTP 401")))
        assertEquals("Session expired. Please log in again.", EsiErrorMapper.userMessage(Exception("Unauthorized")))
        assertEquals("Session expired. Please log in again.", EsiErrorMapper.userMessage(Exception("403 Forbidden")))
    }

    @Test
    fun isAuthErrorIsCaseInsensitiveAndNullSafe() {
        assertTrue(EsiErrorMapper.isAuthError(Exception("UNAUTHORIZED")))
        assertTrue(EsiErrorMapper.isAuthError(Exception("forbidden")))
        assertFalse(EsiErrorMapper.isAuthError(Exception("500 Internal Server Error")))
        assertFalse(EsiErrorMapper.isAuthError(Exception())) // no message at all
    }

    @Test
    fun timeoutsAreDetectedByMessageContent() {
        assertEquals(
            "Connection timed out. Check your connection.",
            EsiErrorMapper.userMessage(Exception("Request timeout after 30000ms")),
        )
    }

    @Test
    fun timeoutsAreDetectedByExceptionClassNameEvenWithAnUnrelatedMessage() {
        assertEquals(
            "Connection timed out. Check your connection.",
            EsiErrorMapper.userMessage(FakeHttpRequestTimeoutException("deadline exceeded")),
        )
    }

    @Test
    fun serverErrorsCoverAllFiveHundredsRangeCodesChecked() {
        assertEquals("EVE servers are temporarily unavailable.", EsiErrorMapper.userMessage(Exception("HTTP 500")))
        assertEquals("EVE servers are temporarily unavailable.", EsiErrorMapper.userMessage(Exception("502 Bad Gateway")))
        assertEquals("EVE servers are temporarily unavailable.", EsiErrorMapper.userMessage(Exception("503")))
        assertEquals("EVE servers are temporarily unavailable.", EsiErrorMapper.userMessage(Exception("504")))
    }

    @Test
    fun networkErrorsCoverCommonConnectivityFailureMessages() {
        assertEquals("No internet connection.", EsiErrorMapper.userMessage(Exception("Unable to resolve host")))
        assertEquals("No internet connection.", EsiErrorMapper.userMessage(Exception("Failed to connect to esi.evetech.net")))
        assertEquals(
            "No internet connection.",
            EsiErrorMapper.userMessage(Exception("Software caused connection abort: socket write error")),
        )
        assertEquals("No internet connection.", EsiErrorMapper.userMessage(Exception("Network is unreachable")))
    }

    @Test
    fun unrecognizedFailuresFallBackToAGenericMessage() {
        assertEquals("Something went wrong. Please try again.", EsiErrorMapper.userMessage(Exception("Unexpected JSON token")))
        assertEquals("Something went wrong. Please try again.", EsiErrorMapper.userMessage(Exception())) // no message at all
    }

    @Test
    fun authErrorTakesPrecedenceOverOtherCategoriesWhenMessageMatchesMultiple() {
        // A message that could plausibly match both "auth" and "network" categories must resolve
        // to auth — userMessage() checks in that priority order, and a maintainer adding a new
        // category above it could silently break that ordering without a test catching it.
        assertEquals(
            "Session expired. Please log in again.",
            EsiErrorMapper.userMessage(Exception("401 Unauthorized: network policy denied")),
        )
    }
}

private class FakeHttpRequestTimeoutException(message: String) : Exception(message)
