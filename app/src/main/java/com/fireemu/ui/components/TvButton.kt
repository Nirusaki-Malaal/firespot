package com.fireemu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.ui.theme.*

@Composable
fun TvButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,isPrimary:Boolean=false,isDestructive:Boolean=false) {
    val interaction=remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val light=isPrimary || focused
    val shape=RoundedCornerShape(5.dp)
    Box(modifier.heightIn(min=44.dp).clip(shape).glassFinish()
        .background(if(light) FireSpotTextPrimary else Color.Transparent)
        .border(if(focused) 2.dp else 1.dp, if(focused) Color.White else FireSpotGlassBorder,shape)
        .tvAction(interaction,enabled,onClick)
        .padding(horizontal=20.dp,vertical=12.dp),contentAlignment=Alignment.Center) {
        Text(text,color=if(!enabled) FireSpotTextSubtitle else if(light) Color.Black else FireSpotTextPrimary,fontSize=14.sp,fontWeight=FontWeight.Medium)
    }
}
