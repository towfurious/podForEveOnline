package com.podforeve.tracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PlanetTest {
    private val now = 1_000_000L

    private fun planetLastUpdated(secondsAgo: Long?) = Planet(
        planetId = 1,
        characterId = 1L,
        planetName = "Test IV",
        planetType = "barren",
        lastUpdateEpochSeconds = secondsAgo?.let { now - it },
        upgradeLevel = 3,
    )

    @Test
    fun statusIsIdleWhenNeverVisited() {
        assertEquals(PlanetStatus.IDLE, planetLastUpdated(null).status(now))
    }

    @Test
    fun statusIsActiveWithinTheFirst24Hours() {
        assertEquals(PlanetStatus.ACTIVE, planetLastUpdated(0).status(now))
        assertEquals(PlanetStatus.ACTIVE, planetLastUpdated(23 * 3_600).status(now)) // exactly 23h
        assertEquals(PlanetStatus.ACTIVE, planetLastUpdated(23 * 3_600 + 3_599).status(now)) // 23h59m59s
    }

    @Test
    fun statusIsNeedsAttentionFrom24UpTo71Hours() {
        assertEquals(PlanetStatus.NEEDS_ATTENTION, planetLastUpdated(24 * 3_600).status(now)) // exactly 24h
        assertEquals(PlanetStatus.NEEDS_ATTENTION, planetLastUpdated(71 * 3_600).status(now)) // exactly 71h
        assertEquals(PlanetStatus.NEEDS_ATTENTION, planetLastUpdated(71 * 3_600 + 3_599).status(now)) // 71h59m59s
    }

    @Test
    fun statusIsIdleAt72HoursAndBeyond() {
        assertEquals(PlanetStatus.IDLE, planetLastUpdated(72 * 3_600).status(now)) // exactly 72h
        assertEquals(PlanetStatus.IDLE, planetLastUpdated(30L * 24 * 3_600).status(now)) // 30 days
    }

    @Test
    fun lastUpdateTextNeverVisited() {
        assertEquals("Never visited", planetLastUpdated(null).lastUpdateText(now))
    }

    @Test
    fun lastUpdateTextUsesMinutesUnderAnHour() {
        assertEquals("5m ago", planetLastUpdated(5 * 60).lastUpdateText(now))
        assertEquals("59m ago", planetLastUpdated(3_599).lastUpdateText(now))
    }

    @Test
    fun lastUpdateTextUsesHoursAtOneHourAndAbove() {
        assertEquals("1h ago", planetLastUpdated(3_600).lastUpdateText(now)) // boundary: exactly 1h
        assertEquals("23h ago", planetLastUpdated(86_399).lastUpdateText(now))
    }

    @Test
    fun lastUpdateTextUsesDaysAtOneDayAndAbove() {
        assertEquals("1d ago", planetLastUpdated(86_400).lastUpdateText(now)) // boundary: exactly 24h
        assertEquals("3d ago", planetLastUpdated(3 * 86_400).lastUpdateText(now))
    }
}
