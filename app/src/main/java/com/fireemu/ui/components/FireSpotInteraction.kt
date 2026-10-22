package com.fireemu.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.fireemu.ui.audio.FireSpotAudio

@Composable
internal fun rememberFireSpotAction(onClick: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(context, view, onClick) {
        { FireSpotAudio.click(context, view); onClick() }
    }
}

@Composable
internal fun FireSpotFocusFeedback(focused: Boolean) {
    val context = LocalContext.current
    var hasFocused by remember { mutableStateOf(false) }
    LaunchedEffect(focused) {
        if (focused) {
            if (hasFocused) FireSpotAudio.navigate(context)
            hasFocused = true
        }
    }
}
