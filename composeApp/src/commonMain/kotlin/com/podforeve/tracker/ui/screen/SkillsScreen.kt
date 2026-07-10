@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.usecase.formatDhm
import com.podforeve.tracker.ui.theme.EmberColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import com.podforeve.tracker.ui.component.ActiveSkillProgressSection
import com.podforeve.tracker.ui.component.ErrorState
import com.podforeve.tracker.ui.component.SkillQueueRow
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.SkillQueueViewModel

// See wiki: [[Screen - Skills]]
class SkillsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<SkillQueueViewModel>()
        val state by viewModel.uiState.collectAsState()
        SkillsContent(state = state, onRefresh = viewModel::refresh)
    }
}

@Composable
private fun SkillsContent(
    state: UiState<List<SkillQueueEntry>>,
    onRefresh: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                is UiState.Loading  -> SkillsLoadingSkeleton()
                is UiState.Success  -> SkillsSuccessContent(state.data, onRefresh)
                is UiState.Error    -> SkillsErrorContent(state.message, onRefresh)
            }
        }
    }
}

@Composable
private fun SkillsSuccessContent(
    entries: List<SkillQueueEntry>,
    onRefresh: () -> Unit,
) {
    val now = Clock.System.now().epochSeconds
    val pending = entries.filterNot { it.hasFinished(now) }

    if (pending.isEmpty()) {
        QueueEmptyBanner()
        return
    }

    val head = pending.first()
    val rest = pending.drop(1)
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = navBottom + 80.dp)) {
        item {
            ActiveSkillProgressSection(entry = head, modifier = Modifier.fillMaxWidth())
            HorizontalDivider()
        }
        if (head.startDate == null) {
            item {
                PausedQueueBanner()
                HorizontalDivider()
            }
        }
        itemsIndexed(rest, key = { _, e -> e.queuePosition }) { index, entry ->
            SkillQueueRow(entry = entry, displayPosition = index + 2)
        }
        item {
            val totalRemaining = pending.lastOrNull()?.finishDate
                ?.let { (it - Clock.System.now().epochSeconds).seconds }
            HorizontalDivider()
            TotalQueueRow(totalRemaining?.formatDhm())
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SkillsLoadingSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        // Progress bar skeleton
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shimmer()
        )
        Spacer(Modifier.height(16.dp))
        repeat(6) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .shimmer()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SkillsErrorContent(message: String, onRetry: () -> Unit) = ErrorState(message, onRetry)

@Composable
private fun TotalQueueRow(total: String?) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "Total",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            total ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PausedQueueBanner() {
    Text(
        text = "Queue paused — open the EVE client to resume training.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun QueueEmptyBanner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Skill queue is empty.\nOpen the EVE client to add skills.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private fun previewEntry(pos: Int, name: String, level: Int, training: Boolean) = SkillQueueEntry(
    queuePosition = pos, characterId = 0L, skillId = 10000 + pos,
    skillName = name, finishedLevel = level,
    startSp = 1_000 * pos, finishSp = 90_000 * pos,
    startDate = if (training) 1_752_000_000L else null,
    finishDate = if (training) 4_000_000_000L else null,
)

@Preview @Composable
private fun SkillsPreviewSuccess() = MaterialTheme(EmberColorScheme) {
    SkillsContent(
        state = UiState.Success(listOf(
            previewEntry( 1, "Coherent Ore Processing",   5, training = true),
            previewEntry( 2, "Simple Ore Processing",     3, training = false),
            previewEntry( 3, "Simple Ore Processing",     4, training = false),
            previewEntry( 4, "Simple Ore Processing",     5, training = false),
            previewEntry( 5, "Graviton Physics",          3, training = false),
            previewEntry( 6, "Graviton Physics",          4, training = false),
            previewEntry( 7, "Retail",                    3, training = false),
            previewEntry( 8, "Retail",                    4, training = false),
            previewEntry( 9, "Marketing",                 4, training = false),
            previewEntry(10, "Accounting",                4, training = false),
            previewEntry(11, "Broker Relations",          4, training = false),
            previewEntry(12, "Contracting",               4, training = false),
            previewEntry(13, "Mining Drone Operation",    4, training = false),
            previewEntry(14, "Warp Drive Operation",      5, training = false),
            previewEntry(15, "Jump Drive Operation",      1, training = false),
            previewEntry(16, "Jump Drive Operation",      2, training = false),
            previewEntry(17, "Jump Drive Operation",      3, training = false),
            previewEntry(18, "Jump Drive Operation",      4, training = false),
            previewEntry(19, "Nanite Operation",          2, training = false),
            previewEntry(20, "Nanite Interfacing",        3, training = false),
            previewEntry(21, "Remote Armor Repair",       4, training = false),
            previewEntry(22, "Repair Drone Operation",    5, training = false),
            previewEntry(23, "Drones",                    5, training = false),
            previewEntry(24, "Drone Interfacing",         4, training = false),
            previewEntry(25, "Heavy Drone Operation",     4, training = false),
        )),
        onRefresh = {},
    )
}

@Preview @Composable
private fun SkillsPreviewLoading() = MaterialTheme(EmberColorScheme) {
    SkillsContent(state = UiState.Loading, onRefresh = {})
}
