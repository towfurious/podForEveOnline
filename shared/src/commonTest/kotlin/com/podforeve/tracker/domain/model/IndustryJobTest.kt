package com.podforeve.tracker.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class IndustryJobTest {
    private fun job(activityId: Int) = IndustryJob(
        jobId = 1,
        characterId = 1L,
        activityId = activityId,
        blueprintName = "Test Blueprint",
        runs = 1,
        startDateEpochSeconds = 0,
        endDateEpochSeconds = 100,
        status = "active",
    )

    @Test
    fun activityNameMapsKnownActivityIds() {
        assertEquals("Manufacturing", job(1).activityName)
        assertEquals("TE Research", job(3).activityName)
        assertEquals("ME Research", job(4).activityName)
        assertEquals("Copying", job(5).activityName)
        assertEquals("Invention", job(8).activityName)
        assertEquals("Reactions", job(9).activityName)
    }

    @Test
    fun activityNameFallsBackToGenericLabelForUnknownIds() {
        assertEquals("Activity 2", job(2).activityName) // not one of MVP's mapped ids
        assertEquals("Activity 42", job(42).activityName)
    }
}
