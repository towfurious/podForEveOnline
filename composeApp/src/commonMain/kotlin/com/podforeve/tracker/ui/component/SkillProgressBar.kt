@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatDhm
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

// Large hero progress section for the currently-training skill.
// Ticks every second via produceState — no network calls per tick.
// See wiki: [[Math-Based Progress Bar]], [[ADR-005 - Math-Based Skill Progress]]
@Composable
fun ActiveSkillProgressSection(
    entry: SkillQueueEntry,
    modifier: Modifier = Modifier,
    calculator: SkillProgressCalculator = remember { SkillProgressCalculator() },
) {
    val snapshot by produceState(
        initialValue = calculator.snapshot(entry),
        key1 = entry.skillId,
        key2 = entry.finishDate,
    ) {
        while (true) {
            value = calculator.snapshot(entry)
            delay(1_000)
        }
    }

    Column(modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${entry.skillName} → Level ${entry.finishedLevel}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = snapshot?.remaining?.formatDhm() ?: "Paused",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        GradientProgressBar(
            progress = snapshot?.progress?.toFloat() ?: 0f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(8.dp),
        )
    }
}

// Compact row for queue entries below the head skill.
// Shows finishDate - startDate = this skill's own training time only (not cumulative).
@Composable
fun SkillQueueRow(entry: SkillQueueEntry, displayPosition: Int, modifier: Modifier = Modifier) {
    val start = entry.startDate
    val finish = entry.finishDate
    val duration = remember(start, finish) {
        if (start != null && finish != null) (finish - start).seconds else null
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "$displayPosition. ${entry.skillName} ${entry.finishedLevel}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            duration?.formatDhm() ?: "Paused",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun GradientProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier) {
        val r = CornerRadius(size.height / 2f)
        // Track
        drawRoundRect(color = trackColor, cornerRadius = r)
        // Gradient fill — orange left, gold right, always spanning full width
        if (progress > 0f) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFBF3A20),
                        Color(0xFFDF7820),
                        Color(0xFFF5D060),
                    ),
                ),
                size = Size(size.width * progress.coerceIn(0f, 1f), size.height),
                cornerRadius = r,
            )
        }
    }
}
