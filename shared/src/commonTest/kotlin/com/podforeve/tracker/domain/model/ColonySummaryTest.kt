package com.podforeve.tracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColonySummaryTest {
    private val now = 1_000_000L

    private fun colony(
        extractorExpiry: Long? = null,
        sfCapacity: Double = 0.0,
        sfUsed: Double = 0.0,
        lpCapacity: Double = 0.0,
        lpUsed: Double = 0.0,
        fetchedSecondsAgo: Long = 0,
    ) = ColonySummary(
        extractorExpiryEpochSeconds = extractorExpiry,
        runningFactories = 0,
        totalFactories = 0,
        sfCapacityM3 = sfCapacity,
        sfUsedM3 = sfUsed,
        lpCapacityM3 = lpCapacity,
        lpUsedM3 = lpUsed,
        dataFetchedAtEpochSeconds = now - fetchedSecondsAgo,
    )

    @Test
    fun fillRatiosAreZeroWhenNoCapacity() {
        val c = colony(sfCapacity = 0.0, sfUsed = 0.0, lpCapacity = 0.0, lpUsed = 0.0)
        assertEquals(0.0, c.sfFillRatio.toDouble(), 1e-6)
        assertEquals(0.0, c.lpFillRatio.toDouble(), 1e-6)
    }

    @Test
    fun fillRatiosComputeUsedOverCapacity() {
        val c = colony(sfCapacity = 12_000.0, sfUsed = 3_000.0, lpCapacity = 10_000.0, lpUsed = 500.0)
        assertEquals(0.25, c.sfFillRatio.toDouble(), 1e-6)
        assertEquals(0.05, c.lpFillRatio.toDouble(), 1e-6)
    }

    @Test
    fun fillRatiosAreClampedToOneEvenIfOverfull() {
        // ESI can transiently report contents above nominal capacity (e.g. right after an upgrade
        // downgrade) — the ratio must never exceed 1.0 or progress bars would overflow.
        val c = colony(sfCapacity = 12_000.0, sfUsed = 20_000.0)
        assertEquals(1.0, c.sfFillRatio.toDouble(), 1e-6)
    }

    @Test
    fun extractorStoppedComparesExpiryAgainstClock() {
        assertTrue(colony(extractorExpiry = now - 1).extractorStopped(now))
        assertTrue(colony(extractorExpiry = now).extractorStopped(now)) // boundary: exactly now counts as stopped
        assertFalse(colony(extractorExpiry = now + 1).extractorStopped(now))
        assertFalse(colony(extractorExpiry = null).extractorStopped(now))
    }

    @Test
    fun hasStorageRequiresEitherFacilityToHaveCapacity() {
        assertFalse(colony(sfCapacity = 0.0, lpCapacity = 0.0).hasStorage())
        assertTrue(colony(sfCapacity = 12_000.0, lpCapacity = 0.0).hasStorage())
        assertTrue(colony(sfCapacity = 0.0, lpCapacity = 10_000.0).hasStorage())
    }

    @Test
    fun dataAgeTextUsesJustNowUnderAMinute() {
        assertEquals("just now", colony(fetchedSecondsAgo = 0).dataAgeText(now))
        assertEquals("just now", colony(fetchedSecondsAgo = 59).dataAgeText(now))
    }

    @Test
    fun dataAgeTextUsesMinutesUnderAnHour() {
        assertEquals("1m ago", colony(fetchedSecondsAgo = 60).dataAgeText(now)) // boundary: exactly 60s
        assertEquals("59m ago", colony(fetchedSecondsAgo = 3_599).dataAgeText(now))
    }

    @Test
    fun dataAgeTextUsesHoursAtOneHourAndAbove() {
        assertEquals("1h ago", colony(fetchedSecondsAgo = 3_600).dataAgeText(now)) // boundary: exactly 1h
        assertEquals("10h ago", colony(fetchedSecondsAgo = 10 * 3_600).dataAgeText(now))
    }
}
