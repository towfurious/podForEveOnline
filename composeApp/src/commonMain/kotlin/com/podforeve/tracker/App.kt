package com.podforeve.tracker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.podforeve.tracker.auth.AuthRepository
import com.podforeve.tracker.auth.model.AuthState
import com.podforeve.tracker.ui.screen.DashboardScreen
import com.podforeve.tracker.ui.screen.JobsScreen
import com.podforeve.tracker.ui.screen.LoginScreen
import com.podforeve.tracker.ui.screen.PiScreen
import com.podforeve.tracker.ui.screen.SkillsScreen
import org.koin.core.context.GlobalContext

// Root Composable. Dark M3 theme per [[ADR-002 - Material 3 Dark Default]].
// Auth gate: shows LoginScreen until authenticated, then the 4-tab main app.
@Composable
fun App() {
    val authRepository: AuthRepository = remember { GlobalContext.get().get() }
    val authState by authRepository.authState.collectAsState()

    // On startup: if a stored refresh token exists, state starts as Loading.
    // Trigger a silent token restore to resolve to Authenticated or Unauthenticated.
    LaunchedEffect(Unit) {
        if (authState is AuthState.Loading) {
            authRepository.getValidAccessToken()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        when (authState) {
            is AuthState.Loading          -> SplashScreen()
            is AuthState.Unauthenticated,
            is AuthState.Error            -> LoginScreen()
            is AuthState.Authenticated    -> MainApp()
        }
    }
}

// ── Splash ────────────────────────────────────────────────────────────────────

@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ── Main app (4-tab navigation) ───────────────────────────────────────────────

@Composable
private fun MainApp() {
    TabNavigator(DashboardTab) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val tabNavigator = LocalTabNavigator.current
                    listOf(DashboardTab, SkillsTab, PiTab, JobsTab).forEach { tab ->
                        NavigationBarItem(
                            selected = tabNavigator.current == tab,
                            onClick = { tabNavigator.current = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        DashboardTab -> Icons.Default.Home
                                        SkillsTab    -> Icons.Default.Star
                                        PiTab        -> Icons.Default.Public
                                        else         -> Icons.Default.DateRange
                                    },
                                    contentDescription = tab.options.title,
                                )
                            },
                            label = { Text(tab.options.title) },
                        )
                    }
                }
            },
        ) {
            CurrentTab()
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
