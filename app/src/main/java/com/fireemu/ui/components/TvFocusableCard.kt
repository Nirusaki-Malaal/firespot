package com.fireemu.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fireemu.ui.theme.*

@Composable
fun TvFocusableCard(
    onClick: () -> Unit, modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp), cornerRadiusDp: Float = 6f,
    focusedScale: Float = 1f, borderWidth: Dp = 2.dp,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val action = rememberFireSpotAction(onClick)
    FireSpotFocusFeedback(focused)
    // Exactly one focus target; focus changes do not resize/re-layout the card.
    Box(modifier.clip(shape).glassFinish()
        .border(if(focused) borderWidth else 1.dp, if(focused) FireSpotFocusBorder else FireSpotGlassBorder,shape)
        .tvAction(interaction,action=action)) { content(focused) }
}
