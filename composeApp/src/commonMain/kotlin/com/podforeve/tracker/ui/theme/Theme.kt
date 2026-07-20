package com.podforeve.tracker.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// AppTheme — one entry per colour scheme; persisted by ThemeRepository
// ─────────────────────────────────────────────────────────────────────────────

enum class AppTheme(val displayName: String) {
    EMBER("Minmatar"),
    AMARR("Amarr"),
    CALDARI("Caldari"),
    GALLENTE("Gallente"),
    AMOLED("AMOLED"),
}

fun AppTheme.toColorScheme() = when (this) {
    AppTheme.EMBER -> EmberColorScheme
    AppTheme.AMARR -> AmarrColorScheme
    AppTheme.CALDARI -> CaldariColorScheme
    AppTheme.GALLENTE -> GallenteColorScheme
    AppTheme.AMOLED -> AmoledColorScheme
}

// Dot colour shown in the theme picker — does NOT need to match the actual
// primary exactly, just needs to read as "this faction" at a glance.
val AppTheme.previewColor: Color
    get() = when (this) {
        AppTheme.EMBER -> Color(0xFFFF8040)
        AppTheme.AMARR -> Color(0xFFD4A820)
        AppTheme.CALDARI -> Color(0xFF4888E0)
        AppTheme.GALLENTE -> Color(0xFF30B858)
        AppTheme.AMOLED -> Color(0xFF4090FF)
    }

// Wallet/job "gain" colour — universally green, except Gallente whose own
// primary IS emerald green; amber keeps the same positive-signal meaning
// there without sitting on top of the theme's own accent.
val AppTheme.gainColor: Color
    get() = if (this == AppTheme.GALLENTE) Color(0xFFE0B84A) else Color(0xFF3FB950)

// ─────────────────────────────────────────────────────────────────────────────
// EMBER — Minmatar. Capsule icon gradient: yellow → amber → red → magenta → void
// ─────────────────────────────────────────────────────────────────────────────

private val EmOrange = Color(0xFFFF8040)
private val EmOrangeOnP = Color(0xFF1A0800)
private val EmOrangeCont = Color(0xFF4A1800)
private val EmOrangeOnC = Color(0xFFFFBB94)

private val EmRed = Color(0xFFD4603A)
private val EmRedOnS = Color(0xFF1A0800)
private val EmRedCont = Color(0xFF3D1500)
private val EmRedOnC = Color(0xFFFFB59A)

private val EmAmber = Color(0xFFE8A020)
private val EmAmberOnT = Color(0xFF1A0800)
private val EmAmberCont = Color(0xFF3D2400)
private val EmAmberOnC = Color(0xFFFFD088)

private val EmBg = Color(0xFF0E0908)
private val EmOnBg = Color(0xFFF0E8E0)
private val EmSurface = Color(0xFF170D0A)
private val EmOnSurface = Color(0xFFF0E8E0)
private val EmSurfVar = Color(0xFF2A1610)
private val EmOnSurfVar = Color(0xFFC4A898)
private val EmOutline = Color(0xFF7A5244)
private val EmOutlineVar = Color(0xFF3D2018)
private val EmSurfCLowest = Color(0xFF0A0604)
private val EmSurfCLow = Color(0xFF1C1008)
private val EmSurfC = Color(0xFF221408)
private val EmSurfCHigh = Color(0xFF2A1A0C)
private val EmSurfCHighest = Color(0xFF321F10)
private val EmSurfDim = Color(0xFF0E0908)
private val EmSurfBright = Color(0xFF352014)

