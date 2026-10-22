package com.fireemu.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import com.fireemu.FireEmuApp
import com.fireemu.domain.models.Game
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*

@Composable
fun GameDetailsSheet(viewModel:GameDetailsViewModel,onLaunchGame:(Game,Boolean)->Unit,onNavigateBack:()->Unit) {
    val state by viewModel.uiState.collectAsState()
    val owner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.loadGame()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    if(state.isLoading) {LoadingState("Opening game…");return}
    val game=state.game ?: run {EmptyState("Game unavailable","This game is no longer in the library.","Back",onNavigateBack);return}
    val scope=rememberCoroutineScope()
    var preparing by remember {mutableStateOf(false)}
    var preparationMessage by remember {mutableStateOf("")}
    var prepareJob by remember {mutableStateOf<Job?>(null)}
    fun prepare(resume:Boolean) {
        if(preparing)return
        prepareJob=scope.launch {
            preparing=true;preparationMessage="Preparing console support…"
            try {
                if(!(com.fireemu.BuildConfig.BUNDLED_CORE && game.consoleId=="gameboy")) FireEmuApp.instance.downloads.ensureCore(game.console)
                onLaunchGame(game,resume)
                preparationMessage=""
            } catch(_:CancellationException) {preparationMessage="Preparation cancelled. Your game is still in the library."}
            catch(e:Exception) {preparationMessage=e.message?:"Could not prepare this game. Check your connection and retry."}
            finally {preparing=false}
        }
    }
    val transfer by FireEmuApp.instance.downloads.transfer.collectAsState()
    val firstFocus=remember {FocusRequester()}
    LaunchedEffect(game.id) {firstFocus.requestFocus()}
    Row(Modifier.fillMaxSize().etchedBackdrop()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
            HeaderPillButton("back","Library",onNavigateBack)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(32.dp)) {
                TvFocusableCard(onClick={prepare(false)},modifier=Modifier.width(270.dp).height(250.dp)) {
                    Column {FallbackCover(game,Modifier.fillMaxWidth().height(210.dp));Text("${game.console.shortName}  /  ${game.genre.uppercase()}",fontSize=10.sp,letterSpacing=1.sp,color=FireSpotTextSecondary,modifier=Modifier.padding(12.dp))}
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Text("THE COLLECTION  /  ${game.console.shortName}",color=FireSpotTextSubtitle,fontSize=10.sp,letterSpacing=1.5.sp)
                    Text(game.cleanTitle,color=FireSpotTextPrimary,fontSize=34.sp,lineHeight=38.sp,fontWeight=FontWeight.Light)
                    Text("${game.maxPlayers} PLAYER${if(game.maxPlayers==1) "" else "S"}  /  ${if(game.releaseYear>0) game.releaseYear.toString() else "LOCAL"}  /  ${game.formattedPlayTime}",color=FireSpotTextSecondary,fontSize=11.sp)
                    Text(game.description,color=FireSpotTextSecondary,fontSize=13.sp,lineHeight=20.sp,maxLines=4)
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        TvButton(if(preparing) "Preparing…" else "Play from start",{prepare(false)},Modifier.focusRequester(firstFocus),enabled=!preparing,isPrimary=true)
                        if(state.hasSaveState) TvButton("Resume",{prepare(true)},enabled=!preparing)
                    }
                    if(preparationMessage.isNotEmpty()) Text(if(preparing) "${transfer.message} · ${transfer.progress}%" else preparationMessage,color=FireSpotTextSecondary,fontSize=12.sp)
                    if(preparing) TvButton("Cancel",{prepareJob?.cancel()})
                    HeaderPillButton("heart",if(state.isFavorite) "Remove favorite" else "Add to favorites",{viewModel.toggleFavorite()})
                }
            }
            Text("D-pad: move   /   Select or gamepad A: action   /   Menu: pause, save and controls",color=FireSpotTextSubtitle,fontSize=12.sp)
        }
    }
}
