package com.podforeve.tracker.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Persistent indicator while AuthState.Demo is active, so sample data is never mistaken
// for a real account — see [[ADR-022 - Demo Mode]]. Unlike OfflineBanner this has no
// AnimatedVisibility: entering/exiting Demo Mode already swaps the whole screen
// (LoginScreen ↔ MainApp), so an extra transition here would just fight that one.
@Composable
fun DemoModeBanner(visible: Boolean, onExit: () -> Unit, modifier: Modifier = Modifier) {
    if (!visible) return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Demo Mode — sample data",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onExit) {
                Text("Exit", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
