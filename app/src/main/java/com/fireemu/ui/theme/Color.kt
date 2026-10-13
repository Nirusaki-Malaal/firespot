package com.fireemu.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Background Palette: Deep navy subtle vertical gradient and soft vignette
// top #0E1A33 -> mid #0A1226 -> bottom #070C1A
val FireSpotNavyTop = Color(0xFF0E1A33)
val FireSpotNavyMid = Color(0xFF0A1226)
val FireSpotNavyBottom = Color(0xFF070C1A)

val FireSpotBackgroundGradient = Brush.verticalGradient(
    listOf(FireSpotNavyTop, FireSpotNavyMid, FireSpotNavyBottom)
)

// Glass Surfaces: rgba(255,255,255,0.06) over navy, border rgba(255,255,255,0.10)
val FireSpotGlassCard = Color(0x0FFFFFFF) // ~6% white
val FireSpotGlassBorder = Color(0x1AFFFFFF) // ~10% white
val FireSpotGlassCardHover = Color(0x1FFFFFFF) // ~12% white

// Text: primary #FFFFFF, secondary #A9B4CC, section labels #B8C2D9, subtitle #9AA7C2
val FireSpotTextPrimary = Color(0xFFFFFFFF)
val FireSpotTextSecondary = Color(0xFFA9B4CC)
val FireSpotSectionLabel = Color(0xFFB8C2D9)
val FireSpotTextSubtitle = Color(0xFF9AA7C2)
val FireSpotTitleCyan = Color(0xFF4FD1FF)

// Focus Accent: electric blue #3D8BFF, glow rgba(61,139,255,0.55)
val FireSpotAccentBlue = Color(0xFF3D8BFF)
val FireSpotAccentGlow = Color(0x8C3D8BFF)
val FireSpotSearchFocusedBg = Color(0xFF16315F)
val FireSpotResumeBadge = Color(0xFF2F6BFF)

// Brand Amber: #FFB020 -> #FF8A00 for logo wordmark (#FFC845 -> #FF9A1F gradient)
val FireSpotBrandAmberTop = Color(0xFFFFC845)
val FireSpotBrandAmberBottom = Color(0xFFFF9A1F)
val FireSpotBrandAmberSolid = Color(0xFFFFB020)

val FireSpotBrandGradient = Brush.horizontalGradient(
    listOf(FireSpotBrandAmberTop, FireSpotBrandAmberBottom)
)

// Left Rail tokens
val FireSpotRailIdleIcon = Color(0xFF8FA0C0)
val FireSpotRailSelectedBg = Color(0xD91E3C7A) // #1E3C7A at ~85% opacity
val FireSpotRailSelectedIcon = Color(0xFFE4EFFF)

// Console Edge Colors per spec
val ConsoleColorNes = Color(0xFFE53935)       // NES Red
val ConsoleColorGb = Color(0xFF8E44D6)        // GB/GBC Purple
val ConsoleColorGba = Color(0xFF3F5BD8)       // GBA Indigo-Blue
val ConsoleColorPs1 = Color(0xFF2EA8E6)       // PS1 Cyan-Blue
val ConsoleColorPsp = Color(0xFF1FB5A6)       // PSP Teal
val ConsoleColorSnes = Color(0xFF8A7BE0)      // SNES Lavender
val ConsoleColorGenesis = Color(0xFF2B5CB8)   // Genesis/Mega Drive Dark Blue

fun getConsoleColor(consoleId: String): Color {
    return when (consoleId.lowercase()) {
        "nes" -> ConsoleColorNes
        "gameboy", "gb", "gbc" -> ConsoleColorGb
        "gba" -> ConsoleColorGba
        "ps1" -> ConsoleColorPs1
        "psp" -> ConsoleColorPsp
        "snes" -> ConsoleColorSnes
        "genesis", "megadrive" -> ConsoleColorGenesis
        else -> Color(0xFF3D8BFF)
    }
}
