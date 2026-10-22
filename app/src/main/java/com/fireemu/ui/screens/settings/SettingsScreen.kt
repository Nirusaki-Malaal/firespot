package com.fireemu.ui.screens.settings

import android.content.ActivityNotFoundException
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.BuildConfig
import com.fireemu.FireEmuApp
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
import kotlinx.coroutines.*

@Composable
fun SettingsScreen(viewModel:SettingsViewModel,onOpenDiscover:()->Unit,onNavigateMapping:()->Unit,onOpenInputDoctor:()->Unit,onOpenStorage:()->Unit,onNavigateBack:()->Unit) {
    val settings by viewModel.settings.collectAsState()
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    var message by remember {mutableStateOf<String?>(null)}
    var importing by remember {mutableStateOf(false)}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->
        if(uri!=null) scope.launch {
            importing=true
            message=try {
                withContext(Dispatchers.IO) {
                    var name="Imported game.gb"
                    context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use {if(it.moveToFirst()) name=it.getString(0)}
                    val importer=com.fireemu.data.repository.CartridgeImporter(context,FireEmuApp.instance.gameRepository)
                    val game=importer.import(context.contentResolver.openInputStream(uri) ?: error("This file cannot be opened."),name)
                    "Added ${game.cleanTitle} to your library."

                }
            } catch(e:Exception) {e.message?:"Import failed. Please try again."}
            importing=false
        }
    }
    Row(Modifier.fillMaxSize().etchedBackdrop()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(32.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Text("Make yourself at home.",color=FireSpotTextPrimary,fontSize=30.sp,fontWeight=FontWeight.Light)
                HeaderPillButton("back","Back",onNavigateBack)
            }
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                TvButton("Discover games & console support",onOpenDiscover)
                TvButton("Browse ROMsGames",{context.startActivity(android.content.Intent(context,com.fireemu.ui.screens.browser.GameBrowserActivity::class.java))})
            }
            Text("LIBRARY",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=2.sp)
            SettingsRow("Two original games, already included","Orbit Collector and Pocket Courier are original CC0 homebrew. They work offline.")
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                TvButton(if(importing) "Importing…" else "Import a game",{
                    try {picker.launch(arrayOf("*/*"))} catch(_:ActivityNotFoundException) {message="This TV has no file picker. The two included games are ready to play."}
                },enabled=!importing)
            }
            if(message!=null) Text(message!!,color=FireSpotTextPrimary,fontSize=13.sp)
            Text("PLAY & DISPLAY",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=2.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                HeaderPillButton("display","Scaling: ${settings.videoScaling.label}",{viewModel.cycleScaling()})
                HeaderPillButton("display","Frame counter: ${if(settings.showFps) "On" else "Off"}",{viewModel.toggleFps()})
                HeaderPillButton("controller","Remap controls",onNavigateMapping)
            }
            Text("Keyboard: arrows or WASD move; Z/X are A/B; Enter starts; Right Shift selects; Escape pauses. USB and Bluetooth keyboards use the same controls.",fontSize=12.sp,color=FireSpotTextSecondary)
            Text("Audio uses the game core’s native sample rate. Menu opens save slots, load, reset and exit during play.",fontSize=12.sp,color=FireSpotTextSecondary)
            Text("YOUR SETUP",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=2.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                TvButton("Controller & keyboard tester",onOpenInputDoctor)
                TvButton("Storage & playtime",onOpenStorage)
            }
            Text("ABOUT THIS BUILD",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=2.sp)
            SettingsRow(if(BuildConfig.BUNDLED_CORE) "Phase 0 · Self-contained demo" else "Standard · Separate console software",if(BuildConfig.BUNDLED_CORE) "Game Boy / Game Boy Color support is included. The standard APK keeps emulator cores separate." else "Game Boy support must be installed separately before playing.")
            Text("FireSpot · GPL-3.0-or-later · Upstream component notices retained.\nGambatte · GPL-2.0 · Source and license included with the demo distribution.\nStarter games · CC0-1.0 · No commercial games or proprietary BIOS included.",color=FireSpotTextSubtitle,fontSize=11.sp,lineHeight=18.sp)
        }
    }
}
@Composable
private fun SettingsRow(title:String,body:String) {
    Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(title,color=FireSpotTextPrimary,fontSize=15.sp)
        Text(body,color=FireSpotTextSecondary,fontSize=12.sp,lineHeight=18.sp)
    }
}
