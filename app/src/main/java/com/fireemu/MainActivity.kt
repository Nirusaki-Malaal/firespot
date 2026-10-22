package com.fireemu

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.fireemu.ui.navigation.FireEmuNavGraph
import com.fireemu.ui.theme.FireSpotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireSpotTheme {
                val navController = rememberNavController()
                Box(Modifier.fillMaxSize().onPreviewKeyEvent { event ->
                    val key=event.nativeKeyEvent
                    if(key.keyCode==android.view.KeyEvent.KEYCODE_ESCAPE) {
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
