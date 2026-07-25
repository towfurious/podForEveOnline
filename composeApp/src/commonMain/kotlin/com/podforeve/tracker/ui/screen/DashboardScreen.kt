@file:OptIn(kotlin.time.ExperimentalTime::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.podforeve.tracker.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import coil3.compose.AsyncImage
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.domain.model.SkillQueueEntry
import com.podforeve.tracker.domain.model.UiState
import com.podforeve.tracker.domain.model.WalletJournalEntry
import com.podforeve.tracker.ui.component.ActiveSkillProgressSection
import com.podforeve.tracker.ui.component.ErrorState
import com.podforeve.tracker.ui.component.GlowCard
import com.podforeve.tracker.ui.component.shimmer
import com.podforeve.tracker.ui.icon.EveIcons
import com.podforeve.tracker.ui.theme.AppTheme
import com.podforeve.tracker.ui.theme.EmberColorScheme
import com.podforeve.tracker.ui.theme.gainColor
import com.podforeve.tracker.ui.theme.previewColor
import com.podforeve.tracker.ui.theme.rememberThemeRepositoryOrNull
import com.podforeve.tracker.ui.viewmodel.DashboardData
import com.podforeve.tracker.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.mp.KoinPlatform.getKoin
import kotlin.math.abs
import kotlin.math.round
import kotlin.time.Clock

// See wiki: [[Screen - Dashboard]]
class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<DashboardViewModel>()
        val state by viewModel.uiState.collectAsState()
        val authRepository: AuthRepository = remember { getKoin().get() }
        val authState by authRepository.authState.collectAsState()
        val isDemo = authState is AuthState.Demo
        DashboardContent(
            state = state,
            isDemo = isDemo,
            onRetry = viewModel::refresh,
            onLogout = { if (isDemo) authRepository.exitDemo() else viewModel.logout() },
        )
    }
}

@Composable
private fun DashboardContent(state: UiState<DashboardData>, onRetry: () -> Unit, isDemo: Boolean = false, onLogout: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                is UiState.Loading -> DashboardSkeleton()
                is UiState.Error -> DashboardError(state.message, onRetry)
                is UiState.Success -> DashboardSuccess(state.data, isDemo, onLogout)
            }
        }
    }
}

@Composable
private fun DashboardSuccess(data: DashboardData, isDemo: Boolean = false, onLogout: () -> Unit) {
    var showSettings by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    val themeRepo = rememberThemeRepositoryOrNull()
    val currentTheme by (themeRepo?.themeFlow ?: remember { MutableStateFlow(AppTheme.EMBER) }).collectAsState()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val now = remember { Clock.System.now().epochSeconds }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = navBottom + 96.dp),
    ) {
        // ── Character header ──────────────────────────────────────────────────
        val glowColor = MaterialTheme.colorScheme.onPrimaryContainer
        Row(verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = data.portraitUrl,
                contentDescription = "${data.name} portrait",
                modifier = Modifier
                    .size(76.dp)
                    .drawBehind {
                        // Soft glow ring behind the portrait — same filled-disc
                        // technique as GlowCard, drawn before the clip so it bleeds
                        // past the circle instead of being cut off. Must be filled,
                        // not stroked, or it's too thin to read as glow.
                        listOf(2.dp to 0.28f, 5.dp to 0.13f).forEach { (inset, alpha) ->
                            drawCircle(
                                color = glowColor.copy(alpha = alpha),
                                radius = size.minDimension / 2f + inset.toPx(),
                            )
                        }
                    }
                    .clip(CircleShape)
                    .border(1.5.dp, glowColor, CircleShape),
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f).padding(top = 2.dp)) {
                Text(data.name, style = MaterialTheme.typography.headlineSmall)
                if (data.corporationName.isNotEmpty()) {
                    Text(
                        text = data.corporationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(5.dp))
                    SecurityStatusBadge(data.securityStatus)
                }
            }
            IconButton(onClick = {
                showSettings = true
                showAppearance = false
            }) {
                Icon(
                    imageVector = EveIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Stats row: ISK + SP ───────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                label = "ISK Balance",
                value = data.iskBalance.formatIsk(),
                modifier = Modifier.weight(1f),
                isGold = true,
            )
            StatCard(
                label = "Skill Points",
                value = if (data.totalSp > 0L) data.totalSp.formatSp() else "—",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Training card ─────────────────────────────────────────────────────
        GlowCard(Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Training",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                )
                if (data.activeSkill != null) {
                    ActiveSkillProgressSection(
                        entry = data.activeSkill,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = "No skill training — open the EVE client to start a queue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // ── Recent activity ───────────────────────────────────────────────────
        if (data.walletJournal.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            GlowCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Recent activity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    val gain = currentTheme.gainColor
                    data.walletJournal.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(Modifier.padding(vertical = 1.dp))
                        WalletJournalRow(entry = entry, now = now, gainColor = gain)
                    }
                }
            }
        }
    }

    if (showSettings) {
        DashboardSettingsSheet(
            isDemo = isDemo,
            showAppearance = showAppearance,
            currentTheme = currentTheme,
            onBack = { showAppearance = false },
            onShowAppearance = { showAppearance = true },
            onThemeChange = { themeRepo?.current = it },
            onDismissRequest = { if (showAppearance) showAppearance = false else showSettings = false },
            onLogoutClick = {
                showSettings = false
                onLogout()
            },
        )
    }
}

