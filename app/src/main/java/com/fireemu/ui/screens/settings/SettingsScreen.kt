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
import com.fireemu.domain.models.Game
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest

@Composable
fun SettingsScreen(viewModel:SettingsViewModel,onOpenDiscover:()->Unit,onNavigateMapping:()->Unit,onNavigateBack:()->Unit) {
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
                    val ext=name.substringAfterLast('.').lowercase()
                    val console=when(ext) {"gb","gbc"->com.fireemu.domain.models.ConsoleType.GAMEBOY;"nes"->com.fireemu.domain.models.ConsoleType.NES;"gba"->com.fireemu.domain.models.ConsoleType.GBA;else->error("Choose a .gb, .gbc, .nes or .gba cartridge. Disc sets need a separate transfer flow.")}
                    val maxBytes=if(console==com.fireemu.domain.models.ConsoleType.GAMEBOY) 8L*1024*1024 else 32L*1024*1024
                    val dir=File(context.filesDir,"games/imports").apply {mkdirs()}
                    val temp=File.createTempFile("import-",".tmp",dir)
                    try {
                        val digest=MessageDigest.getInstance("SHA-256")
                        var total=0L
                        context.contentResolver.openInputStream(uri)!!.use {input->temp.outputStream().use {output->
                            val buf=ByteArray(65536)
                            while(true) {val n=input.read(buf);if(n<0) break;total+=n;require(total<=maxBytes) {"This cartridge exceeds the ${maxBytes/1024/1024} MiB import limit."};digest.update(buf,0,n);output.write(buf,0,n)}
                        }}
                        when(console) {
                            com.fireemu.domain.models.ConsoleType.GAMEBOY -> require(total>=32768 && total%16384==0L) {"This is not a complete Game Boy cartridge."}
                            com.fireemu.domain.models.ConsoleType.NES -> {
                                val header=ByteArray(16);temp.inputStream().use {check(it.read(header)==16)}
                                require(header.take(4)==listOf<Byte>(78,69,83,26) && total>=16+(header[4].toInt() and 255)*16384L+(header[5].toInt() and 255)*8192L) {"This is not a complete iNES cartridge."}
                            }
                            else -> require(total>=192) {"The GBA cartridge is incomplete."}
                        }
                        val hash=digest.digest().joinToString("") {"%02x".format(it)}
                        val file=File(dir,"$hash.${name.substringAfterLast('.').lowercase()}")
                        if(!file.exists()) check(temp.renameTo(file)) {"Could not save imported game."}
                        val repo=FireEmuApp.instance.gameRepository
                        if(repo.getGameByPath(file.absolutePath)==null) repo.insertOrUpdateGames(listOf(Game(title=name.substringBeforeLast('.'),cleanTitle=name.substringBeforeLast('.'),romPath=file.absolutePath,consoleId=console.id,fileSizeBytes=total,genre="Imported game",releaseYear=0,maxPlayers=1,rating="",description="Your imported ${console.shortName} cartridge. D-pad moves; Select is A; Play/Pause is B. Menu opens controls and saves.")))
                        "Added ${name.substringBeforeLast('.')} to your library."
                    } finally {temp.delete()}
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
            TvButton("Discover games & console support",onOpenDiscover)
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
