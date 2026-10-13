package com.fireemu

import android.os.Bundle
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
                FireEmuNavGraph(
                    navController = navController,
                    onExitApp = { finish() }
                )
            }
        }
    }
}
