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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.PlanetStatus
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.ui.theme.EmberColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.viewmodel.PlanetViewModel
import kotlin.time.Clock

// See wiki: [[Screen - PI]], [[Planet]]
class PiScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PlanetViewModel>()
        val state by viewModel.uiState.collectAsState()
        PiContent(state = state, onRetry = viewModel::refresh)
    }
}

@Composable
private fun PiContent(state: UiState<List<Planet>>, onRetry: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
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
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = navBottom + 80.dp)) {
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
                Spacer(Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    PlanetTypeChip(planet.planetType)
                    Text(
                        "Level ${planet.upgradeLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(3.dp))
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
private fun PlanetTypeChip(type: String) {
    val colors = when (type.lowercase()) {
        "barren"    -> Color(0xFF2A1E10) to Color(0xFFC9841A)
        "plasma"    -> Color(0xFF2A1015) to Color(0xFFE84030)
        "storm"     -> Color(0xFF1A1535) to Color(0xFF7860E8)
        "oceanic"   -> Color(0xFF0E2035) to Color(0xFF2090D8)
        "temperate" -> Color(0xFF102015) to Color(0xFF40A060)
        "lava"      -> Color(0xFF251010) to Color(0xFFC02010)
        "ice"       -> Color(0xFF101830) to Color(0xFF50A8E0)
        "gas"       -> Color(0xFF1C2210) to Color(0xFF9AB020)
        else        -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = colors.first,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text     = type.replaceFirstChar { it.uppercase() },
            style    = MaterialTheme.typography.labelSmall,
            color    = colors.second,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview @Composable
private fun PiPreviewSuccess() = MaterialTheme(EmberColorScheme) {
    PiContent(
        state = UiState.Success(listOf(
            Planet(40000001, 0L, "Jita IV",       "temperate", 1_752_000_000L - 3_600,  4),
            Planet(40000002, 0L, "Perimeter II",   "barren",    1_752_000_000L - 50_000, 3),
            Planet(40000003, 0L, "Nonni VI",       "gas",       1_752_000_000L - 90_000, 3),
            Planet(40000004, 0L, "Maurasi I",      "lava",      null,                    2),
        )),
        onRetry = {},
    )
}

@Preview @Composable
private fun PiPreviewLoading() = MaterialTheme(EmberColorScheme) {
    PiContent(state = UiState.Loading, onRetry = {})
}
