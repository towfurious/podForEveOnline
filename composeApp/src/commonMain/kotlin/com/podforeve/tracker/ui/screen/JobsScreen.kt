@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatDhm
import com.podforeve.tracker.ui.component.ErrorState
import com.podforeve.tracker.ui.component.GradientProgressBar
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.theme.EmberColorScheme
import com.podforeve.tracker.ui.viewmodel.IndustryJobViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

// See wiki: [[Screen - Jobs]], [[Industry Job]]
class JobsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<IndustryJobViewModel>()
        val state by viewModel.uiState.collectAsState()
        JobsContent(state = state, onRetry = viewModel::refresh)
    }
}

@Composable
private fun JobsContent(state: UiState<List<IndustryJob>>, onRetry: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is UiState.Loading -> JobsSkeleton()
                is UiState.Error -> JobsError(state.message, onRetry)
                is UiState.Success -> JobsSuccess(state.data, onRetry)
            }
        }
    }
}

@Composable
private fun JobsSuccess(jobs: List<IndustryJob>, onRetry: () -> Unit) {
    if (jobs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No active industry jobs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val calculator = remember { SkillProgressCalculator() }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = navBottom + 80.dp)) {
        items(jobs, key = { it.jobId }) { job ->
            JobCard(job = job, calculator = calculator, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun JobCard(job: IndustryJob, calculator: SkillProgressCalculator, modifier: Modifier = Modifier) {
    val start = remember(job.startDateEpochSeconds) { Instant.fromEpochSeconds(job.startDateEpochSeconds) }
    val end = remember(job.endDateEpochSeconds) { Instant.fromEpochSeconds(job.endDateEpochSeconds) }

    val snapshot by produceState(
        initialValue = calculator.snapshot(start, end),
        key1 = job.jobId,
    ) {
        while (true) {
            value = calculator.snapshot(start, end)
            delay(60_000)
        }
    }

    val isActive = job.status == "active" && snapshot.progress < 1.0
    val isComplete = job.status == "active" && snapshot.progress >= 1.0
    val statusLabel = when {
        isComplete -> "Complete"
        isActive -> "Active"
        else -> job.status.replaceFirstChar { it.uppercase() }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (!isActive) 0.75f else 1f),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = job.activityName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                JobStatusChip(isActive = isActive, isComplete = isComplete, label = statusLabel)
            }
            Spacer(Modifier.height(4.dp))
            Text(job.blueprintName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            GradientProgressBar(
                progress = snapshot.progress.toFloat(),
                modifier = Modifier.fillMaxWidth().height(5.dp),
            )
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = when {
                        isComplete -> "Ready to deliver"
                        isActive -> snapshot.remaining.formatDhm()
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isComplete) Color(0xFF3FB950) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${job.runs} run${if (job.runs > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun JobStatusChip(isActive: Boolean, isComplete: Boolean, label: String) {
    val (bg, fg) = when {
        isComplete -> Color(0x1F3FB950) to Color(0xFF3FB950)
        !isActive -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f) to
            MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) to
            MaterialTheme.colorScheme.primary
    }
    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun JobsSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(100.dp).shimmer())
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun JobsError(message: String, onRetry: () -> Unit) = ErrorState(message, onRetry)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview @Composable
private fun JobsPreviewSuccess() = MaterialTheme(EmberColorScheme) {
    JobsContent(
        state = UiState.Success(
            listOf(
                IndustryJob(1001, 0L, 1, "Stabber Blueprint", 5, 1_752_000_000L - 7_200, 1_752_000_000L + 18_000, "active"),
                IndustryJob(1002, 0L, 4, "Mining Barge Blueprint", 1, 1_752_000_000L - 86_400, 1_752_000_000L + 86_400, "active"),
                IndustryJob(1003, 0L, 8, "Catalyst Blueprint", 10, 1_752_000_000L - 3_600, 1_752_000_000L - 600, "delivered"),
                IndustryJob(1004, 0L, 1, "Procurer Blueprint", 3, 1_752_000_000L - 172_800, 1_752_000_000L - 43_200, "delivered"),
            ),
        ),
        onRetry = {},
    )
}

@Preview @Composable
private fun JobsPreviewLoading() = MaterialTheme(EmberColorScheme) {
    JobsContent(state = UiState.Loading, onRetry = {})
}
