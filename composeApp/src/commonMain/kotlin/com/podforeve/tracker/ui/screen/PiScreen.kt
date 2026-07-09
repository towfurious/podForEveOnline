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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.PlanetStatus
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.PlanetViewModel
import kotlinx.datetime.Clock

// See wiki: [[Screen - PI]], [[Planet]]
class PiScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PlanetViewModel>()
        val state by viewModel.uiState.collectAsState()
        PiContent(state = state, onRetry = viewModel::refresh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PiContent(state: UiState<List<Planet>>, onRetry: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Planets") }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is UiState.Loading -> PiSkeleton()
                is UiState.Error   -> PiError(state.message, onRetry)
                is UiState.Success -> PiSuccess(state.data, onRetry)
            }
        }
    }
}

@Composable
private fun PiSuccess(planets: List<Planet>, onRetry: () -> Unit) {
    if (planets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No planets found.\nSet up PI colonies in the EVE client.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val now = remember { Clock.System.now().epochSeconds }
    LazyColumn(Modifier.fillMaxSize()) {
        items(planets, key = { it.planetId }) { planet ->
            PlanetCard(planet = planet, now = now, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlanetCard(planet: Planet, now: Long, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(planet.planetName, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${planet.planetType.replaceFirstChar { it.uppercase() }} · Level ${planet.upgradeLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    planet.lastUpdateText(now),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(planet.status(now))
        }
    }
}

@Composable
private fun StatusChip(status: PlanetStatus) {
    val color = when (status) {
        PlanetStatus.ACTIVE          -> MaterialTheme.colorScheme.primary
        PlanetStatus.NEEDS_ATTENTION -> MaterialTheme.colorScheme.tertiary
        PlanetStatus.IDLE            -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        PlanetStatus.ACTIVE          -> "Active"
        PlanetStatus.NEEDS_ATTENTION -> "Attention"
        PlanetStatus.IDLE            -> "Idle"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PiSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        repeat(4) {
            Box(Modifier.fillMaxWidth().height(80.dp).shimmer())
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PiError(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}
