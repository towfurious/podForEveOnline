package com.podforeve.tracker.domain

import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatHms
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

// See spec: SkillProgressCalculatorTest in [[Source - 2026-04-24 - EVE Online KMP Design Spec]]
class SkillProgressCalculatorTest {
    private val start = Instant.parse("2026-01-01T00:00:00Z")
    private val end = Instant.parse("2026-01-01T01:00:00Z") // 1 hour later

    @Test
    fun progressIsZeroAtStart() {
        val snap = calcAt("2026-01-01T00:00:00Z").snapshot(start, end)
        assertEquals(0.0, snap.progress, 1e-6)
        assertEquals(1.hours, snap.remaining)
    }

    @Test
    fun progressIsHalfAtMidpoint() {
        val snap = calcAt("2026-01-01T00:30:00Z").snapshot(start, end)
        assertEquals(0.5, snap.progress, 1e-6)
        assertEquals(30.minutes, snap.remaining)
    }

    @Test
    fun progressIsOneAtEnd() {
        val snap = calcAt("2026-01-01T01:00:00Z").snapshot(start, end)
        assertEquals(1.0, snap.progress, 1e-6)
        assertEquals(0.seconds, snap.remaining)
    }

    @Test
    fun progressClampedAtZeroBeforeStart() {
        val snap = calcAt("2025-12-31T23:00:00Z").snapshot(start, end)
        assertEquals(0.0, snap.progress, 1e-6)
    }

    @Test
    fun progressClampedAtOneAfterEnd() {
        val snap = calcAt("2026-01-01T02:00:00Z").snapshot(start, end)
        assertEquals(1.0, snap.progress, 1e-6)
        assertEquals(0.seconds, snap.remaining)
    }

    @Test
    fun snapshotIsNullWhenPaused() {
        val entry = fakeEntry(startDate = null, finishDate = null)
        assertNull(SkillProgressCalculator().snapshot(entry))
    }

    @Test
    fun formatHmsCoversAllBranches() {
        assertEquals("2h 3m 4s", (7384.seconds).formatHms())
        assertEquals("5m 6s", (306.seconds).formatHms())
        assertEquals("9s", (9.seconds).formatHms())
        assertEquals("0s", (0.seconds).formatHms())
    }

    private fun calcAt(iso: String) = SkillProgressCalculator(FixedClock(Instant.parse(iso)))

    private fun fakeEntry(startDate: Long?, finishDate: Long?) = com.podforeve.tracker.domain.model.SkillQueueEntry(
        queuePosition = 0, characterId = 1L, skillId = 1, skillName = "Test",
        finishedLevel = 1, startSp = 0, finishSp = 100,
        startDate = startDate, finishDate = finishDate,
    )
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now() = instant
}
