package com.podforeve.tracker.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ── Palette ──────────────────────────────────────────────────────────────────
// Derived from the capsule icon gradient:
//   yellow #F5D060 → amber #DF7820 → red #BF3A20 → magenta #641C48 → void #1C0A30

private val Orange      = Color(0xFFFF8040)
private val OrangeOnP   = Color(0xFF1A0800)
private val OrangeCont  = Color(0xFF4A1800)
private val OrangeOnC   = Color(0xFFFFBB94)

private val Red         = Color(0xFFD4603A)
private val RedOnS      = Color(0xFF1A0800)
private val RedCont     = Color(0xFF3D1500)
private val RedOnC      = Color(0xFFFFB59A)

private val Amber       = Color(0xFFE8A020)
private val AmberOnT    = Color(0xFF1A0800)
private val AmberCont   = Color(0xFF3D2400)
private val AmberOnC    = Color(0xFFFFD088)

private val Bg          = Color(0xFF0E0908)
private val OnBg        = Color(0xFFF0E8E0)
private val Surface     = Color(0xFF170D0A)
private val OnSurface   = Color(0xFFF0E8E0)
private val SurfVar     = Color(0xFF2A1610)
private val OnSurfVar   = Color(0xFFC4A898)
private val Outline     = Color(0xFF7A5244)
private val OutlineVar  = Color(0xFF3D2018)

// Surface container family — used by Card, BottomSheet, NavigationBar, etc. in M3 1.2+
private val SurfContLowest  = Color(0xFF0A0604)
private val SurfContLow     = Color(0xFF1C1008)  // Card background
private val SurfCont        = Color(0xFF221408)
private val SurfContHigh    = Color(0xFF2A1A0C)
private val SurfContHighest = Color(0xFF321F10)
private val SurfDim         = Color(0xFF0E0908)
private val SurfBright      = Color(0xFF352014)

val EmberColorScheme = darkColorScheme(
    primary                = Orange,
    onPrimary              = OrangeOnP,
    primaryContainer       = OrangeCont,
    onPrimaryContainer     = OrangeOnC,

    secondary              = Red,
    onSecondary            = RedOnS,
    secondaryContainer     = RedCont,
    onSecondaryContainer   = RedOnC,

    tertiary               = Amber,
    onTertiary             = AmberOnT,
    tertiaryContainer      = AmberCont,
    onTertiaryContainer    = AmberOnC,

    background             = Bg,
    onBackground           = OnBg,
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfVar,
    onSurfaceVariant       = OnSurfVar,
    surfaceDim             = SurfDim,
    surfaceBright          = SurfBright,
    surfaceContainerLowest = SurfContLowest,
    surfaceContainerLow    = SurfContLow,
    surfaceContainer       = SurfCont,
    surfaceContainerHigh   = SurfContHigh,
    surfaceContainerHighest= SurfContHighest,
    outline                = Outline,
    outlineVariant         = OutlineVar,

    error                  = Color(0xFFFFB4AB),
    onError                = Color(0xFF690005),
    errorContainer         = Color(0xFF93000A),
    onErrorContainer       = Color(0xFFFFDAD6),

    inverseSurface         = OnBg,
    inverseOnSurface       = Color(0xFF1A0D08),
    inversePrimary         = Color(0xFF8C3A10),
    scrim                  = Color(0xFF000000),
)
