package com.podforeve.tracker.domain.model

// See wiki: [[Planet]]
enum class PlanetStatus { ACTIVE, NEEDS_ATTENTION, IDLE }

data class Planet(
    val planetId: Int,
    val characterId: Long,
    val planetName: String,
    val planetType: String, // "temperate" | "barren" | "lava" | "oceanic" | "ice" | "storm" | "plasma" | "gas"
    val lastUpdateEpochSeconds: Long?,
    val upgradeLevel: Int,
    val colony: ColonySummary? = null,
) {
    // Status based on how long ago the planet was last visited in-game.
    fun status(nowEpochSeconds: Long): PlanetStatus {
        val lu = lastUpdateEpochSeconds ?: return PlanetStatus.IDLE
        return when ((nowEpochSeconds - lu) / 3600) {
            in 0..23 -> PlanetStatus.ACTIVE
            in 24..71 -> PlanetStatus.NEEDS_ATTENTION
            else -> PlanetStatus.IDLE
        }
    }

    fun lastUpdateText(nowEpochSeconds: Long): String {
        val lu = lastUpdateEpochSeconds ?: return "Never visited"
        val age = nowEpochSeconds - lu
        return when {
            age < 3_600 -> "${age / 60}m ago"
            age < 86_400 -> "${age / 3_600}h ago"
            else -> "${age / 86_400}d ago"
        }
    }
}
