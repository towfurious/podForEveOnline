package com.podforeve.tracker.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatHms
import kotlinx.coroutines.delay

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
            )
            Text(
                text = snapshot?.remaining?.formatHms() ?: "Paused",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LinearProgressIndicator(
            progress = { snapshot?.progress?.toFloat() ?: 0f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(8.dp),
        )
    }
}

// Compact row for queue entries below the head skill.
@Composable
fun SkillQueueRow(
    entry: SkillQueueEntry,
    displayPosition: Int,
    modifier: Modifier = Modifier,
    calculator: SkillProgressCalculator = remember { SkillProgressCalculator() },
) {
    val snapshot by produceState(
        initialValue = calculator.snapshot(entry),
        key1 = entry.finishDate,
    ) {
        while (true) {
            value = calculator.snapshot(entry)
            delay(60_000) // queue rows only need a minute-level tick
        }
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
            snapshot?.remaining?.formatHms() ?: "Paused",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
