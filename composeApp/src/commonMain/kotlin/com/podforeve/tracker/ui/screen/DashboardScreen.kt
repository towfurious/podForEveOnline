package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import coil3.compose.AsyncImage
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.ui.component.ActiveSkillProgressSection
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.DashboardData
import com.podforeve.tracker.ui.viewmodel.DashboardViewModel
import kotlin.math.abs
import kotlin.math.round

// See wiki: [[Screen - Dashboard]]
class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<DashboardViewModel>()
        val state by viewModel.uiState.collectAsState()
        DashboardContent(state = state, onRetry = viewModel::refresh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(state: UiState<DashboardData>, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                is UiState.Loading -> DashboardSkeleton()
                is UiState.Error   -> DashboardError(state.message, onRetry)
                is UiState.Success -> DashboardSuccess(state.data)
            }
        }
    }
}

@Composable
private fun DashboardSuccess(data: DashboardData) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Character header
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model              = data.portraitUrl,
                contentDescription = "${data.name} portrait",
                modifier           = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text  = data.name,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Spacer(Modifier.height(20.dp))

        // ISK balance
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text  = "ISK Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = data.iskBalance.formatIsk(),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Training widget
        Card(Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text     = "Training",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                )
                if (data.activeSkill != null) {
                    ActiveSkillProgressSection(
                        entry    = data.activeSkill,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text     = "No skill training — open the EVE client to start a queue.",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape).shimmer())
            Spacer(Modifier.width(12.dp))
            Box(Modifier.width(160.dp).height(24.dp).shimmer())
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(80.dp).shimmer())
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(80.dp).shimmer())
    }
}

@Composable
private fun DashboardError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text     = message,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

// ISK formatter — pure Kotlin, no JVM String.format()
private fun Double.formatIsk(): String = when {
    this >= 1e9 -> "${roundTo2dp(this / 1e9)}B ISK"
    this >= 1e6 -> "${roundTo2dp(this / 1e6)}M ISK"
    this >= 1e3 -> "${roundTo2dp(this / 1e3)}K ISK"
    else        -> "${toLong()} ISK"
}

private fun roundTo2dp(value: Double): String {
    val rounded = round(value * 100.0) / 100.0
    val i = rounded.toLong()
    val f = abs(round((rounded - i) * 100.0)).toLong()
    return "$i.${f.toString().padStart(2, '0')}"
}
