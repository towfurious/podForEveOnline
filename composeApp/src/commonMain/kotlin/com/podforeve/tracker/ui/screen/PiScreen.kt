@file:OptIn(kotlin.time.ExperimentalTime::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.podforeve.tracker.domain.model.ColonySummary
import com.podforeve.tracker.domain.model.Planet
import com.podforeve.tracker.domain.model.PlanetStatus
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.ui.component.ErrorState
import com.podforeve.tracker.ui.component.GlowCard
import com.podforeve.tracker.ui.component.GradientProgressBar
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.icon.EveIcons
import com.podforeve.tracker.ui.theme.EmberColorScheme
import com.podforeve.tracker.ui.viewmodel.PlanetViewModel
import kotlin.time.Clock

private val ColorStopped = Color(0xFFE84030)
private val ColorAmber = Color(0xFFD6A020)
private val ColorGreen = Color(0xFF30B858)

class PiScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<PlanetViewModel>()
        val state by viewModel.uiState.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        PiContent(state = state, isRefreshing = isRefreshing, onRefresh = viewModel::refresh)
    }
}

@Composable
private fun PiContent(state: UiState<List<Planet>>, isRefreshing: Boolean, onRefresh: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is UiState.Loading -> PiSkeleton()
                is UiState.Error -> PiError(state.message, onRefresh)
                is UiState.Success -> PiSuccess(state.data, isRefreshing, onRefresh)
            }
        }
    }
}

@Composable
private fun PiSuccess(planets: List<Planet>, isRefreshing: Boolean, onRefresh: () -> Unit) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (planets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No planets found.\nSet up PI colonies in the EVE client.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@PullToRefreshBox
        }
        val now = remember { Clock.System.now().epochSeconds }
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = navBottom + 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(planets, key = { it.planetId }) { planet ->
                PlanetCard(planet = planet, now = now)
            }
        }
    }
}

@Composable
private fun PlanetCard(planet: Planet, now: Long) {
    GlowCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            // ── Header row ───────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = planet.planetName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        PlanetTypeChip(planet.planetType)
                        Text(
                            "Level ${planet.upgradeLevel}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                val colony = planet.colony
                val expiryEpoch = colony?.extractorExpiryEpochSeconds
                if (colony != null && expiryEpoch != null) {
                    ExtractorCountdown(expiryEpochSeconds = expiryEpoch, now = now)
                } else if (colony == null) {
                    StatusChip(planet.status(now))
                }
            }

            // ── Colony detail section ─────────────────────────────────────────
            val colony = planet.colony
            if (colony != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (colony.totalFactories > 0) {
                        FactoriesRow(running = colony.runningFactories, total = colony.totalFactories)
                    }
                    if (colony.sfCapacityM3 > 0) {
                        StorageRow("Storage", colony.sfFillRatio, colony.sfUsedM3, colony.sfCapacityM3)
                    }
                    if (colony.lpCapacityM3 > 0) {
                        StorageRow("Launchpad", colony.lpFillRatio, colony.lpUsedM3, colony.lpCapacityM3)
                    }
                    Text(
                        text = "data ${colony.dataAgeText(now)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Extractor countdown ───────────────────────────────────────────────────────

@Composable
private fun ExtractorCountdown(expiryEpochSeconds: Long, now: Long) {
    val remainingSecs = expiryEpochSeconds - now
    val stopped = remainingSecs <= 0
    val color = when {
        stopped -> ColorStopped
        remainingSecs < 2 * 3_600 -> ColorStopped
        remainingSecs < 8 * 3_600 -> ColorAmber
        else -> ColorGreen
    }
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = if (stopped) "EXTRACTORS" else "STOPS IN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.08.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (stopped) "stopped" else formatCountdown(remainingSecs),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            lineHeight = 22.sp,
        )
    }
}

// ── Factories row ─────────────────────────────────────────────────────────────

@Composable
private fun FactoriesRow(running: Int, total: Int) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = EveIcons.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "Factories",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$total",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(total) { index ->
                Box(Modifier.size(8.dp).clip(CircleShape)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = if (index < running) ColorGreen else MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {}
                }
            }
        }
    }
}

// ── Storage bar ───────────────────────────────────────────────────────────────

