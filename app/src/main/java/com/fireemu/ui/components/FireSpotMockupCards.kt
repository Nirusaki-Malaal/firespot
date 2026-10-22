package com.fireemu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.fireemu.domain.models.*
import com.fireemu.ui.theme.*

@Composable
fun MockupConsoleCard(console:ConsoleType,titleCount:Int,onClick:()->Unit,modifier:Modifier=Modifier) {
    TvFocusableCard(onClick,modifier.width(270.dp).height(90.dp)) { _ ->
        Row(Modifier.fillMaxSize().padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
            FireSpotIcon("console",Modifier.size(36.dp),tint=FireSpotTextSecondary)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text(console.shortName,color=FireSpotTextPrimary,fontSize=17.sp,fontWeight=FontWeight.Medium)
                Text("$titleCount ${if(titleCount==1) "game" else "games"} in your library",color=FireSpotTextSecondary,fontSize=12.sp)
            }
            Text("01",color=FireSpotTextSubtitle,fontSize=11.sp)
        }
    }
}
@Composable
fun MockupContinuePlayingCard(game:Game,isMostRecentOrHasState:Boolean=true,onClick:()->Unit,modifier:Modifier=Modifier) {
    TvFocusableCard(onClick,modifier.width(234.dp).height(192.dp)) { focused ->
        Column {
            Box(Modifier.fillMaxWidth().height(126.dp)) {
                if(!game.coverArtPath.isNullOrEmpty()) AsyncImage(model=game.coverArtPath,contentDescription=null,contentScale=ContentScale.Crop,colorFilter=ColorFilter.colorMatrix(ColorMatrix().apply {setToSaturation(0f)}),modifier=Modifier.fillMaxSize())
                else FallbackCover(game,Modifier.fillMaxSize())
                if(isMostRecentOrHasState && game.lastPlayedTimestamp>0) Text("RECENTLY PLAYED",color=FireSpotTextPrimary,fontSize=9.sp,letterSpacing=1.sp,modifier=Modifier.align(Alignment.TopStart).padding(12.dp))
            }
            Column(Modifier.padding(horizontal=14.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(game.cleanTitle,color=FireSpotTextPrimary,fontSize=15.sp,fontWeight=FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text("${game.console.shortName}  /  ${game.genre}",color=FireSpotTextSecondary,fontSize=10.sp,maxLines=1)
            }
        }
    }
}
@Composable
fun MockupFavoritesCard(game:Game,onClick:()->Unit,modifier:Modifier=Modifier) = MockupContinuePlayingCard(game,false,onClick,modifier)

@Composable
fun FallbackCover(game:Game,modifier:Modifier=Modifier) {
    Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF303030),Color(0xFF121212))))) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke=Stroke(1.1f)
            val c=Color(0xFF959595)
            val center=Offset(size.width*.6f,size.height*.52f)
            if(game.cleanTitle.contains("Orbit")) {
                for(i in 1..4) drawCircle(c.copy(alpha=.14f+i*.1f),size.height*.13f*i,center,style=stroke)
                drawCircle(Color(0xFFE5E5E5),6f,Offset(center.x+size.height*.24f,center.y-size.height*.24f))
                drawLine(c,Offset(center.x-12f,center.y),Offset(center.x+12f,center.y),1f)
                drawLine(c,Offset(center.x,center.y-12f),Offset(center.x,center.y+12f),1f)
            } else {
                val step=size.width/9
                for(i in 0..9) {drawLine(c.copy(alpha=.18f),Offset(i*step,0f),Offset(i*step,size.height),1f);drawLine(c.copy(alpha=.18f),Offset(0f,i*step),Offset(size.width,i*step),1f)}
                drawLine(c,Offset(size.width*.25f,size.height*.7f),Offset(size.width*.65f,size.height*.3f),2f)
                drawRect(Color(0xFFE5E5E5),Offset(size.width*.65f-5,size.height*.3f-5),Size(10f,10f))
                drawCircle(c,6f,Offset(size.width*.25f,size.height*.7f),style=stroke)
            }
        }
        Text("${game.console.shortName.uppercase()}  /  ${if(game.releaseYear>0) game.releaseYear else "LOCAL"}",color=FireSpotTextSubtitle,fontSize=9.sp,letterSpacing=1.sp,modifier=Modifier.align(Alignment.BottomStart).padding(12.dp))
    }
}
