package com.fireemu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val LocalFireSpotTypography = staticCompositionLocalOf { FireSpotTypography }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FireSpotTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = FireSpotBlue,
        secondary = FireSpotViolet,
        tertiary = FireSpotAccentWarm,
        background = FireSpotDarkNavy,
        surface = FireSpotSurfaceCard,
        onPrimary = FireSpotTextWhite,
        onSecondary = FireSpotTextWhite,
        onBackground = FireSpotTextWhite,
        onSurface = FireSpotTextWhite
    )

    CompositionLocalProvider(
        LocalFireSpotTypography provides FireSpotTypography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
