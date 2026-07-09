package com.podforeve.tracker

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.podforeve.tracker.ui.icon.EveIcons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.ui.component.PodSplashScreen
import com.podforeve.tracker.ui.screen.DashboardScreen
import com.podforeve.tracker.ui.screen.JobsScreen
import com.podforeve.tracker.ui.screen.LoginScreen
import com.podforeve.tracker.ui.screen.PiScreen
import com.podforeve.tracker.ui.screen.SkillsScreen
import com.podforeve.tracker.ui.theme.EmberColorScheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.mp.KoinPlatform.getKoin

// Root Composable. Ember theme. Auth gate: Loading→Splash, Unauth→Login, Auth→MainApp.
@Composable
fun App() {
    val authRepository: AuthRepository = remember { getKoin().get() }
    val authState by authRepository.authState.collectAsState()
    var splashAnimDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authState is AuthState.Loading) {
            authRepository.getValidAccessToken()
        }
    }

    MaterialTheme(colorScheme = EmberColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            val authResolved = authState !is AuthState.Loading
            if (!splashAnimDone || !authResolved) {
                SplashScreen(onFinished = { splashAnimDone = true })
            } else {
                when (authState) {
                    is AuthState.Unauthenticated,
                    is AuthState.Error -> LoginScreen()

                    is AuthState.Authenticated -> MainApp()
                    else -> Unit
                }
            }
        }
    }
}

// ── Splash ────────────────────────────────────────────────────────────────────

@Composable
private fun SplashScreen(onFinished: () -> Unit) = PodSplashScreen(onFinished = onFinished)

// ── Main app ──────────────────────────────────────────────────────────────────

private val tabs = listOf(DashboardTab, SkillsTab, PiTab, JobsTab)

private val tabIcon: Map<Tab, ImageVector> = mapOf(
    DashboardTab to EveIcons.CharacterSheet,
    SkillsTab to EveIcons.Skills,
    PiTab to EveIcons.Planets,
    JobsTab to EveIcons.Industry,
)

@Composable
private fun MainApp() {
    val hazeState = remember { HazeState() }
    TabNavigator(DashboardTab) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().hazeSource(hazeState)) { CurrentTab() }
            PodNavBar(modifier = Modifier.align(Alignment.BottomCenter), hazeState = hazeState)
        }
    }
}

// ── Pill nav bar ──────────────────────────────────────────────────────────────

@Composable
private fun PodNavBar(modifier: Modifier = Modifier, hazeState: HazeState? = null) {
    val tabNavigator = LocalTabNavigator.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (hazeState != null)
                        Modifier.hazeEffect(state = hazeState, style = HazeStyle(blurRadius = 24.dp, tint = null))
                    else Modifier
                ),
            color           = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation  = 0.dp,
            shadowElevation = 8.dp,
            shape           = RoundedCornerShape(50),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    PodNavItem(
                        icon = tabIcon[tab]!!,
                        label = tab.options.title,
                        selected = tabNavigator.current == tab,
                        onClick = { tabNavigator.current = tab },
                    )
                }
            }
        }
    }
}

@Composable
private fun PodNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val pillColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "pill",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "content",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(pillColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
    }
}

// ── Tabs ──────────────────────────────────────────────────────────────────────

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 0u, title = "Dashboard") }

    @Composable
    override fun Content() = DashboardScreen().Content()
}

object SkillsTab : Tab {
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 1u, title = "Skills") }

    @Composable
    override fun Content() = SkillsScreen().Content()
}

object PiTab : Tab {
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 2u, title = "PI") }

    @Composable
    override fun Content() = PiScreen().Content()
}

object JobsTab : Tab {
    override val options: TabOptions
        @Composable get() = remember { TabOptions(index = 3u, title = "Jobs") }

    @Composable
    override fun Content() = JobsScreen().Content()
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PodNavBarPreview() = MaterialTheme(EmberColorScheme) {
    Surface(color = MaterialTheme.colorScheme.background) {
        TabNavigator(DashboardTab) {
            PodNavBar()
        }
    }
}
