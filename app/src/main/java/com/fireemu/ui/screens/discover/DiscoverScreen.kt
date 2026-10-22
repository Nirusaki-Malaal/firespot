package com.fireemu.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.FireEmuApp
import com.fireemu.data.catalog.CatalogGame
import com.fireemu.domain.models.ConsoleType
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
import kotlinx.coroutines.*

@Composable
fun DiscoverScreen(onOpenGame:(Long)->Unit,onBack:()->Unit) {
    val repo=FireEmuApp.instance.downloads
    val catalog by repo.catalog.collectAsState()
    val transfer by repo.transfer.collectAsState()
    val scope=rememberCoroutineScope()
    var job by remember {mutableStateOf<Job?>(null)}
    var tab by remember {mutableStateOf("Games")}
    var installed by remember {mutableStateOf(setOf<String>())}
    var note by remember {mutableStateOf("")}
    LaunchedEffect(catalog,transfer.busy) {
        if(!transfer.busy) installed=catalog.games.filter {repo.installedGame(it)!=null}.map {it.id}.toSet()
    }
    fun run(action:suspend ()->Unit) {job=scope.launch {try {note="";action()} catch(_:CancellationException) {} catch(e:Exception) {note=e.message?:"Please retry."}}}
    Column(Modifier.fillMaxSize().etchedBackdrop().padding(32.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Column {
                Text("Discover & play",color=FireSpotTextPrimary,fontSize=30.sp)
                Text("Games you can get here. Console support when you need it.",color=FireSpotTextSecondary,fontSize=12.sp)
            }
            HeaderPillButton("back","Back",onBack)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            TvButton("Games",{tab="Games"},isPrimary=tab=="Games")
            TvButton("Console support",{tab="Console support"},isPrimary=tab!="Games")
            TvButton("Refresh catalog",{run {repo.refresh();note="Catalog is up to date."}},enabled=!transfer.busy)
            if(transfer.busy) TvButton("Cancel download",{job?.cancel()})
        }
        if(transfer.message.isNotEmpty()) {
            Text("${transfer.message}${if(transfer.busy) " · ${transfer.progress}%" else ""}",color=FireSpotTextPrimary,fontSize=12.sp)
            if(transfer.busy) LinearProgressIndicator(progress=transfer.progress/100f,modifier=Modifier.fillMaxWidth(),color=FireSpotTextPrimary,trackColor=FireSpotGlassBorder)
        }
        if(note.isNotEmpty()) Text(note,color=FireSpotTextSecondary,fontSize=12.sp)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=24.dp)) {
            if(tab=="Games") {
                item {Text("ORIGINAL HOMEBREW / No file transfers needed",color=FireSpotTextSubtitle,fontSize=11.sp)}
                items(catalog.games,key={it.id}) {game ->
                    Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(game.title,color=FireSpotTextPrimary,fontSize=21.sp)
                        Text("${ConsoleType.fromId(game.consoleId).shortName} · ${game.license} · ${game.author} · ${game.asset.bytes/1024} KiB",color=FireSpotTextSubtitle,fontSize=11.sp)
                        Text(game.description,color=FireSpotTextSecondary,fontSize=12.sp)
                        TvButton(if(game.id in installed) "Open ${game.title}" else "Get ${game.title}",{
                            run {val ready=repo.installedGame(game) ?: repo.installGame(game);onOpenGame(ready.id)}
                        },enabled=!transfer.busy)
                    }
                }
            } else {
                item {Text("Install only the systems you use. Core licenses and source accompany the public release.",color=FireSpotTextSubtitle,fontSize=11.sp)}
                items(catalog.cores,key={it.id}) {core ->
                    val artifact=core.artifacts.firstOrNull {it.abi==repo.abi}
                    Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(core.title,color=FireSpotTextPrimary,fontSize=21.sp)
                        Text("${core.consoleIds.joinToString(" / ") {ConsoleType.fromId(it).shortName}} · ${core.license} · ${artifact?.bytes?.div(1024)?:0} KiB",color=FireSpotTextSecondary,fontSize=12.sp)
                        if(core.experimental) Text("Experimental: built for Android; game compatibility and Fire TV performance still need testing.",color=FireSpotTextSubtitle,fontSize=11.sp)
                        TvButton(if(repo.installedCore(core)) "Verify ${core.title}" else "Install ${core.title}",{
                            run {repo.installCore(core);note="${core.title} is installed. Add a compatible game to your library."}
                        },enabled=!transfer.busy && artifact!=null)
                    }
                }
            }
        }
    }
}
