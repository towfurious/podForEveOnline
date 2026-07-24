package com.podforeve.tracker.util

import com.podforeve.tracker.data.remote.http.EsiErrorBudgetExhaustedException

object EsiErrorMapper {
    fun userMessage(e: Exception): String = when {
        isRateLimited(e) -> "ESI is rate-limited right now. Try again shortly."
        isAuthError(e) -> "Session expired. Please log in again."
        isTimeout(e) -> "Connection timed out. Check your connection."
        isServerError(e) -> "EVE servers are temporarily unavailable."
        isNetworkError(e) -> "No internet connection."
        else -> "Something went wrong. Please try again."
    }

    fun isRateLimited(e: Exception): Boolean = e is EsiErrorBudgetExhaustedException

    fun isAuthError(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: return false
        return msg.contains("401") ||
            msg.contains("unauthorized") ||
            msg.contains("403") ||
            msg.contains("forbidden")
    }

    private fun isTimeout(e: Exception): Boolean = e::class.simpleName?.contains("Timeout", ignoreCase = true) == true ||
        e.message?.contains("timeout", ignoreCase = true) == true

    private fun isServerError(e: Exception): Boolean {
        val msg = e.message ?: return false
        return msg.contains("500") ||
            msg.contains("502") ||
            msg.contains("503") ||
            msg.contains("504")
    }

    private fun isNetworkError(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: return false
        return msg.contains("failed to connect") ||
            msg.contains("unable to resolve") ||
            msg.contains("no address") ||
            msg.contains("network") ||
            msg.contains("unreachable") ||
            msg.contains("hostname") ||
            msg.contains("socket") ||
            msg.contains("connection refused") ||
            msg.contains("eof")
    }
}
