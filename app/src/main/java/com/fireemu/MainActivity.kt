package com.fireemu

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.fireemu.ui.navigation.FireEmuNavGraph
import com.fireemu.ui.theme.FireSpotTheme
import com.fireemu.ui.audio.FireSpotAudio
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var inputInspector: ((android.view.InputEvent) -> Boolean)? = null
    private var uiAudioJob: Job? = null

    override fun onStart() {
        super.onStart()
        uiAudioJob = lifecycleScope.launch {
            FireEmuApp.instance.settingsRepository.getSettings().collectLatest { settings ->
                FireSpotAudio.setSoundsEnabled(settings.uiSoundsEnabled)
                FireSpotAudio.setAmbientEnabled(this@MainActivity, settings.uiAmbientEnabled)
            }
        }
    }

    override fun onStop() {
        uiAudioJob?.cancel()
        uiAudioJob = null
        FireSpotAudio.pauseAmbient()
        super.onStop()
    }

    override fun dispatchGenericMotionEvent(event: android.view.MotionEvent): Boolean {
        if (inputInspector?.invoke(event) == true) return true
        return super.dispatchGenericMotionEvent(event)
    }

    // Focused Compose controls handle Escape in the preview layer. These fallbacks
    // cover freshly opened screens that have not acquired a keyboard focus yet.
    override fun onKeyDown(keyCode:Int,event:android.view.KeyEvent):Boolean {
        if (inputInspector?.invoke(event) == true) return true
        if(keyCode==android.view.KeyEvent.KEYCODE_ESCAPE)return true
        return super.onKeyDown(keyCode,event)
    }
    override fun onKeyUp(keyCode:Int,event:android.view.KeyEvent):Boolean {
        if (inputInspector?.invoke(event) == true) return true
        if(keyCode==android.view.KeyEvent.KEYCODE_ESCAPE) {onBackPressedDispatcher.onBackPressed();return true}
        return super.onKeyUp(keyCode,event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireSpotTheme {
                val navController = rememberNavController()
                Box(Modifier.fillMaxSize().onPreviewKeyEvent { event ->
                    val key=event.nativeKeyEvent
                    if(inputInspector?.invoke(key)==true) true
                    else if(key.keyCode==android.view.KeyEvent.KEYCODE_ESCAPE) {
                        if(key.action==android.view.KeyEvent.ACTION_UP) onBackPressedDispatcher.onBackPressed()
                        true
                    } else false
                }) {
                    FireEmuNavGraph(navController = navController, onExitApp = { finish() })
                }
            }
        }
    }
}
