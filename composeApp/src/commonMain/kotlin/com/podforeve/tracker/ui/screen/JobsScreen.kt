@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.ui.theme.EmberColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatDhm
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.IndustryJobViewModel
import kotlinx.coroutines.delay
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
                is UiState.Error   -> JobsError(state.message, onRetry)
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
private fun JobCard(
    job: IndustryJob,
    calculator: SkillProgressCalculator,
    modifier: Modifier = Modifier,
) {
    val start = remember(job.startDateEpochSeconds) { Instant.fromEpochSeconds(job.startDateEpochSeconds) }
    val end   = remember(job.endDateEpochSeconds)   { Instant.fromEpochSeconds(job.endDateEpochSeconds) }

    val snapshot by produceState(
        initialValue = calculator.snapshot(start, end),
        key1 = job.jobId,
    ) {
        while (true) {
            value = calculator.snapshot(start, end)
            delay(60_000) // update every minute
        }
    }

    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = job.activityName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text  = "${job.runs} run${if (job.runs > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(job.blueprintName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { snapshot.progress.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = snapshot.remaining.formatDhm(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
private fun JobsError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview @Composable
private fun JobsPreviewSuccess() = MaterialTheme(EmberColorScheme) {
    JobsContent(
        state = UiState.Success(listOf(
            IndustryJob(1001, 0L, 1, "Stabber Blueprint",        5,  1_752_000_000L - 7_200,  1_752_000_000L + 18_000, "active"),
            IndustryJob(1002, 0L, 4, "Mining Barge Blueprint",   1,  1_752_000_000L - 86_400, 1_752_000_000L + 86_400, "active"),
            IndustryJob(1003, 0L, 8, "Catalyst Blueprint",       10, 1_752_000_000L - 3_600,  1_752_000_000L - 600,    "delivered"),
            IndustryJob(1004, 0L, 1, "Procurer Blueprint",       3,  1_752_000_000L - 172_800,1_752_000_000L - 43_200, "delivered"),
        )),
        onRetry = {},
    )
}

@Preview @Composable
private fun JobsPreviewLoading() = MaterialTheme(EmberColorScheme) {
    JobsContent(state = UiState.Loading, onRetry = {})
}
