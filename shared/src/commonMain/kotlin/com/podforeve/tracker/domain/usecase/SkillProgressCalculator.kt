package com.podforeve.tracker.domain.usecase

import com.podforeve.tracker.domain.model.SkillQueueEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Math-only progress computer — no network, no state, fully testable via injected Clock.
// See wiki: [[ADR-005 - Math-Based Skill Progress]], [[Math-Based Progress Bar]]
class SkillProgressCalculator(private val clock: Clock = Clock.System) {

    data class Snapshot(
        val progress: Double,   // 0.0–1.0
        val remaining: Duration,
    )

    // Returns null when the entry is paused (startDate or finishDate absent).
    fun snapshot(entry: SkillQueueEntry): Snapshot? {
        val start = entry.startDate?.let { Instant.fromEpochSeconds(it) } ?: return null
        val end = entry.finishDate?.let { Instant.fromEpochSeconds(it) } ?: return null
        return snapshot(start, end)
    }

    fun snapshot(start: Instant, end: Instant): Snapshot {
        val now = clock.now()
        val totalMs = (end - start).inWholeMilliseconds.coerceAtLeast(1L)
        val elapsedMs = (now - start).inWholeMilliseconds.coerceIn(0L, totalMs)
        val remaining = (end - now).coerceAtLeast(Duration.ZERO)
        return Snapshot(
            progress  = elapsedMs.toDouble() / totalMs.toDouble(),
            remaining = remaining,
        )
    }
}

// Extension used by UI formatters.
fun Duration.formatHms(): String {
    val total = inWholeSeconds.coerceAtLeast(0L)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return when {
        h > 0L -> "${h}h ${m}m ${s}s"
        m > 0L -> "${m}m ${s}s"
        else   -> "${s}s"
    }
}
