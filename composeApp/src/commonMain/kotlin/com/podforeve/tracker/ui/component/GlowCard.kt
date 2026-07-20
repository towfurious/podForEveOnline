package com.podforeve.tracker.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.podforeve.tracker.ui.theme.CaldariColorScheme
import com.podforeve.tracker.ui.theme.EmberColorScheme
import com.podforeve.tracker.ui.theme.GallenteColorScheme

// Drop-in replacement for Material3's Card carrying the "plasma conduit" neon-outline
// treatment: a primary-colour border with glow bleeding both outward (past the card's
// own bounds) and inward (hugging the border, fading toward the center) — a neon-tube
// look, not a spotlight in the middle of the card.
// No Modifier.blur — this is drawn per-instance via Canvas strokes so it stays cheap when
// reused inside scrolling lists (JobCard, PlanetCard). See ADR-017.
//
// Every colour comes from MaterialTheme.colorScheme, so this automatically matches
// whichever AppTheme is active — no theme-specific code here.
@Composable
fun GlowCard(modifier: Modifier = Modifier, cornerRadius: Dp = 6.dp, content: @Composable () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .drawBehind {
                // Outer glow: FILLED rounded rects, progressively larger and fainter,
                // drawn before the clip below so they bleed past the card's own bounds.
                // Must be filled, not stroked — a stroked outline at low alpha is too
                // thin to read as glow (this is what a first pass got wrong: the same
                // technique GradientProgressBar uses for its fill glow is filled rects,
                // not stroked rings).
                val baseRadiusPx = cornerRadius.toPx()
                // TUNE OUTER GLOW HERE: each pair is (reach, alpha). Raise the alpha
                // values for a brighter bleed, raise the dp values for a wider/softer
                // one, or add a 4th pair further out for a longer falloff tail.
                listOf(2.dp to 0.24f, 4.dp to 0.13f, 7.dp to 0.06f).forEach { (inset, alpha) ->
                    val insetPx = inset.toPx()
                    drawRoundRect(
                        color = primary.copy(alpha = alpha),
                        topLeft = Offset(-insetPx, -insetPx),
                        size = Size(size.width + insetPx * 2, size.height + insetPx * 2),
                        cornerRadius = CornerRadius(baseRadiusPx + insetPx),
                    )
                }
            }
            .clip(shape)
            .background(cardColor)
            .drawBehind {
                // Inner edge glow: strokes centered ON the border path, each half
                // clipped away by the .clip(shape) above — only the inward-facing
                // half survives. Layered widest/faintest under narrowest/brightest
                // so the light hugs the border and fades toward the center. This is
                // the Compose analog of the mockup's `inset` box-shadow; a first pass
                // used a center-out Brush.radialGradient here instead, which glows
                // brightest in the middle of the card — the opposite of what "glow on
                // the edges" means, and very visible as a stray blob on wide/tall cards.
                val baseRadiusPx = cornerRadius.toPx()
                // TUNE INNER GLOW HERE: each pair is (stroke width, alpha) — width/2 is
                // how deep that layer reaches inward from the border. Raise the alpha
                // values for a brighter hug, raise the widths to push the glow further
                // toward the card's center (watch it against content padding if so).
                listOf(18.dp to 0.05f, 10.dp to 0.10f, 4.dp to 0.18f).forEach { (width, alpha) ->
                    drawRoundRect(
                        color = primary.copy(alpha = alpha),
                        style = Stroke(width = width.toPx()),
                        cornerRadius = CornerRadius(baseRadiusPx),
                    )
                }
            }
            .drawBehind {
                // Crisp structural border, drawn last so it sits on top, sharp and bright.
                drawRoundRect(
                    color = primary,
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                )
            },
    ) {
        content()
    }
}

// ── Previews across visually distinct themes ────────────────────────────────────

@Preview
@Composable
private fun GlowCardPreviewEmber() = MaterialTheme(EmberColorScheme) {
    Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        GlowCard(Modifier.fillMaxWidth()) {
            Text("Ember", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview
@Composable
private fun GlowCardPreviewCaldari() = MaterialTheme(CaldariColorScheme) {
    Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        GlowCard(Modifier.fillMaxWidth()) {
            Text("Caldari", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview
@Composable
private fun GlowCardPreviewGallente() = MaterialTheme(GallenteColorScheme) {
    Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        GlowCard(Modifier.fillMaxWidth()) {
            Text("Gallente", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