// ── Settings sheet — two-page: menu → appearance ──────────────────────────────
@Composable
private fun DashboardSettingsSheet(
    isDemo: Boolean,
    showAppearance: Boolean,
    currentTheme: AppTheme,
    onBack: () -> Unit,
    onShowAppearance: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(Modifier.fillMaxWidth().padding(bottom = 36.dp)) {
            if (showAppearance) {
                // ── Appearance page ───────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text("Appearance", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                AppTheme.entries.forEach { theme ->
                    ThemeRow(
                        theme = theme,
                        selected = currentTheme == theme,
                        onClick = { onThemeChange(theme) },
                    )
                    if (theme != AppTheme.entries.last()) {
                        HorizontalDivider(Modifier.padding(start = 64.dp, end = 20.dp))
                    }
                }
            } else {
                // ── Main menu ─────────────────────────────────────────────────
                SettingsRow(label = "Appearance", chevron = true, onClick = onShowAppearance)
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                SettingsRow(
                    label = if (isDemo) "Exit Demo Mode" else "Log out",
                    color = MaterialTheme.colorScheme.error,
                    onClick = onLogoutClick,
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, isGold: Boolean = false) {
    GlowCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            // Glow reserved for the hero number (ISK balance) — Skill Points stays
            // neutral, same hierarchy the isGold flag already expressed via colour.
            val glowShadow = if (isGold) {
                Shadow(color = MaterialTheme.colorScheme.onPrimaryContainer, blurRadius = 18f)
            } else {
                null
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(shadow = glowShadow),
                color = if (isGold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SecurityStatusBadge(status: Double) {
    val color = when {
        status < 0.0 -> MaterialTheme.colorScheme.error
        status >= 5.0 -> Color(0xFF3FB950)
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.75.dp, color.copy(alpha = 0.5f)),
    ) {
        Text(
            text = status.formatSecurityStatus(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun WalletJournalRow(entry: WalletJournalEntry, now: Long, gainColor: Color) {
    val isIncome = entry.amount >= 0.0
    val absAmount = abs(entry.amount)
    val amountText = if (isIncome) "+${absAmount.formatIsk()}" else "-${absAmount.formatIsk()}"
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = entry.description.ifEmpty { entry.displayName },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                entry.dateEpochSeconds.relativeTime(now),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isIncome) gainColor else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DashboardSkeleton() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(76.dp).clip(CircleShape).shimmer())
            Spacer(Modifier.width(13.dp))
            Column {
                Box(Modifier.width(140.dp).height(24.dp).shimmer())
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(100.dp).height(14.dp).shimmer())
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).height(76.dp).shimmer())
            Box(Modifier.weight(1f).height(76.dp).shimmer())
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(90.dp).shimmer())
    }
}

@Composable
private fun DashboardError(message: String, onRetry: () -> Unit) = ErrorState(message, onRetry)

// ── Settings sheet composables ────────────────────────────────────────────────

@Composable
private fun SettingsRow(label: String, color: Color = MaterialTheme.colorScheme.onSurface, chevron: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.weight(1f),
        )
        if (chevron) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemeRow(theme: AppTheme, selected: Boolean, onClick: () -> Unit) {
    val accent = theme.previewColor
    val dotBg = if (theme == AppTheme.AMOLED) Color(0xFF000000) else accent.copy(alpha = 0.15f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(dotBg)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) accent else accent.copy(alpha = 0.4f),
                    shape = CircleShape,
                ),
        )
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Text(text = "✓", color = accent, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ── Formatters ────────────────────────────────────────────────────────────────

// ISK formatter — pure Kotlin, no JVM String.format()
private fun Double.formatIsk(): String = when {
    this >= 1e9 -> "${roundTo2dp(this / 1e9)}B ISK"
    this >= 1e6 -> "${roundTo2dp(this / 1e6)}M ISK"
    this >= 1e3 -> "${roundTo2dp(this / 1e3)}K ISK"
    else -> "${toLong()} ISK"
}

private fun Long.formatSp(): String = when {
    this >= 1_000_000L -> "${roundTo2dp(this / 1_000_000.0)}M SP"
    this >= 1_000L -> "${roundTo2dp(this / 1_000.0)}K SP"
    else -> "$this SP"
}

private fun Double.formatSecurityStatus(): String {
    val sign = if (this >= 0.0) "+" else "-"
    val tenths = round(abs(this) * 10.0).toLong()
    return "$sign${tenths / 10}.${tenths % 10}"
}

private fun Long.relativeTime(nowEpochSeconds: Long): String {
    val diff = nowEpochSeconds - this
    return when {
        diff < 60L -> "just now"
        diff < 3_600L -> "${diff / 60}m ago"
        diff < 86_400L -> "${diff / 3_600}h ago"
        else -> "${diff / 86_400}d ago"
    }
}

private fun roundTo2dp(value: Double): String {
    val rounded = round(value * 100.0) / 100.0
    val i = rounded.toLong()
    val f = abs(round((rounded - i) * 100.0)).toLong()
    return "$i.${f.toString().padStart(2, '0')}"
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewSkill = SkillQueueEntry(
    queuePosition = 1, characterId = 0L, skillId = 3456,
    skillName = "Coherent Ore Processing", finishedLevel = 5,
    startSp = 24_000, finishSp = 1_280_000,
    startDate = 1_752_000_000L, finishDate = 4_000_000_000L,
)

private val previewJournal = listOf(
    WalletJournalEntry(1, "bounty_prizes", 45_000_000.0, 1_752_000_000L - 3_600),
    WalletJournalEntry(2, "market_transaction", -12_500_000.0, 1_752_000_000L - 7_200),
    WalletJournalEntry(3, "pi_export_tax", -890_000.0, 1_752_000_000L - 86_400),
)

@Preview @Composable
private fun DashboardPreviewSuccess() = MaterialTheme(EmberColorScheme) {
    DashboardContent(
        state = UiState.Success(
            DashboardData(
                name = "ToWFurious",
                portraitUrl = "",
                iskBalance = 99_970_000.0,
                securityStatus = 2.3,
                corporationName = "Caldari Navy",
                totalSp = 142_500_000L,
                activeSkill = previewSkill,
                walletJournal = previewJournal,
            ),
        ),
        onRetry = {},
        onLogout = {},
    )
}

@Preview @Composable
private fun DashboardPreviewLoading() = MaterialTheme(EmberColorScheme) {
    DashboardContent(state = UiState.Loading, onRetry = {}, onLogout = {})
}

@Preview @Composable
private fun DashboardPreviewError() = MaterialTheme(EmberColorScheme) {
    DashboardContent(state = UiState.Error("Not authenticated"), onRetry = {}, onLogout = {})
}
