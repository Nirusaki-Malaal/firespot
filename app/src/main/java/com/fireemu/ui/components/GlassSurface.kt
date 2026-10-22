package com.fireemu.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Cached gradients and etched lines give depth without backdrop blur or moving noise. */
fun Modifier.glassFinish() = drawWithCache {
    val finish = Brush.verticalGradient(listOf(Color(0xFF292929), Color(0xFF181818), Color(0xFF121212)))
    val radius = CornerRadius(6.dp.toPx())
    onDrawBehind {
        drawRoundRect(finish, cornerRadius=radius)
        drawLine(Color(0x35FFFFFF),Offset(7.dp.toPx(),1f),Offset(size.width-7.dp.toPx(),1f))
        drawLine(Color(0xAA000000),Offset(7.dp.toPx(),size.height-1f),Offset(size.width-7.dp.toPx(),size.height-1f))
    }
}
fun Modifier.etchedBackdrop() = drawWithCache {
    val brush = Brush.radialGradient(listOf(Color(0xFF292929),Color(0xFF080808)), center=Offset(size.width*.75f,0f),radius=size.width*.8f)
    val step=48.dp.toPx()
    onDrawBehind {
        drawRect(brush)
        var x=0f
        while(x<size.width) {drawLine(Color(0x05FFFFFF),Offset(x,0f),Offset(x,size.height));x+=step}
    }
}
