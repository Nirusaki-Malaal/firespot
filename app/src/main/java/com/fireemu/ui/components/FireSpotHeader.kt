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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotBrandAmberBottom
import com.fireemu.ui.theme.FireSpotBrandAmberTop
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard
import com.fireemu.ui.theme.FireSpotSearchFocusedBg
import com.fireemu.ui.theme.FireSpotTextPrimary
import com.fireemu.ui.theme.FireSpotTextSubtitle

/**
 * Top Header of content area:
 * - Left: Lightning bolt amber icon + wordmark "FireSpot" (bold, 34sp, #FFC845 -> #FF9A1F)
 *   Subtext: "Amazon Fire TV Edition • Alexa Remote Ready" in 13sp #9AA7C2
 * - Right: Two pill buttons, 48dp tall, radius 12dp: Search (with magnifier), Settings (with gear)
 */
@Composable
fun FireSpotHeader(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Logo + Tagline
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    fontSize = 32.sp,
                    color = FireSpotBrandAmberTop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "FireSpot",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold, // 800
                    color = FireSpotBrandAmberTop // fallback/tint
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Amazon Fire TV Edition • Alexa Remote Ready",
                color = FireSpotTextSubtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal // 400
            )
        }

        // Right: Search & Settings Pill Buttons (48dp tall, radius 12dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderPillButton(
                icon = "🔍",
                label = "Search",
                onClick = onOpenSearch
            )

            HeaderPillButton(
                icon = "⚙",
                label = "Settings",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
fun HeaderPillButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                if (isFocused) {
                    drawRoundRect(
                        color = Color(0x553D8BFF),
                        topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                        size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FireSpotAccentBlue else FireSpotGlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (isFocused) FireSpotSearchFocusedBg else FireSpotGlassCard,
                shape = RoundedCornerShape(12.dp)
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 17.sp,
                color = if (isFocused) Color(0xFF68B2FF) else FireSpotTextSubtitle
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = FireSpotTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
