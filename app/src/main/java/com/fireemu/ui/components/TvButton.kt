package com.fireemu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.ui.theme.FireSpotBlue
import com.fireemu.ui.theme.FireSpotFocusBorder
import com.fireemu.ui.theme.FireSpotSurfaceCard
import com.fireemu.ui.theme.FireSpotSurfaceCardHover
import com.fireemu.ui.theme.FireSpotTextSecondary
import com.fireemu.ui.theme.FireSpotTextWhite
import com.fireemu.ui.theme.FireSpotViolet

@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        label = "firespot_btn_scale"
    )

    val backgroundBrush = when {
        isPrimary && isFocused -> Brush.horizontalGradient(listOf(FireSpotBlue, FireSpotViolet))
        isPrimary -> Brush.horizontalGradient(listOf(FireSpotBlue.copy(alpha = 0.85f), FireSpotViolet.copy(alpha = 0.85f)))
        isFocused -> Brush.horizontalGradient(listOf(FireSpotSurfaceCardHover, FireSpotSurfaceCardHover))
        else -> Brush.horizontalGradient(listOf(FireSpotSurfaceCard, FireSpotSurfaceCard))
    }

    Box(
        modifier = modifier
            .scale(scale)
            .border(
                border = if (isFocused) BorderStroke(2.dp, FireSpotFocusBorder) else BorderStroke(1.dp, Color(0x2EFFFFFF)),
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                brush = backgroundBrush,
                shape = RoundedCornerShape(10.dp)
            )
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) FireSpotTextWhite else FireSpotTextSecondary,
            fontSize = 16.sp,
            fontWeight = if (isPrimary || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}
