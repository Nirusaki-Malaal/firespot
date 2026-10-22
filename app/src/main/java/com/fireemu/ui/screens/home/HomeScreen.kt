package com.fireemu.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.domain.models.*
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*

@Composable
fun HomeScreen(viewModel:HomeViewModel,onSelectConsole:(String)->Unit,onSelectGame:(Long)->Unit,onOpenDiscover:()->Unit,onOpenSearch:()->Unit,onOpenSettings:()->Unit,onSelectRailTab:(LeftRailTab)->Unit) {
    val firstFocus = remember {FocusRequester()}
    val state by viewModel.uiState.collectAsState()
    if(state.isLoading) {LoadingState("Opening your library…");return}
    if(state.error!=null) {ErrorState(message=state.error?:"Unable to load games",onRetry={viewModel.loadData()});return}
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    Row(Modifier.fillMaxSize().etchedBackdrop()) {
        FireSpotLeftRail(LeftRailTab.HOME,onSelectRailTab)
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
            item("header") { FireSpotHeader(onOpenSearch,onOpenSettings) }
            item("hero") {
                Row(Modifier.fillMaxWidth().padding(horizontal=32.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text("THE LIVING ROOM COLLECTION",color=FireSpotTextSecondary,fontSize=10.sp,letterSpacing=2.sp)
                        Text("Less setup.\nMore play.",color=FireSpotTextPrimary,fontSize=40.sp,lineHeight=42.sp,fontWeight=FontWeight.Light,letterSpacing=(-1).sp)
                        Text("Your library. Your progress. A little nostalgia.",color=FireSpotTextSecondary,fontSize=13.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            TvButton("Open library",{onSelectRailTab(LeftRailTab.LIBRARY)},Modifier.focusRequester(firstFocus),isPrimary=true)
                            HeaderPillButton("plus","Discover games",onOpenDiscover)
                        }
                    }
                    Box(Modifier.width(240.dp).height(174.dp)) {
                        ControllerSculpture(Modifier.fillMaxSize())
                        Text("${state.allGames.size.toString().padStart(2,'0')} GAMES  /  READY WHEN YOU ARE",color=FireSpotTextSubtitle,fontSize=9.sp,letterSpacing=1.sp,modifier=Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
            if(state.recentGames.isNotEmpty()) item("recent") { GameShelf("Continue playing",state.recentGames,onSelectGame) }
            item("library") { GameShelf("Your collection",state.allGames,onSelectGame) }
            if(state.favoriteGames.isNotEmpty()) item("favorites") {GameShelf("Favorites",state.favoriteGames,onSelectGame)}
            item("console") {
                Column(Modifier.padding(horizontal=32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("YOUR SYSTEMS",color=FireSpotSectionLabel,fontSize=10.sp,letterSpacing=2.sp)
                    LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        items(state.consoleCounts.keys.sorted(),key={it}) { id ->
                            MockupConsoleCard(ConsoleType.fromId(id),state.consoleCounts[id]?:0,{onSelectConsole(id)})
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun GameShelf(title:String,games:List<Game>,select:(Long)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=32.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(title,color=FireSpotTextPrimary,fontSize=18.sp,fontWeight=FontWeight.Medium)
            Text("${games.size.toString().padStart(2,'0')} TITLES",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=1.sp)
        }
        LazyRow(contentPadding=PaddingValues(horizontal=32.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            items(games,key={it.id},contentType={"game"}) {game->MockupContinuePlayingCard(game,onClick={select(game.id)})}
        }
    }
}
@Composable
fun SectionTitle(title:String) { Text(title,color=FireSpotSectionLabel,fontSize=11.sp,letterSpacing=1.5.sp,modifier=Modifier.padding(horizontal=32.dp)) }
@Composable
private fun ControllerSculpture(modifier:Modifier) {
    Canvas(modifier) {
        val w=size.width; val h=size.height
        drawOval(Color(0x66000000),Offset(w*.12f,h*.72f),Size(w*.76f,h*.12f))
        val body=Path().apply {
            moveTo(w*.2f,h*.25f);cubicTo(w*.3f,h*.18f,w*.7f,h*.18f,w*.8f,h*.25f)
            cubicTo(w*.9f,h*.4f,w*.96f,h*.75f,w*.79f,h*.75f)
            lineTo(w*.64f,h*.6f);lineTo(w*.36f,h*.6f)
            cubicTo(w*.12f,h*.92f,w*.05f,h*.61f,w*.2f,h*.25f);close()
        }
        drawPath(body,Brush.verticalGradient(listOf(Color(0xFF484848),Color(0xFF161616),Color(0xFF242424))))
        drawPath(body,Color(0xFF5C5C5C),style=Stroke(1f))
        drawRoundRect(Color(0xFF111111),Offset(w*.23f,h*.35f),Size(w*.18f,h*.065f),CornerRadius(2f))
        drawRoundRect(Color(0xFF111111),Offset(w*.295f,h*.28f),Size(w*.055f,h*.2f),CornerRadius(2f))
        for(p in listOf(Offset(.72f,.31f),Offset(.8f,.4f),Offset(.64f,.4f),Offset(.72f,.49f))) {
            drawCircle(Color(0xFF0B0B0B),w*.026f,Offset(w*p.x,h*p.y))
            drawArc(Color(0xFF959595),185f,145f,false,Offset(w*p.x-w*.026f,h*p.y-w*.026f),Size(w*.052f,w*.052f),style=Stroke(1f))
        }
        drawLine(Color(0xFF777777),Offset(w*.46f,h*.43f),Offset(w*.5f,h*.43f),2f)
        drawLine(Color(0xFF777777),Offset(w*.54f,h*.43f),Offset(w*.58f,h*.43f),2f)
    }
}
