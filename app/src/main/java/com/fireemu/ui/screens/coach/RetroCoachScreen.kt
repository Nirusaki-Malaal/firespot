package com.fireemu.ui.screens.coach

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.fireemu.BuildConfig
import com.fireemu.ui.audio.FireSpotAudio
import com.fireemu.data.cloud.CloudSaves
import com.fireemu.ui.components.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

@Composable
fun RetroCoachScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val prefs = remember { context.getSharedPreferences("retro_coach", 0) }
    var endpoint by rememberSaveable { mutableStateOf(prefs.getString("endpoint", BuildConfig.COACH_ENDPOINT) ?: "") }
    var setup by remember { mutableStateOf(endpoint.isBlank()) }
    var token by remember { mutableStateOf("") }
    var game by rememberSaveable { mutableStateOf("") }
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }
    var references by remember { mutableStateOf(listOf<String>()) }
    var grounded by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var voiceFile by remember { mutableStateOf<File?>(null) }
    var voiceJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    fun stopVoice() {
        voiceJob?.cancel(); voiceJob = null
        FireSpotAudio.setSpeechDucking(false)
        player?.release(); player = null
        voiceFile?.delete(); voiceFile = null; speaking = false
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) stopVoice() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); stopVoice() }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color.White)) {
        Column(Modifier.fillMaxSize().etchedBackdrop().verticalScroll(rememberScrollState()).padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Retro Coach", color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp)
                TvButton("Back", onBack)
            }
            Text("A little help for your next adventure.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
            Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ASK YOUR COACH", color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(game, { game = it.take(160) }, label = { Text("Game / console (optional)") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(question, { question = it.take(1000) }, label = { Text("Describe where you are stuck") }, enabled = !busy, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvButton("Explain the controls", { question = "Explain FireSpot's default keyboard controls and how to remap controls." }, enabled = !busy)
                    TvButton("Help with saves", { question = "How do I save and resume progress in FireSpot?" }, enabled = !busy)
                }
                TvButton(if (busy) "Thinking…" else "Ask coach", {
                    if (endpoint.isBlank() || token.isBlank()) { setup = true; status = "Enter your connection details and session access token below." }
                    else {
                        stopVoice(); busy = true; status = "Finding guidance…"; answer = ""; references = emptyList()
                        val address = endpoint.trim(); val access = token; val title = game; val prompt = question
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) { CloudSaves.request(address, body = JSONObject().put("game", title).put("question", prompt), token = access) }
                                answer = result.getString("answer"); grounded = result.optBoolean("grounded")
                                val sources = result.optJSONArray("sources")
                                references = if (sources == null) emptyList() else (0 until sources.length()).map { sources.getJSONObject(it).getString("excerpt") }
                                status = if (grounded) "Based on FireSpot reference documents" else "General guidance · no reference documents were returned"
                            } catch (e: CancellationException) { throw e }
                            catch (e: Exception) { status = e.message ?: "Could not reach the coach. Please try again." }
                            finally { busy = false }
                        }
                    }
                }, enabled = !busy && question.isNotBlank(), isPrimary = true)
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (answer.isNotBlank()) {
                Column(Modifier.fillMaxWidth().glassFinish().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(answer, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, lineHeight = 27.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvButton(if (speaking) "Preparing / playing…" else "Listen to answer", {
                            stopVoice(); speaking = true; val text = answer.take(2400); val access = token
                            val address = endpoint.trim().substringBeforeLast('/') + "/speech"
                            voiceJob = scope.launch {
                                val file = File.createTempFile("coach-voice-", ".mp3", context.cacheDir)
                                voiceFile = file
                                try {
                                    withContext(Dispatchers.IO) {
                                        val response = CloudSaves.request(address, body = JSONObject().put("text", text), token = access)
                                        file.writeBytes(Base64.decode(response.getString("audio"), Base64.DEFAULT))
                                    }
                                    val media = MediaPlayer(); player = media
                                    media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                                    media.setOnPreparedListener { FireSpotAudio.setSpeechDucking(true); it.start() }
                                    media.setOnCompletionListener { stopVoice() }
                                    media.setOnErrorListener { _, _, _ -> stopVoice(); status = "Could not play speech. You can still read the answer."; true }
                                    media.setDataSource(file.absolutePath); media.prepareAsync()
                                } catch (e: CancellationException) { file.delete(); throw e }
                                catch (_: Exception) { stopVoice(); status = "Spoken answers are unavailable. You can still read the answer." }
                            }
                        }, enabled = !speaking && !busy && token.isNotBlank())
                        if (speaking) TvButton("Stop audio", { stopVoice() })
                    }
                    if (references.isNotEmpty()) {
                        Text("FIRESPOT REFERENCES", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        references.forEachIndexed { index, text -> Text("${index + 1}. $text", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
                    }
                }
            }
            TvButton(if (setup) "Hide connection settings" else "Connection settings", { setup = !setup }, enabled = !busy && !speaking)
            if (setup) {
                OutlinedTextField(endpoint, { endpoint = it.take(500) }, label = { Text("HTTPS coach endpoint") }, singleLine = true, enabled = !busy && !speaking, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(token, { token = it.take(512) }, label = { Text("Coach access token · this session") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, enabled = !busy && !speaking, modifier = Modifier.fillMaxWidth())
                TvButton("Save connection", { prefs.edit().putString("endpoint", endpoint.trim()).apply(); setup = false; status = "Connection saved." }, enabled = !busy && !speaking)
            }
            Text("Powered by Amazon Bedrock and Polly. Only your question and title are sent for guidance; spoken playback sends the answer for synthesis. Answers may be inaccurate.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
