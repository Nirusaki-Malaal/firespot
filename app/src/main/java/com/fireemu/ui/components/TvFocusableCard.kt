package com.fireemu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fireemu.ui.theme.FireSpotAccentBlue
import com.fireemu.ui.theme.FireSpotAccentGlow
import com.fireemu.ui.theme.FireSpotGlassBorder
import com.fireemu.ui.theme.FireSpotGlassCard

/**
 * Pixel-faithful TV Focusable Glass Card:
 * - Idle: translucent glass rgba(255,255,255,0.06), 1dp border rgba(255,255,255,0.10)
 * - Focused: 160ms ease-out scale (1.06 - 1.08), 2dp electric blue #3D8BFF border,
 *   blur-free outer glow using drawBehind alpha strokes, lifts with zIndex.
 */
@Composable
fun TvFocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    cornerRadiusDp: Float = 14f,
    focusedScale: Float = 1.06f,
    borderWidth: Dp = 2.dp,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = tween(durationMillis = 160),
        label = "firespot_card_scale"
    )

    Box(
        modifier = modifier
            .zIndex(if (isFocused) 20f else 1f)
            .scale(scale)
            .drawBehind {
                if (isFocused) {
                    val cr = CornerRadius(cornerRadiusDp.dp.toPx(), cornerRadiusDp.dp.toPx())
                    // Draw outer glow layers without blur
                    drawRoundRect(
                        color = Color(0x333D8BFF),
                        topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                        size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                        cornerRadius = CornerRadius((cornerRadiusDp + 4).dp.toPx(), (cornerRadiusDp + 4).dp.toPx()),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0x553D8BFF),
                        topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                        size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                        cornerRadius = CornerRadius((cornerRadiusDp + 2).dp.toPx(), (cornerRadiusDp + 2).dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
            .border(
                border = if (isFocused) {
                    BorderStroke(borderWidth, FireSpotAccentBlue)
                } else {
                    BorderStroke(1.dp, FireSpotGlassBorder)
                },
                shape = shape
            )
            .background(
                color = if (isFocused) Color(0x1A1E3C7A) else FireSpotGlassCard,
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        content = { content(isFocused) }
    )
}
