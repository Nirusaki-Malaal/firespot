package com.fireemu.ui.screens.tools

import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.FireEmuApp
import com.fireemu.ui.components.TvButton
import com.fireemu.ui.components.etchedBackdrop
import com.fireemu.ui.components.glassFinish
import com.fireemu.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private data class StorageSnapshot(val groups: List<Pair<String, Long>>, val available: Long)
private fun bytesLabel(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> "%.2f GiB".format(bytes / (1024.0 * 1024 * 1024))
    bytes >= 1024L * 1024 -> "%.1f MiB".format(bytes / (1024.0 * 1024))
    bytes >= 1024 -> "%.1f KiB".format(bytes / 1024.0)
    else -> "$bytes B"
}
private fun folderBytes(file: File): Long = if (!file.exists()) 0 else
    file.walkTopDown().filter { it.isFile }.sumOf { it.length() }

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
fun StorageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val library = remember { FireEmuApp.instance.gameRepository.getAllGames() }
    val games by library.collectAsState(initial = emptyList())
    var refresh by remember { mutableStateOf(0) }
    var snapshot by remember { mutableStateOf<StorageSnapshot?>(null) }
    var busy by remember { mutableStateOf(false) }
    var clearRequested by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(refresh) {
        busy = true
        try {
            val cleared = clearRequested
            snapshot = withContext(Dispatchers.IO) {
                // Coil can hold open cache files. Clear through its own cache API.
                if (cleared) {
                    coil.Coil.imageLoader(context).memoryCache?.clear()
                    coil.Coil.imageLoader(context).diskCache?.clear()
                }
                StorageSnapshot(listOf(
                    "Game cartridges" to folderBytes(File(context.filesDir, "games")),
                    "Downloaded console software" to folderBytes(File(context.filesDir, "cores")),
                    "Save slots & recovery copies" to folderBytes(File(context.filesDir, "states")),
                    "Cartridge battery progress" to folderBytes(File(context.filesDir, "battery")),
                    "Image cache" to folderBytes(File(context.cacheDir, "image_cache"))
                ), StatFs(context.filesDir.absolutePath).availableBytes)
            }
            if (cleared) message = "Image cache cleared. Games, console software and saves are preserved."
        } catch (e: Exception) {
            message = "Could not refresh storage: ${e.message ?: "try again"}"
        } finally { clearRequested = false; busy = false }
    }
    Column(Modifier.fillMaxSize().etchedBackdrop().verticalScroll(rememberScrollState()).padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Storage & playtime", color = FireSpotTextPrimary, fontSize = 28.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TvButton(if (busy) "Refreshing…" else "Refresh", { refresh++ }, enabled = !busy)
            TvButton("Clear image cache", { clearRequested = true; refresh++ }, enabled = !busy)
            TvButton("Back", onBack)
        }
        message?.let { Text(it, color = FireSpotTextPrimary, fontSize = 13.sp) }
        snapshot?.let { data ->
            Text("${bytesLabel(data.available)} available on app storage", color = FireSpotTextPrimary, fontSize = 20.sp)
            data.groups.forEach { (label, bytes) ->
                Row(Modifier.fillMaxWidth().glassFinish().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = FireSpotTextSecondary)
                    Text(bytesLabel(bytes), color = FireSpotTextPrimary)
                }
            }
        }
        Text("YOUR PLAYTIME", color = FireSpotTextSubtitle, fontSize = 12.sp)
        val totalSeconds = games.sumOf { it.playTimeSeconds }
        Text("${games.size} games · ${games.count { it.isFavorite }} favorites · ${totalSeconds / 3600}h ${(totalSeconds % 3600) / 60}m played", color = FireSpotTextPrimary, fontSize = 18.sp)
        val played = games.filter { it.playTimeSeconds > 0 }.sortedByDescending { it.playTimeSeconds }
        if (played.isEmpty()) Text("Your most-played games will appear after you finish a play session.", color = FireSpotTextSecondary)
        played.take(10).forEach { game ->
            Row(Modifier.fillMaxWidth().glassFinish().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(game.cleanTitle, color = FireSpotTextPrimary, modifier = Modifier.weight(1f))
                Text(game.formattedPlayTime, color = FireSpotTextSecondary)
            }
        }
        Text("Storage totals cover FireSpot’s managed folders. The APK, app database, preferences and any cartridges outside app storage are excluded. Playtime is recorded when a session ends.", color = FireSpotTextSubtitle, fontSize = 12.sp)
    }
}
