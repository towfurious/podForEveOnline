package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.podforeve.tracker.domain.model.IndustryJob
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.usecase.SkillProgressCalculator
import com.podforeve.tracker.domain.usecase.formatDhm
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.IndustryJobViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant

// See wiki: [[Screen - Jobs]], [[Industry Job]]
class JobsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<IndustryJobViewModel>()
        val state by viewModel.uiState.collectAsState()
        JobsContent(state = state, onRetry = viewModel::refresh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobsContent(state: UiState<List<IndustryJob>>, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Industry Jobs") }) },
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
    LazyColumn(Modifier.fillMaxSize()) {
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
