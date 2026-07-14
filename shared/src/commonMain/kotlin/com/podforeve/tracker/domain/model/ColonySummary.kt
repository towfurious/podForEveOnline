package com.podforeve.tracker.domain.model

// Per-planet colony state fetched from GET /v5/characters/{id}/planets/{planet_id}/.
// Not cached to DB — always fresh from ESI.
data class ColonySummary(
    val extractorExpiryEpochSeconds: Long?,
    val runningFactories: Int,
    val totalFactories: Int,
    // Storage Facility — P0 raw buffer, 12,000 m³ per unit. 0 if planet has none.
    val sfCapacityM3: Double,
    val sfUsedM3: Double,
    // Launchpad — finished goods waiting for pickup, 10,000 m³ per unit. 0 if planet has none.
    val lpCapacityM3: Double,
    val lpUsedM3: Double,
    // Wall-clock time when this colony snapshot was received from ESI.
    val dataFetchedAtEpochSeconds: Long,
) {
    val sfFillRatio: Float
        get() = if (sfCapacityM3 > 0) (sfUsedM3 / sfCapacityM3).toFloat().coerceIn(0f, 1f) else 0f
    val lpFillRatio: Float
        get() = if (lpCapacityM3 > 0) (lpUsedM3 / lpCapacityM3).toFloat().coerceIn(0f, 1f) else 0f

    fun extractorStopped(nowEpochSeconds: Long): Boolean =
        extractorExpiryEpochSeconds != null && extractorExpiryEpochSeconds <= nowEpochSeconds

    fun hasStorage(): Boolean = sfCapacityM3 > 0 || lpCapacityM3 > 0

    fun dataAgeText(nowEpochSeconds: Long): String {
        val age = nowEpochSeconds - dataFetchedAtEpochSeconds
        return when {
            age < 60 -> "just now"
            age < 3_600 -> "${age / 60}m ago"
            else -> "${age / 3_600}h ago"
        }
    }
}