@Composable
private fun StorageRow(label: String, fillRatio: Float, usedM3: Double, capacityM3: Double) {
    val fillColor = if (fillRatio > 0.85f) ColorStopped else MaterialTheme.colorScheme.primary

    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = EveIcons.Industry,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${(fillRatio * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = fillColor,
            )
        }
        Spacer(Modifier.height(5.dp))
        GradientProgressBar(
            progress = fillRatio,
            modifier = Modifier.fillMaxWidth().height(5.dp),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "${formatM3(usedM3)} / ${formatM3(capacityM3)} m³",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun StatusChip(status: PlanetStatus) {
    val color = when (status) {
        PlanetStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        PlanetStatus.NEEDS_ATTENTION -> MaterialTheme.colorScheme.tertiary
        PlanetStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        PlanetStatus.ACTIVE -> "Active"
        PlanetStatus.NEEDS_ATTENTION -> "Attention"
        PlanetStatus.IDLE -> "Idle"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun PlanetTypeChip(type: String) {
    val (bg, fg) = when (type.lowercase()) {
        "barren" -> Color(0xFF2A1E10) to Color(0xFFC9841A)
        "plasma" -> Color(0xFF2A1015) to Color(0xFFE84030)
        "storm" -> Color(0xFF1A1535) to Color(0xFF7860E8)
        "oceanic" -> Color(0xFF0E2035) to Color(0xFF2090D8)
        "temperate" -> Color(0xFF102015) to Color(0xFF40A060)
        "lava" -> Color(0xFF251010) to Color(0xFFC02010)
        "ice" -> Color(0xFF101830) to Color(0xFF50A8E0)
        "gas" -> Color(0xFF1C2210) to Color(0xFF9AB020)
        else -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun PiSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(5) {
            Box(Modifier.fillMaxWidth().height(110.dp).shimmer())
        }
    }
}

@Composable
private fun PiError(message: String, onRetry: () -> Unit) = ErrorState(message, onRetry)

private fun formatCountdown(secs: Long): String {
    val d = secs / 86_400
    val h = (secs % 86_400) / 3_600
    val m = (secs % 3_600) / 60
    return when {
        d > 0 -> "${d}d ${h}h"
        h > 0 -> "${h}h ${m}m"
        else -> "${m}m"
    }
}

private fun formatM3(value: Double): String {
    val v = value.toLong()
    return if (v >= 1_000) "${v / 1_000} ${(v % 1_000).toString().padStart(3, '0')}" else v.toString()
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewNow = Clock.System.now().epochSeconds

@Suppress("LongMethod")
@Preview
@Composable
private fun PiPreviewSuccess() = androidx.compose.material3.MaterialTheme(EmberColorScheme) {
    PiContent(
        state = UiState.Success(
            listOf(
                Planet(
                    40132050,
                    0L,
                    "Nakugard I",
                    "barren",
                    previewNow - 3_600,
                    4,
                    colony = ColonySummary(
                        extractorExpiryEpochSeconds = previewNow + 2 * 86_400 + 19 * 3_600,
                        runningFactories = 3,
                        totalFactories = 3,
                        sfCapacityM3 = 12_000.0,
                        sfUsedM3 = 1_572.0,
                        lpCapacityM3 = 10_000.0,
                        lpUsedM3 = 60.0,
                        dataFetchedAtEpochSeconds = previewNow - 180,
                    ),
                ),
                Planet(
                    40132051,
                    0L,
                    "Nakugard II",
                    "lava",
                    previewNow - 1_800,
                    4,
                    colony = ColonySummary(
                        extractorExpiryEpochSeconds = previewNow + 2 * 86_400 + 19 * 3_600 - 1_600,
                        runningFactories = 3,
                        totalFactories = 3,
                        sfCapacityM3 = 12_000.0,
                        sfUsedM3 = 856.0,
                        lpCapacityM3 = 10_000.0,
                        lpUsedM3 = 52.5,
                        dataFetchedAtEpochSeconds = previewNow - 420,
                    ),
                ),
                Planet(
                    40132056,
                    0L,
                    "Nakugard IV",
                    "storm",
                    previewNow - 2_000,
                    4,
                    colony = ColonySummary(
                        extractorExpiryEpochSeconds = previewNow + 3 * 86_400 + 20 * 3_600,
                        runningFactories = 3,
                        totalFactories = 3,
                        sfCapacityM3 = 12_000.0,
                        sfUsedM3 = 985.0,
                        lpCapacityM3 = 10_000.0,
                        lpUsedM3 = 37.5,
                        dataFetchedAtEpochSeconds = previewNow - 55,
                    ),
                ),
                Planet(
                    40132079,
                    0L,
                    "Nakugard VI",
                    "temperate",
                    previewNow - 900,
                    4,
                    colony = ColonySummary(
                        extractorExpiryEpochSeconds = previewNow + 3 * 86_400 + 21 * 3_600,
                        runningFactories = 2,
                        totalFactories = 2,
                        sfCapacityM3 = 24_000.0,
                        sfUsedM3 = 6_041.0,
                        lpCapacityM3 = 20_000.0,
                        lpUsedM3 = 1_102.0,
                        dataFetchedAtEpochSeconds = previewNow - 600,
                    ),
                ),
            ),
        ),
        isRefreshing = false,
        onRefresh = {},
    )
}

@Preview
@Composable
private fun PiPreviewLoading() = MaterialTheme(EmberColorScheme) {
    PiContent(state = UiState.Loading, isRefreshing = false, onRefresh = {})
}