val EmberColorScheme = darkColorScheme(
    primary = EmOrange,
    onPrimary = EmOrangeOnP,
    primaryContainer = EmOrangeCont,
    onPrimaryContainer = EmOrangeOnC,
    secondary = EmRed,
    onSecondary = EmRedOnS,
    secondaryContainer = EmRedCont,
    onSecondaryContainer = EmRedOnC,
    tertiary = EmAmber,
    onTertiary = EmAmberOnT,
    tertiaryContainer = EmAmberCont,
    onTertiaryContainer = EmAmberOnC,
    background = EmBg,
    onBackground = EmOnBg,
    surface = EmSurface,
    onSurface = EmOnSurface,
    surfaceVariant = EmSurfVar,
    onSurfaceVariant = EmOnSurfVar,
    surfaceDim = EmSurfDim,
    surfaceBright = EmSurfBright,
    surfaceContainerLowest = EmSurfCLowest,
    surfaceContainerLow = EmSurfCLow,
    surfaceContainer = EmSurfC,
    surfaceContainerHigh = EmSurfCHigh,
    surfaceContainerHighest = EmSurfCHighest,
    outline = EmOutline,
    outlineVariant = EmOutlineVar,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = EmOnBg,
    inverseOnSurface = Color(0xFF1A0D08),
    inversePrimary = Color(0xFF8C3A10),
    scrim = Color(0xFF000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// AMARR — Imperial. Golden scripture, purple void, ancient crystal spires.
// ─────────────────────────────────────────────────────────────────────────────

private val AmGold = Color(0xFFD4A820)
private val AmGoldOnP = Color(0xFF1A0E00)
private val AmGoldCont = Color(0xFF3D2A00)
private val AmGoldOnC = Color(0xFFFFE070)

private val AmCopper = Color(0xFFB05010)
private val AmCopperOnS = Color(0xFF180A00)
private val AmCopperCont = Color(0xFF3A1800)
private val AmCopperOnC = Color(0xFFFFB870)

private val AmPurple = Color(0xFF9860C0)
private val AmPurpleOnT = Color(0xFF1A0028)
private val AmPurpleCont = Color(0xFF3A0068)
private val AmPurpleOnC = Color(0xFFE0B8FF)

private val AmBg = Color(0xFF0A0608)
private val AmOnBg = Color(0xFFEEE4D8)
private val AmSurface = Color(0xFF160A14)
private val AmOnSurface = Color(0xFFEEE4D8)
private val AmSurfVar = Color(0xFF2C1428)
private val AmOnSurfVar = Color(0xFFC8A0BC)
private val AmOutline = Color(0xFF7A4870)
private val AmOutlineVar = Color(0xFF3C1C38)
private val AmSurfCLowest = Color(0xFF080406)
private val AmSurfCLow = Color(0xFF1C0E18)
private val AmSurfC = Color(0xFF22121E)
private val AmSurfCHigh = Color(0xFF2C1828)
private val AmSurfCHighest = Color(0xFF361C2E)
private val AmSurfDim = Color(0xFF0A0608)
private val AmSurfBright = Color(0xFF3A1C34)

val AmarrColorScheme = darkColorScheme(
    primary = AmGold,
    onPrimary = AmGoldOnP,
    primaryContainer = AmGoldCont,
    onPrimaryContainer = AmGoldOnC,
    secondary = AmCopper,
    onSecondary = AmCopperOnS,
    secondaryContainer = AmCopperCont,
    onSecondaryContainer = AmCopperOnC,
    tertiary = AmPurple,
    onTertiary = AmPurpleOnT,
    tertiaryContainer = AmPurpleCont,
    onTertiaryContainer = AmPurpleOnC,
    background = AmBg,
    onBackground = AmOnBg,
    surface = AmSurface,
    onSurface = AmOnSurface,
    surfaceVariant = AmSurfVar,
    onSurfaceVariant = AmOnSurfVar,
    surfaceDim = AmSurfDim,
    surfaceBright = AmSurfBright,
    surfaceContainerLowest = AmSurfCLowest,
    surfaceContainerLow = AmSurfCLow,
    surfaceContainer = AmSurfC,
    surfaceContainerHigh = AmSurfCHigh,
    surfaceContainerHighest = AmSurfCHighest,
    outline = AmOutline,
    outlineVariant = AmOutlineVar,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = AmOnBg,
    inverseOnSurface = Color(0xFF1A0D0A),
    inversePrimary = Color(0xFF7A5800),
    scrim = Color(0xFF000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// CALDARI — Corporate State. Cold blue steel, neon precision, efficiency above all.
// ─────────────────────────────────────────────────────────────────────────────

private val CaBlue = Color(0xFF4888E0)
private val CaBlueOnP = Color(0xFF001228)
private val CaBlueCont = Color(0xFF002060)
private val CaBlueOnC = Color(0xFFAAC8FF)

private val CaSteel = Color(0xFF5080A8)
private val CaSteelOnS = Color(0xFF001018)
private val CaSteelCont = Color(0xFF0C2040)
private val CaSteelOnC = Color(0xFF9CC0E0)

private val CaCyan = Color(0xFF10A8C0)
private val CaCyanOnT = Color(0xFF001C24)
private val CaCyanCont = Color(0xFF003848)
private val CaCyanOnC = Color(0xFF80E0F8)

private val CaBg = Color(0xFF06080E)
private val CaOnBg = Color(0xFFD8E0F0)
private val CaSurface = Color(0xFF0C1018)
private val CaOnSurface = Color(0xFFD8E0F0)
private val CaSurfVar = Color(0xFF14202E)
private val CaOnSurfVar = Color(0xFF8898B8)
private val CaOutline = Color(0xFF384870)
private val CaOutlineVar = Color(0xFF1A2840)
private val CaSurfCLowest = Color(0xFF04060C)
private val CaSurfCLow = Color(0xFF0E1420)
private val CaSurfC = Color(0xFF121824)
private val CaSurfCHigh = Color(0xFF182030)
private val CaSurfCHighest = Color(0xFF1E2838)
private val CaSurfDim = Color(0xFF06080E)
private val CaSurfBright = Color(0xFF242E40)

val CaldariColorScheme = darkColorScheme(
    primary = CaBlue,
    onPrimary = CaBlueOnP,
    primaryContainer = CaBlueCont,
    onPrimaryContainer = CaBlueOnC,
    secondary = CaSteel,
    onSecondary = CaSteelOnS,
    secondaryContainer = CaSteelCont,
    onSecondaryContainer = CaSteelOnC,
    tertiary = CaCyan,
    onTertiary = CaCyanOnT,
    tertiaryContainer = CaCyanCont,
    onTertiaryContainer = CaCyanOnC,
    background = CaBg,
    onBackground = CaOnBg,
    surface = CaSurface,
    onSurface = CaOnSurface,
    surfaceVariant = CaSurfVar,
    onSurfaceVariant = CaOnSurfVar,
    surfaceDim = CaSurfDim,
    surfaceBright = CaSurfBright,
    surfaceContainerLowest = CaSurfCLowest,
    surfaceContainerLow = CaSurfCLow,
    surfaceContainer = CaSurfC,
    surfaceContainerHigh = CaSurfCHigh,
    surfaceContainerHighest = CaSurfCHighest,
    outline = CaOutline,
    outlineVariant = CaOutlineVar,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = CaOnBg,
    inverseOnSurface = Color(0xFF080E1A),
    inversePrimary = Color(0xFF003A90),
    scrim = Color(0xFF000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// GALLENTE — Federation. Emerald biomes, democratic warmth, living ships.
// ─────────────────────────────────────────────────────────────────────────────

private val GaEmerald = Color(0xFF30B858)
private val GaEmerOnP = Color(0xFF001A0A)
private val GaEmerCont = Color(0xFF003818)
private val GaEmerOnC = Color(0xFF78FFA8)

private val GaTeal = Color(0xFF20A080)
private val GaTealOnS = Color(0xFF001A12)
private val GaTealCont = Color(0xFF003828)
private val GaTealOnC = Color(0xFF78E8C8)

private val GaLime = Color(0xFF80C030)
private val GaLimeOnT = Color(0xFF101800)
private val GaLimeCont = Color(0xFF203800)
private val GaLimeOnC = Color(0xFFC8E878)

private val GaBg = Color(0xFF060A07)
private val GaOnBg = Color(0xFFD8EED8)
private val GaSurface = Color(0xFF0A1208)
private val GaOnSurface = Color(0xFFD8EED8)
private val GaSurfVar = Color(0xFF142018)
private val GaOnSurfVar = Color(0xFF88A890)
private val GaOutline = Color(0xFF387848)
private val GaOutlineVar = Color(0xFF183C28)
private val GaSurfCLowest = Color(0xFF040804)
private val GaSurfCLow = Color(0xFF0C140C)
private val GaSurfC = Color(0xFF101A10)
private val GaSurfCHigh = Color(0xFF182018)
private val GaSurfCHighest = Color(0xFF1E2820)
private val GaSurfDim = Color(0xFF060A07)
private val GaSurfBright = Color(0xFF283828)

val GallenteColorScheme = darkColorScheme(
    primary = GaEmerald,
    onPrimary = GaEmerOnP,
    primaryContainer = GaEmerCont,
    onPrimaryContainer = GaEmerOnC,
    secondary = GaTeal,
    onSecondary = GaTealOnS,
    secondaryContainer = GaTealCont,
    onSecondaryContainer = GaTealOnC,
    tertiary = GaLime,
    onTertiary = GaLimeOnT,
    tertiaryContainer = GaLimeCont,
    onTertiaryContainer = GaLimeOnC,
    background = GaBg,
    onBackground = GaOnBg,
    surface = GaSurface,
    onSurface = GaOnSurface,
    surfaceVariant = GaSurfVar,
    onSurfaceVariant = GaOnSurfVar,
    surfaceDim = GaSurfDim,
    surfaceBright = GaSurfBright,
    surfaceContainerLowest = GaSurfCLowest,
    surfaceContainerLow = GaSurfCLow,
    surfaceContainer = GaSurfC,
    surfaceContainerHigh = GaSurfCHigh,
    surfaceContainerHighest = GaSurfCHighest,
    outline = GaOutline,
    outlineVariant = GaOutlineVar,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = GaOnBg,
    inverseOnSurface = Color(0xFF080E08),
    inversePrimary = Color(0xFF006020),
    scrim = Color(0xFF000000),
)

// ─────────────────────────────────────────────────────────────────────────────
// AMOLED — True black. Saves battery on OLED; electric blue accent cuts through.
// ─────────────────────────────────────────────────────────────────────────────

private val AoBlue = Color(0xFF4090FF)
private val AoBlueOnP = Color(0xFF000A20)
private val AoBlueCont = Color(0xFF001A58)
private val AoBlueOnC = Color(0xFFA8C8FF)

private val AoSilver = Color(0xFF7090B0)
private val AoSilverOnS = Color(0xFF081018)
private val AoSilverCont = Color(0xFF182030)
private val AoSilverOnC = Color(0xFFC0D8F0)

private val AoViolet = Color(0xFF8860E0)
private val AoVioletOnT = Color(0xFF100018)
private val AoVioletCont = Color(0xFF280058)
private val AoVioletOnC = Color(0xFFD4B8FF)

private val AoBg = Color(0xFF000000)
private val AoOnBg = Color(0xFFE0E0F0)
private val AoSurface = Color(0xFF08080E)
private val AoOnSurface = Color(0xFFE0E0F0)
private val AoSurfVar = Color(0xFF10101C)
private val AoOnSurfVar = Color(0xFF9090A8)
private val AoOutline = Color(0xFF404060)
private val AoOutlineVar = Color(0xFF20203C)
private val AoSurfCLowest = Color(0xFF020202)
private val AoSurfCLow = Color(0xFF0C0C16)
private val AoSurfC = Color(0xFF10101A)
private val AoSurfCHigh = Color(0xFF181820)
private val AoSurfCHighest = Color(0xFF202028)
private val AoSurfDim = Color(0xFF000000)
private val AoSurfBright = Color(0xFF1C1C28)

val AmoledColorScheme = darkColorScheme(
    primary = AoBlue,
    onPrimary = AoBlueOnP,
    primaryContainer = AoBlueCont,
    onPrimaryContainer = AoBlueOnC,
    secondary = AoSilver,
    onSecondary = AoSilverOnS,
    secondaryContainer = AoSilverCont,
    onSecondaryContainer = AoSilverOnC,
    tertiary = AoViolet,
    onTertiary = AoVioletOnT,
    tertiaryContainer = AoVioletCont,
    onTertiaryContainer = AoVioletOnC,
    background = AoBg,
    onBackground = AoOnBg,
    surface = AoSurface,
    onSurface = AoOnSurface,
    surfaceVariant = AoSurfVar,
    onSurfaceVariant = AoOnSurfVar,
    surfaceDim = AoSurfDim,
    surfaceBright = AoSurfBright,
    surfaceContainerLowest = AoSurfCLowest,
    surfaceContainerLow = AoSurfCLow,
    surfaceContainer = AoSurfC,
    surfaceContainerHigh = AoSurfCHigh,
    surfaceContainerHighest = AoSurfCHighest,
    outline = AoOutline,
    outlineVariant = AoOutlineVar,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = AoOnBg,
    inverseOnSurface = Color(0xFF080808),
    inversePrimary = Color(0xFF003498),
    scrim = Color(0xFF000000),
)
