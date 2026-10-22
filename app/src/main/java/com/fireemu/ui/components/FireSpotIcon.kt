package com.fireemu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fireemu.ui.theme.FireSpotTextPrimary

/** Small monochrome line icons, drawn locally. No emoji font or external icon dependency. */
@Composable
fun FireSpotIcon(name: String, modifier: Modifier = Modifier, tint: Color = FireSpotTextPrimary, description: String? = null) {
    Canvas(modifier.size(22.dp).semantics { if (description != null) contentDescription = description }) {
        withTransform({ scale(size.width / 24f, size.height / 24f, Offset.Zero) }) {
            val stroke = Stroke(1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            fun line(x:Float,y:Float,a:Float,b:Float) = drawLine(tint,Offset(x,y),Offset(a,b),1.5f,StrokeCap.Round)
            fun path(vararg points:Float) {
                val p = Path().apply { moveTo(points[0],points[1]); var i=2; while(i<points.size) { lineTo(points[i],points[i+1]); i+=2 } }
                drawPath(p,tint,style=stroke)
            }
            when(name.lowercase()) {
                "home" -> { path(3f,10f,12f,3f,21f,10f);path(5f,9f,5f,21f,10f,21f,10f,14f,14f,14f,14f,21f,19f,21f,19f,9f) }
                "search" -> { drawCircle(tint,6.5f,Offset(10f,10f),style=stroke);line(15f,15f,21f,21f) }
                "settings" -> { for(y in listOf(6f,12f,18f)) line(3f,y,21f,y);drawCircle(tint,2f,Offset(8f,6f),style=stroke);drawCircle(tint,2f,Offset(16f,12f),style=stroke);drawCircle(tint,2f,Offset(10f,18f),style=stroke) }
                "play", "resume" -> path(8f,4f,20f,12f,8f,20f,8f,4f)
                "back" -> {path(10f,5f,3f,12f,10f,19f);line(3f,12f,21f,12f)}
                "favorite", "heart" -> { val p=Path().apply {moveTo(12f,21f);cubicTo(-5f,10f,5f,-2f,12f,7f);cubicTo(19f,-2f,29f,10f,12f,21f)};drawPath(p,tint,style=stroke) }
                "gamepad", "controller", "games" -> {drawRoundRect(tint,Offset(2f,6f),Size(20f,13f),androidx.compose.ui.geometry.CornerRadius(3f),style=stroke);line(5f,12f,11f,12f);line(8f,9f,8f,15f);drawCircle(tint,1f,Offset(16f,10f));drawCircle(tint,1f,Offset(19f,14f))}
                "console", "display", "video" -> {drawRoundRect(tint,Offset(5f,2f),Size(14f,20f),androidx.compose.ui.geometry.CornerRadius(2f),style=stroke);drawRect(tint,Offset(8f,5f),Size(8f,7f),style=stroke);line(8f,17f,12f,17f);line(10f,15f,10f,19f);drawCircle(tint,0.8f,Offset(16f,17f))}
                "audio" -> {path(3f,9f,7f,9f,12f,4f,12f,20f,7f,15f,3f,15f,3f,9f);drawArc(tint,-55f,110f,false,Offset(9f,4f),Size(12f,16f),style=stroke)}
                "save" -> {path(4f,3f,18f,3f,21f,6f,21f,21f,3f,21f,3f,3f);drawRect(tint,Offset(7f,3f),Size(9f,6f),style=stroke);drawRect(tint,Offset(7f,14f),Size(10f,7f),style=stroke)}
                "plus", "add" -> {line(12f,4f,12f,20f);line(4f,12f,20f,12f)}
                "check" -> path(4f,12f,9f,17f,20f,6f)
                "folder", "storage" -> path(3f,5f,10f,5f,12f,8f,21f,8f,21f,20f,3f,20f,3f,5f)
                "refresh" -> {drawArc(tint,35f,285f,false,Offset(4f,4f),Size(16f,16f),style=stroke);path(21f,5f,20f,11f,14f,10f)}
                "brand" -> {path(3f,4f,11f,4f,11f,10f,21f,10f,21f,20f,13f,20f,13f,14f,3f,14f,3f,4f)}
                else -> for(x in listOf(4f,14f)) for(y in listOf(4f,14f)) drawRect(tint,Offset(x,y),Size(6f,6f),style=stroke)
            }
        }
    }
}
