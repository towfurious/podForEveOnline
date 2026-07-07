package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import kotlinx.datetime.Clock
import com.podforeve.tracker.ui.component.ActiveSkillProgressSection
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillsContent(
    state: UiState<List<SkillQueueEntry>>,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Skills") }) },
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

    LazyColumn(Modifier.fillMaxSize()) {
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
        item { Spacer(Modifier.height(16.dp)) }
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
private fun SkillsErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
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
