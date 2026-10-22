package com.fireemu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import com.fireemu.ui.theme.*

enum class LeftRailTab(val id:String,val icon:String,val title:String) {
    HOME("home","home","Home"), LIBRARY("library","library","Library"),
    CONSOLES("consoles","console","Game Boy"), GAMES("games","heart","Favorites"), SETTINGS("settings","settings","Settings")
}
@Composable
fun FireSpotLeftRail(selectedTab:LeftRailTab,onTabSelected:(LeftRailTab)->Unit,modifier:Modifier=Modifier) {
    Column(modifier.width(78.dp).fillMaxHeight().background(Color(0xFF0B0B0B)).padding(vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween) {
        FireSpotIcon("brand",Modifier.size(24.dp))
        Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
            LeftRailTab.values().forEach { tab ->
                TvFocusableCard(onClick={onTabSelected(tab)},modifier=Modifier.size(54.dp).semantics { contentDescription=tab.title;selected=tab==selectedTab }) { focused ->
                    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                        FireSpotIcon(tab.icon,tint=if(focused||tab==selectedTab) FireSpotTextPrimary else FireSpotRailIdleIcon)
                        if(tab==selectedTab) Box(Modifier.align(Alignment.CenterStart).width(2.dp).height(18.dp).background(FireSpotTextPrimary))
                    }
                }
            }
        }
    }
}
