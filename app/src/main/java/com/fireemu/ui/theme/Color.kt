package com.fireemu.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// One neutral palette. Legacy names remain aliases so every screen uses the same finish.
val FireSpotNavyTop = Color(0xFF202020)
val FireSpotNavyMid = Color(0xFF101010)
val FireSpotNavyBottom = Color(0xFF080808)
val FireSpotBackgroundGradient = Brush.verticalGradient(listOf(FireSpotNavyMid, FireSpotNavyBottom))
val FireSpotGlassCard = Color(0xFF191919)
val FireSpotGlassBorder = Color(0xFF363636)
val FireSpotGlassCardHover = Color(0xFF292929)
val FireSpotTextPrimary = Color(0xFFF2F2F2)
val FireSpotTextSecondary = Color(0xFFAAAAAA)
val FireSpotSectionLabel = Color(0xFFBDBDBD)
val FireSpotTextSubtitle = Color(0xFF999999)
val FireSpotTitleCyan = FireSpotTextPrimary
val FireSpotAccentBlue = Color(0xFFF2F2F2)
val FireSpotAccentGlow = Color(0x22FFFFFF)
val FireSpotSearchFocusedBg = Color(0xFF303030)
val FireSpotResumeBadge = Color(0xFFE4E4E4)
val FireSpotBrandAmberTop = Color(0xFFF2F2F2)
val FireSpotBrandAmberBottom = Color(0xFFC0C0C0)
val FireSpotBrandAmberSolid = FireSpotTextPrimary
val FireSpotBrandGradient = Brush.verticalGradient(listOf(FireSpotBrandAmberTop, FireSpotBrandAmberBottom))
val FireSpotRailIdleIcon = Color(0xFF858585)
val FireSpotRailSelectedBg = Color(0xFF242424)
val FireSpotRailSelectedIcon = FireSpotTextPrimary
val ConsoleColorNes = Color(0xFFBDBDBD)
val ConsoleColorGb = Color(0xFFCBCBCB)
val ConsoleColorGba = Color(0xFFAEAEAE)
val ConsoleColorPs1 = Color(0xFFCBCBCB)
val ConsoleColorPsp = Color(0xFFBABABA)
val ConsoleColorSnes = ConsoleColorNes
val ConsoleColorGenesis = ConsoleColorGba
fun getConsoleColor(consoleId: String): Color = when (consoleId) {
    "gameboy" -> ConsoleColorGb
    else -> ConsoleColorNes
}
val FireSpotBlue = FireSpotAccentBlue
val FireSpotViolet = Color(0xFFB4B4B4)
val FireSpotAccentWarm = FireSpotTextPrimary
val FireSpotDarkNavy = FireSpotNavyBottom
val FireSpotTextWhite = FireSpotTextPrimary
val FireSpotFocusBorder = FireSpotTextPrimary
val FireSpotSurfaceCard = FireSpotGlassCard
val FireSpotSurfaceCardHover = FireSpotGlassCardHover
val FireSpotAccentGradient = FireSpotBrandGradient
val FireSpotHeroScrim = Brush.horizontalGradient(listOf(FireSpotDarkNavy, Color.Transparent))
val FireSpotHeroBottomScrim = Brush.verticalGradient(listOf(Color.Transparent, FireSpotDarkNavy))
val BgDark = FireSpotDarkNavy
val CardBg = FireSpotSurfaceCard
val FocusBorder = FireSpotFocusBorder
val OverlayDim = Color(0xEE080808)
val TextPrimary = FireSpotTextPrimary
val TextSecondary = FireSpotTextSecondary
