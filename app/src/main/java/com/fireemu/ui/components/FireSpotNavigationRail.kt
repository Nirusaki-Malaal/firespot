package com.fireemu.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.fireemu.ui.theme.FireSpotAccentWarm
import com.fireemu.ui.theme.FireSpotBlue
import com.fireemu.ui.theme.FireSpotDarkNavy
import com.fireemu.ui.theme.FireSpotFocusBorder
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextWhite

enum class NavRailItem(val id: String, val icon: String, val label: String) {
    SEARCH("search", "🔍", "Search"),
    HOME("home", "🏠", "Home"),
    CONSOLES("consoles", "📺", "Consoles"),
    GAMES("games", "🎮", "All Games"),
    FAVORITES("favorites", "⭐", "Favorites"),
    CATEGORIES("categories", "🏷️", "Categories"),
    SETTINGS("settings", "⚙️", "Settings")
}

@Composable
fun FireSpotNavigationRail(
    selectedItemId: String,
    onItemSelected: (NavRailItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRailFocused by remember { mutableStateOf(false) }

    val railWidth by animateDpAsState(
        targetValue = if (isRailFocused) 230.dp else 68.dp,
        animationSpec = tween(durationMillis = 200),
        label = "firespot_rail_width"
    )

    Box(
        modifier = modifier
            .zIndex(50f)
            .width(railWidth)
            .fillMaxHeight()
            .background(FireSpotDarkNavy.copy(alpha = if (isRailFocused) 0.98f else 0.85f))
            .border(
                width = 1.dp,
                color = Color(0x1F5D5D5D),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            )
            .onFocusChanged { focusState ->
                isRailFocused = focusState.hasFocus
            }
            .padding(vertical = 20.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Brand Logo tag at top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = if (isRailFocused) Alignment.Start else Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = "FS",
                        fontSize = 24.sp,
                        color = FireSpotAccentWarm
                    )
                    if (isRailFocused) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FireSpot",
                            color = FireSpotTextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Navigation Items
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                NavRailItem.values().forEach { item ->
                    val isSelected = item.id == selectedItemId
                    NavRailButton(
                        item = item,
                        isSelected = isSelected,
                        isExpanded = isRailFocused,
                        onClick = { onItemSelected(item) }
                    )
                }
            }

            // Profile / Avatar Slot at Bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x14FFFFFF))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FireSpotBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FS",
                        color = FireSpotTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (isRailFocused) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Player 1",
                            color = FireSpotTextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Local Profile",
                            color = FireSpotTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavRailButton(
    item: NavRailItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val action = rememberFireSpotAction(onClick)
    FireSpotFocusFeedback(isFocused)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isFocused -> FireSpotBlue.copy(alpha = 0.25f)
                    isSelected -> Color(0x1A5D5D5D)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) FireSpotFocusBorder else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = action
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Accent Indicator Bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) FireSpotAccentWarm else Color.Transparent)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = item.icon,
            fontSize = 20.sp
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = item.label,
                color = if (isSelected || isFocused) FireSpotTextWhite else FireSpotTextSecondary,
                fontSize = 16.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
