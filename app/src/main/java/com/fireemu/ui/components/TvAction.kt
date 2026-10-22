package com.fireemu.ui.components

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/** One explicit keyboard focus target, with pointer and accessibility activation too. */
fun Modifier.tvAction(interaction: MutableInteractionSource, enabled: Boolean = true, action: () -> Unit): Modifier =
    this.onKeyEvent { event ->
        val native=event.nativeKeyEvent
        if(enabled && native.keyCode in listOf(KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER,KeyEvent.KEYCODE_NUMPAD_ENTER,KeyEvent.KEYCODE_BUTTON_A)) {
            if(native.action==KeyEvent.ACTION_UP) action()
            true
        } else false
    }.focusable(enabled, interaction)
        .semantics { role=Role.Button; if(enabled) onClick { action();true } }
        .pointerInput(enabled,action) { detectTapGestures { if(enabled) action() } }
