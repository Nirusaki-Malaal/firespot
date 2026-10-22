package com.fireemu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.ui.theme.*

@Composable
fun FireSpotHeader(onOpenSearch:()->Unit,onOpenSettings:()->Unit,modifier:Modifier=Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal=32.dp,vertical=22.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            FireSpotIcon("brand",Modifier.size(28.dp))
            Text("firespot",fontSize=25.sp,fontWeight=FontWeight.SemiBold,color=FireSpotTextPrimary,letterSpacing=(-1).sp)
            Text("/  PLAY AT HOME",fontSize=10.sp,color=FireSpotTextSubtitle,letterSpacing=2.sp,modifier=Modifier.padding(start=16.dp))
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            HeaderPillButton("search","Search",onOpenSearch)
            HeaderPillButton("settings","Settings",onOpenSettings)
        }
    }
}
@Composable
fun HeaderPillButton(icon:String,label:String,onClick:()->Unit) {
    TvFocusableCard(onClick=onClick,modifier=Modifier.height(42.dp)) { focused ->
        Row(Modifier.padding(horizontal=14.dp).fillMaxHeight(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            FireSpotIcon(icon,Modifier.size(17.dp),tint=if(focused) FireSpotTextPrimary else FireSpotTextSecondary)
            Text(label,color=FireSpotTextPrimary,fontSize=13.sp)
        }
    }
}
