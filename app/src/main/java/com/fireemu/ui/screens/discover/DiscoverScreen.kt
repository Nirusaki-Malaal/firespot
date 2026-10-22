package com.fireemu.ui.screens.discover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.content.ActivityNotFoundException
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fireemu.FireEmuApp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
import kotlinx.coroutines.*

/** Native catalog stays lightweight; the external provider opens only on request. */
@Composable
fun DiscoverScreen(onOpenGame:(Long)->Unit,onImportGame:()->Unit,onBack:()->Unit,initialTab:String="Games") {
    val context=LocalContext.current
    val app=FireEmuApp.instance
    val repo=app.downloads
    val catalog by repo.catalog.collectAsState()
    val transfer by repo.transfer.collectAsState()
    val library by remember {app.gameRepository.getAllGames()}.collectAsState(initial=emptyList())
    val scope=rememberCoroutineScope()
    val firstFocus=remember {FocusRequester()}
    var job by remember {mutableStateOf<Job?>(null)}
    var tab by rememberSaveable {mutableStateOf(initialTab)}
    var installed by remember {mutableStateOf(setOf<String>())}
    var note by remember {mutableStateOf("")}
    var query by rememberSaveable {mutableStateOf("")}
    var showSearch by remember {mutableStateOf(false)}
    var importing by remember {mutableStateOf(false)}
    var system by rememberSaveable {mutableStateOf<String?>(null)}
    var sort by rememberSaveable {mutableStateOf("A–Z")}
    val systems=remember(catalog) {listOf<String?>(null)+catalog.cores.flatMap {it.consoleIds}.distinct().sortedBy {ConsoleType.fromId(it).displayName}}
    val visibleGames=remember(catalog,query,system,sort,tab,installed) {
        val filtered=catalog.games.filter {g->(system==null || g.consoleId==system) && (tab!="Installed" || g.id in installed) && listOf(g.title,g.description,g.author,g.genre).any {it.contains(query,true)}}
        when(sort) {"Size"->filtered.sortedBy {it.asset.bytes};"Newest"->filtered.sortedByDescending {it.year};else->filtered.sortedBy {it.title.lowercase()}}
    }
    LaunchedEffect(Unit) {firstFocus.requestFocus()}
    LaunchedEffect(catalog,transfer.busy,library) {
        if(!transfer.busy) installed=catalog.games.filter {repo.installedGame(it)!=null}.map {it.id}.toSet()
    }
    fun run(action:suspend ()->Unit) {job=scope.launch {try {note="";action()} catch(_:CancellationException) {} catch(e:Exception) {note=e.message?:"Please retry."}}}
    val importPicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->
        if(uri!=null) job=scope.launch {
            importing=true
            note="Importing game…"
            try {
                val title=withContext(Dispatchers.IO) {
                    var name="Imported game.gb"
                    context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use {if(it.moveToFirst()) name=it.getString(0)}
                    val game=com.fireemu.data.repository.CartridgeImporter(context,app.gameRepository)
                        .import(context.contentResolver.openInputStream(uri)?:error("This file cannot be opened."),name)
                    game.cleanTitle
                }
                note="Added $title to your library."
            } catch(e:Exception) {note=e.message?:"Import failed. Please try again."}
            finally {importing=false}
        }
    }
    fun openImporter() {
        try {importPicker.launch(arrayOf("*/*"))}
        catch(_:ActivityNotFoundException) {note="This device has no file picker available."}
    }
    if(showSearch) Dialog(onDismissRequest={showSearch=false},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Row(Modifier.width(650.dp).glassFinish().padding(20.dp).onPreviewKeyEvent {event->
            val k=event.nativeKeyEvent;val char=k.unicodeChar
            val edit=k.keyCode==android.view.KeyEvent.KEYCODE_DEL || (char>=32 && !k.isCtrlPressed && !k.isAltPressed)
            if(edit) {if(k.action==android.view.KeyEvent.ACTION_DOWN) query=if(k.keyCode==android.view.KeyEvent.KEYCODE_DEL) query.dropLast(1) else (query+char.toChar()).take(120);true} else false
        },horizontalArrangement=Arrangement.spacedBy(24.dp)) {
            TvKeyboard(onChar={query=(query+it).take(120)},onBackspace={query=query.dropLast(1)},onSpace={query=(query+" ").take(120)},onClear={query=""},modifier=Modifier.width(320.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(18.dp)) {
                Text("Search catalog",color=FireSpotTextPrimary,fontSize=22.sp)
                Text(query.ifEmpty {"Type a title, genre or author"},color=FireSpotTextSecondary,fontSize=15.sp,maxLines=5)
                Text("${visibleGames.size} matching titles",color=FireSpotTextSubtitle,fontSize=12.sp)
                TvButton("Show results",{showSearch=false},isPrimary=true)
            }
        }
    }
    Column(Modifier.fillMaxSize().etchedBackdrop().padding(28.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Column {
                Text("Discover & play",color=FireSpotTextPrimary,fontSize=30.sp)
                Text("Your next game starts here. Browse, download, play.",color=FireSpotTextSecondary,fontSize=12.sp)
            }
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                HeaderPillButton("library","ROMsGames library",{context.startActivity(Intent(context,com.fireemu.ui.screens.browser.GameBrowserActivity::class.java))})
                HeaderPillButton("back","Back",onBack)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            for(label in listOf("Games","Consoles","Installed","Console support")) TvButton(label,{tab=label},if(label=="Games") Modifier.focusRequester(firstFocus) else Modifier,isPrimary=tab==label)
            TvButton("Refresh catalog",{run {repo.refresh();note="Catalog is up to date."}},enabled=!transfer.busy)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            HeaderPillButton("search",if(query.isEmpty()) "Search catalog" else "Search: ${query.take(18)}",{tab="Games";showSearch=true})
            HeaderPillButton("console","Console: ${system?.let {ConsoleType.fromId(it).shortName}?:"All"}",{system=systems[(systems.indexOf(system)+1)%systems.size]})
            HeaderPillButton("library","Sort: $sort",{sort=when(sort){"A–Z"->"Size";"Size"->"Newest";else->"A–Z"}})
            if(query.isNotEmpty() || system!=null) HeaderPillButton("refresh","Clear filters",{query="";system=null})
        }
        if(transfer.message.isNotEmpty()) {
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("${transfer.message}${if(transfer.busy) " · ${transfer.progress}%" else ""}",color=FireSpotTextPrimary,fontSize=12.sp)
                if(transfer.busy) TvButton("Cancel download",{job?.cancel()})
            }
            if(transfer.busy) LinearProgressIndicator(progress=transfer.progress/100f,modifier=Modifier.fillMaxWidth(),color=FireSpotTextPrimary,trackColor=FireSpotGlassBorder)
        }
        if(note.isNotEmpty()) Text(note,color=FireSpotTextSecondary,fontSize=12.sp)
        when(tab) {
            "Consoles" -> LazyVerticalGrid(columns=GridCells.Adaptive(220.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                items(systems.filterNotNull(),key={it}) {id->
                    val console=ConsoleType.fromId(id)
                    TvFocusableCard(onClick={system=id;query="";tab="Games"},modifier=Modifier.fillMaxWidth().height(150.dp)) {
                        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            Text(console.shortName,color=FireSpotTextPrimary,fontSize=21.sp)
                            Text("${catalog.games.count {it.consoleId==id}} catalog games · ${library.count {it.consoleId==id}} installed",color=FireSpotTextSecondary,fontSize=12.sp)
                            Text(if(app.coreRepository.isCoreAvailable(console)) "Console support installed" else "Support available on demand",color=FireSpotTextSubtitle,fontSize=11.sp)
                        }
                    }
                }
            }
            "Console support" -> LazyVerticalGrid(columns=GridCells.Adaptive(300.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                items(catalog.cores,key={it.id}) {core->
                    val artifact=core.artifacts.firstOrNull {it.abi==repo.abi}
                    Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Text(core.title,color=FireSpotTextPrimary,fontSize=20.sp)
                        Text(core.consoleIds.joinToString(" / ") {ConsoleType.fromId(it).shortName},color=FireSpotTextSecondary,fontSize=12.sp)
                        Text("${core.license} · ${artifact?.bytes?.div(1024)?:0} KiB",color=FireSpotTextSubtitle,fontSize=11.sp)
                        Text("Android package available for this device.",color=FireSpotTextSubtitle,fontSize=11.sp)
                        val coreInstalled=repo.installedCore(core)
                        TvButton(if(coreInstalled) "Verify ${core.title}" else "Install ${core.title}",{run {repo.installCore(core);note="${core.title} is installed."}},enabled=!transfer.busy && artifact!=null)
                        if(coreInstalled) {
                            Text("Console support is ready. Add a game or browse the catalog.",color=FireSpotTextSecondary,fontSize=12.sp)
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                TvButton(if(importing) "Importing…" else "Import a game",::openImporter,enabled=!importing)
                                TvButton("Browse game library",{context.startActivity(Intent(context,com.fireemu.ui.screens.browser.GameBrowserActivity::class.java))})
                            }
                        }
                    }
                }
            }
            else -> {
                Text("${visibleGames.size} TITLES / Redistributable homebrew · Verified downloads",color=FireSpotTextSubtitle,fontSize=11.sp)
                if(visibleGames.isEmpty()) {
                    Text("No catalog games match this selection. You can import a supported cartridge you have permission to use.",color=FireSpotTextSecondary,fontSize=14.sp)
                    TvButton("Import a cartridge",onImportGame)
                } else LazyVerticalGrid(columns=GridCells.Adaptive(270.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    items(visibleGames,key={it.id}) {game->
                        Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            Text(game.title,color=FireSpotTextPrimary,fontSize=21.sp)
                            Text("${ConsoleType.fromId(game.consoleId).shortName} · ${game.genre} · ${game.year}",color=FireSpotTextSecondary,fontSize=12.sp)
                            Text("${game.license} · ${game.author} · ${game.asset.bytes/1024} KiB",color=FireSpotTextSubtitle,fontSize=11.sp)
                            Text(game.description,color=FireSpotTextSecondary,fontSize=12.sp,maxLines=4)
                            TvButton(if(game.id in installed) "Open ${game.title}" else "Get ${game.title}",{run {val ready=repo.installedGame(game) ?: repo.installGame(game);onOpenGame(ready.id)}},enabled=!transfer.busy)
                        }
                    }
                }
            }
        }
    }
}
