package com.fireemu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotRailIdleIcon
import com.fireemu.ui.theme.FireSpotRailSelectedBg
import com.fireemu.ui.theme.FireSpotRailSelectedIcon

enum class LeftRailTab(val id: String, val icon: String, val title: String) {
    HOME("home", "⌂", "Home"),
    LIBRARY("library", "🗂", "All Games"),
    CONSOLES("consoles", "📺", "Consoles"),
    GAMES("games", "🎮", "Games"),
    SETTINGS("settings", "⚙", "Settings")
}

/**
 * Left Navigation Rail:
 * - Slim vertical rail, ~72dp wide, icons only, vertically centered group, 40dp spacing
 * - Outlined icons #8FA0C0 idle
 * - Selected item (Gamepad on Home): rounded-rect filled #1E3C7A at ~85% opacity with 3dp accent bar on left edge, icon turns white-blue
 * - No expand on focus; stays icons only.
 */
@Composable
fun FireSpotLeftRail(
    selectedTab: LeftRailTab,
    onTabSelected: (LeftRailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LeftRailTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                RailIconButton(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
fun RailIconButton(
    tab: LeftRailTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> FireSpotRailSelectedBg
                    isFocused -> Color(0x331E3C7A)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) FireSpotAccentBlue else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // 3dp accent indicator bar on left edge for selected item
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(FireSpotAccentBlue)
            )
        }

        Text(
            text = tab.icon,
            fontSize = 24.sp,
            color = if (isSelected || isFocused) FireSpotRailSelectedIcon else FireSpotRailIdleIcon,
            modifier = Modifier.scale(if (isFocused) 1.1f else 1.0f)
        )
    }
}
