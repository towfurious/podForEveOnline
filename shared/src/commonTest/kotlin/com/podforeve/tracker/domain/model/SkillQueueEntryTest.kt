package com.podforeve.tracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SkillQueueEntryTest {
    private val now = 1_000_000L

    @Test
    fun isTrainingRequiresBothDates() {
        assertTrue(entry(pos = 0, start = now - 10, finish = now + 10).isTraining)
        assertFalse(entry(pos = 0, start = null, finish = now + 10).isTraining)
        assertFalse(entry(pos = 0, start = now - 10, finish = null).isTraining)
        assertFalse(entry(pos = 0, start = null, finish = null).isTraining)
    }

    @Test
    fun hasFinishedComparesFinishDateAgainstClock() {
        assertTrue(entry(pos = 0, start = now - 20, finish = now - 1).hasFinished(now))
        assertTrue(entry(pos = 0, start = now - 20, finish = now).hasFinished(now)) // boundary: exactly now counts as finished
        assertFalse(entry(pos = 0, start = now - 20, finish = now + 1).hasFinished(now))
        assertFalse(entry(pos = 0, start = null, finish = null).hasFinished(now)) // paused entries are never "finished"
    }

    @Test
    fun activeSkillIsNullForEmptyQueue() {
        assertNull(emptyList<SkillQueueEntry>().activeSkill(now))
    }

    @Test
    fun activeSkillReturnsTheOneTrainingNonFinishedEntry() {
        val head = entry(pos = 0, start = now - 10, finish = now + 100)
        assertEquals(head, listOf(head).activeSkill(now))
    }

    @Test
    fun activeSkillReturnsNullWhenTheOnlyEntryIsAlreadyFinished() {
        val finished = entry(pos = 0, start = now - 100, finish = now - 10)
        assertNull(listOf(finished).activeSkill(now))
    }

    // Regression test for the real bug found during ADR-015 device verification: ESI leaves a
    // just-finished skill at queuePosition == 0 for a while before rotating it out. Picking
    // "queuePosition == 0" instead of "the first non-finished training entry" schedules a
    // notification for a skill that already completed hours ago, while never noticing the skill
    // that's actually training. See wiki: [[Skill Queue]] business rules, log.md 2026-07-15.
    @Test
    fun activeSkillSkipsAStaleFinishedHeadAndFindsTheRealOneBehindIt() {
        val staleFinishedHead = entry(pos = 0, start = now - 500_000, finish = now - 23_075)
        val realActiveSkill = entry(pos = 1, start = now - 23_075, finish = now + 1_960_279)
        val notYetStarted = entry(pos = 2, start = null, finish = null)

        val result = listOf(staleFinishedHead, realActiveSkill, notYetStarted).activeSkill(now)

        assertEquals(realActiveSkill, result)
    }

    @Test
    fun activeSkillSkipsAPausedHeadAndFindsTheRealOneBehindIt() {
        val paused = entry(pos = 0, start = null, finish = null)
        val training = entry(pos = 1, start = now - 10, finish = now + 10)

        assertEquals(training, listOf(paused, training).activeSkill(now))
    }

    @Test
    fun activeSkillReturnsTheEarliestQueuePositionWhenMultipleQualify() {
        // Shouldn't normally happen (only one skill trains at a time) but the selector must be
        // deterministic rather than picking arbitrarily if it ever does.
        val first = entry(pos = 0, start = now - 10, finish = now + 10)
        val second = entry(pos = 1, start = now - 10, finish = now + 20)

        assertEquals(first, listOf(first, second).activeSkill(now))
    }

    @Test
    fun activeSkillReturnsNullWhenEveryEntryIsPausedOrFinished() {
        val finished = entry(pos = 0, start = now - 100, finish = now - 10)
        val paused = entry(pos = 1, start = null, finish = null)

        assertNull(listOf(finished, paused).activeSkill(now))
    }

    private fun entry(pos: Int, start: Long?, finish: Long?) = SkillQueueEntry(
        queuePosition = pos,
        characterId = 1L,
        skillId = 100 + pos,
        skillName = "Skill $pos",
        finishedLevel = 4,
        startSp = 0,
        finishSp = 100_000,
        startDate = start,
        finishDate = finish,
    )
}
