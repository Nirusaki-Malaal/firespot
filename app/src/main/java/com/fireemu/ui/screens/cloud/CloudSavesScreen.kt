package com.fireemu.ui.screens.cloud

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
import com.fireemu.BuildConfig
import com.fireemu.data.cloud.CloudSaves
import com.fireemu.ui.components.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File

@Composable
fun CloudSavesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("cloud_saves", 0) }
    var endpoint by rememberSaveable { mutableStateOf(prefs.getString("endpoint", BuildConfig.CLOUD_ENDPOINT) ?: "") }
    var region by rememberSaveable { mutableStateOf(prefs.getString("region", BuildConfig.COGNITO_REGION) ?: "") }
    var client by rememberSaveable { mutableStateOf(prefs.getString("client", BuildConfig.COGNITO_CLIENT_ID) ?: "") }
    var setup by remember { mutableStateOf(endpoint.isBlank()) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var confirming by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("Your saves stay local until you choose to back them up.") }
    var restoreConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun runJob(label: String, job: suspend () -> String) {
        if (busy) return
        busy = true; status = label
        scope.launch {
            try { status = job() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { status = e.message ?: "Request failed. Please try again." }
            finally { busy = false }
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color.White)) {
        Column(Modifier.fillMaxSize().etchedBackdrop().verticalScroll(rememberScrollState()).padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Cloud saves", fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                TvButton("Back", onBack, enabled = !busy)
            }
            Text("Keep your progress. Pick it up on another FireSpot installation.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.fillMaxWidth().glassFinish().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (token == null) "SIGN IN · AMAZON COGNITO" else "CONNECTED · $email", color = MaterialTheme.colorScheme.onSurface)
                if (token == null) {
                    OutlinedTextField(email, { email = it.take(254) }, label = { Text("Email") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(password, { password = it.take(256) }, label = { Text("Password · 12+ characters, upper/lowercase, number, symbol") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvButton("Sign in", {
                            runJob("Signing in…") {
                                val result = withContext(Dispatchers.IO) { CloudSaves.cognito(region.trim(), client.trim(), "InitiateAuth", JSONObject().put("AuthFlow", "USER_PASSWORD_AUTH").put("AuthParameters", JSONObject().put("USERNAME", email.trim()).put("PASSWORD", password))) }
                                val auth = result.optJSONObject("AuthenticationResult") ?: error("Sign-in requires an additional challenge. Complete account setup and try again.")
                                token = auth.getString("IdToken"); password = ""; confirming = false
                                "Signed in. Back up your saves or restore your latest cloud backup."
                            }
                        }, enabled = !busy && email.isNotBlank() && password.isNotBlank())
                        TvButton("Create account", {
                            runJob("Creating account…") {
                                val result = withContext(Dispatchers.IO) { CloudSaves.cognito(region.trim(), client.trim(), "SignUp", JSONObject().put("Username", email.trim()).put("Password", password)) }
                                confirming = !result.optBoolean("UserConfirmed")
                                if (confirming) "Check your email for a verification code." else "Account created. You can sign in."
                            }
                        }, enabled = !busy && email.isNotBlank() && password.isNotBlank())
                        TvButton("Verify email", { confirming = true }, enabled = !busy)
                    }
                    if (confirming) {
                        OutlinedTextField(code, { code = it.take(12) }, label = { Text("Email verification code") }, enabled = !busy)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TvButton("Confirm code", { runJob("Verifying…") {
                                withContext(Dispatchers.IO) { CloudSaves.cognito(region.trim(), client.trim(), "ConfirmSignUp", JSONObject().put("Username", email.trim()).put("ConfirmationCode", code.trim())) }
                                confirming = false; code = ""; "Email verified. Sign in to continue."
                            } }, enabled = !busy && code.isNotBlank())
                            TvButton("Resend code", { runJob("Sending code…") {
                                withContext(Dispatchers.IO) { CloudSaves.cognito(region.trim(), client.trim(), "ResendConfirmationCode", JSONObject().put("Username", email.trim())) }; "Verification code sent."
                            } }, enabled = !busy && email.isNotBlank())
                        }
                    }
                } else {
                    Text("Backups include save slots and battery progress. Games and BIOS files are excluded. Use the same game filename and compatible core on the other device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvButton("Back up now", { runJob("Backing up saves…") {
                            withContext(Dispatchers.IO) {
                                val zip = File.createTempFile("saves-", ".zip", context.cacheDir)
                                try {
                                    val count = CloudSaves.archive(context.filesDir, zip)
                                    val post = CloudSaves.request(endpoint.trim(), token = token).getJSONObject("upload")
                                    CloudSaves.upload(post, zip); "Backed up $count save files. Local progress is unchanged."
                                } finally { zip.delete() }
                            }
                        } }, enabled = !busy, isPrimary = true)
                        TvButton("Restore latest", { restoreConfirmation = true }, enabled = !busy)
                        TvButton("Sign out", { token = null; password = ""; status = "Signed out. Local progress is still available." }, enabled = !busy)
                    }
                }
            }
            Text(status, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
            TvButton(if (setup) "Hide connection settings" else "Connection settings", { setup = !setup }, enabled = !busy && token == null)
            if (setup) {
                Text("Use the three outputs from your FireSpot cloud stack. These are public configuration values, not AWS credentials.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(endpoint, { endpoint = it.take(500) }, label = { Text("HTTPS backups endpoint") }, enabled = !busy && token == null, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(region, { region = it.take(50) }, label = { Text("AWS region") }, enabled = !busy && token == null)
                OutlinedTextField(client, { client = it.take(128) }, label = { Text("Cognito app client ID") }, enabled = !busy && token == null, modifier = Modifier.fillMaxWidth())
                TvButton("Save connection", { prefs.edit().putString("endpoint", endpoint.trim()).putString("region", region.trim()).putString("client", client.trim()).apply(); setup = false; status = "Connection saved. Sign in to continue." }, enabled = !busy && token == null)
            }
            Text("Manual backups · newest 10 shown by the service · cloud retention 30 days · newer local saves are preserved during restore", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (restoreConfirmation) AlertDialog(onDismissRequest = { restoreConfirmation = false }, title = { Text("Restore latest backup?") }, text = { Text("This replaces older local saves and adds missing saves. Newer local saves are kept. Close any running game first.") }, confirmButton = {
            TvButton("Restore", { restoreConfirmation = false; runJob("Restoring saves…") {
                withContext(Dispatchers.IO) {
                    val backups = CloudSaves.request(endpoint.trim(), "GET", token = token).getJSONArray("backups")
                    check(backups.length() > 0) { "No cloud backups yet. Back up this device first." }
                    val zip = File.createTempFile("restore-", ".zip", context.cacheDir)
                    val staging = File(context.filesDir, "restore-" + java.util.UUID.randomUUID())
                    try {
                        CloudSaves.download(backups.getJSONObject(0).getString("url"), zip)
                        val (restored, skipped) = CloudSaves.restore(context.filesDir, zip, staging)
                        "Restored $restored files. Kept $skipped current local files."
                    } finally { zip.delete(); staging.deleteRecursively() }
                }
            } })
        }, dismissButton = { TvButton("Cancel", { restoreConfirmation = false }) })
    }
}
