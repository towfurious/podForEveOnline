package com.podforeve.tracker.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

// Modifier-based shimmer per [[ADR-009 - UiState Sealed Class with Shimmer]].
// No extra library — pure Compose animation.
fun Modifier.shimmer(): Modifier = composed {
    val alpha by rememberInfiniteTransition(label = "shimmer")
        .animateFloat(
            initialValue = 0.25f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmer-alpha",
        )
    background(Color.White.copy(alpha = alpha))
}
