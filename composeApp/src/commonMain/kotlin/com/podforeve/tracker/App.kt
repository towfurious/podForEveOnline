package com.podforeve.tracker

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.platform.rememberHapticFeedback
import com.podforeve.tracker.ui.component.PodSplashScreen
import com.podforeve.tracker.ui.icon.EveIcons
import com.podforeve.tracker.ui.screen.DashboardScreen
import com.podforeve.tracker.ui.screen.JobsScreen
import com.podforeve.tracker.ui.screen.LoginScreen
import com.podforeve.tracker.ui.screen.PiScreen
import com.podforeve.tracker.ui.screen.SkillsScreen
import com.podforeve.tracker.ui.theme.EmberColorScheme
import com.podforeve.tracker.ui.theme.ThemeRepository
import com.podforeve.tracker.ui.theme.toColorScheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun App() {
    val authRepository: AuthRepository = remember { getKoin().get() }
    val themeRepo: ThemeRepository = remember { getKoin().get() }
    val authState by authRepository.authState.collectAsState()
    val appTheme by themeRepo.themeFlow.collectAsState()
    var splashAnimDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authState is AuthState.Loading) {
            authRepository.getValidAccessToken()
        }
    }

    MaterialTheme(colorScheme = appTheme.toColorScheme()) {
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
                    is AuthState.Error,
                    -> LoginScreen()
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
        val tabNavigator = LocalTabNavigator.current
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().hazeSource(hazeState)) {
                AnimatedContent(
                    targetState = tabNavigator.current,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                    },
                    contentKey = { it.key },
                    label = "tabTransition",
                ) { tab ->
                    tab.Content()
                }
            }
            PodNavBar(modifier = Modifier.align(Alignment.BottomCenter), hazeState = hazeState)
        }
    }
}

// ── Pill nav bar ──────────────────────────────────────────────────────────────

@Composable
private fun PodNavBar(modifier: Modifier = Modifier, hazeState: HazeState? = null) {
    val tabNavigator = LocalTabNavigator.current
    val selectedIndex = tabs.indexOfFirst { it == tabNavigator.current }.coerceAtLeast(0)
    val density = LocalDensity.current

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
                .clip(RoundedCornerShape(50))
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState, style = HazeStyle(blurRadius = 24.dp, tint = null))
                    } else {
                        Modifier
                    },
                ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(50),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                val itemWidth = maxWidth / tabs.size
                val pillOffsetX by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
                    label = "pillSlide",
                )
                var pillHeight by remember { mutableIntStateOf(0) }
                val pillHeightDp = with(density) { pillHeight.toDp() }

                // Sliding pill — drawn first so it appears behind items
                if (pillHeight > 0) {
                    Box(
                        Modifier
                            .offset(x = pillOffsetX)
                            .width(itemWidth)
                            .height(pillHeightDp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                }

                // Nav items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { pillHeight = it.height },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val haptic = rememberHapticFeedback()
                    tabs.forEach { tab ->
                        PodNavItem(
                            modifier = Modifier.weight(1f),
                            icon = tabIcon[tab]!!,
                            label = tab.options.title,
                            selected = tabNavigator.current == tab,
                            onClick = {
                                haptic()
                                tabNavigator.current = tab
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodNavItem(modifier: Modifier = Modifier, icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "content",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(36.dp),
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
