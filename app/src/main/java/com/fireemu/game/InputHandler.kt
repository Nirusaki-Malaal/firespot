package com.fireemu.game

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

class InputHandler(
    private val onToggleOverlay: () -> Unit
) {
    // Joypad bit positions (Libretro standards)
    companion object {
        const val BTN_B = 0
        const val BTN_Y = 1
        const val BTN_SELECT = 2
        const val BTN_START = 3
        const val BTN_UP = 4
        const val BTN_DOWN = 5
        const val BTN_LEFT = 6
        const val BTN_RIGHT = 7
        const val BTN_A = 8
        const val BTN_X = 9
        const val BTN_L = 10
        const val BTN_R = 11
        const val BTN_L2 = 12
        const val BTN_R2 = 13
        const val BTN_L3 = 14
        const val BTN_R3 = 15
    }

    private var currentBitmask = 0
    private var selectHeld = false
    private var startHeld = false
    private var backDownTime = 0L

    fun getBitmask(): Int = currentBitmask

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isDown = event.action == KeyEvent.ACTION_DOWN

        // Remote Menu button -> In-Game Overlay
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isDown) onToggleOverlay()
            return true
        }

        // Back button: detect long-press (>600ms) or short-press for overlay
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isDown) {
                if (event.repeatCount == 0) {
                    backDownTime = System.currentTimeMillis()
                } else if (System.currentTimeMillis() - backDownTime > 600) {
                    onToggleOverlay()
                    backDownTime = Long.MAX_VALUE
                    return true
                }
            } else {
                if (backDownTime != Long.MAX_VALUE && System.currentTimeMillis() - backDownTime < 600) {
                    onToggleOverlay()
                    return true
                }
            }
            return true
        }

        val retroBtn = mapKeyCodeToRetro(keyCode)
        if (retroBtn >= 0) {
            if (isDown) {
                currentBitmask = currentBitmask or (1 shl retroBtn)
                if (retroBtn == BTN_SELECT) selectHeld = true
                if (retroBtn == BTN_START) startHeld = true
            } else {
                currentBitmask = currentBitmask and (1 shl retroBtn).inv()
                if (retroBtn == BTN_SELECT) selectHeld = false
                if (retroBtn == BTN_START) startHeld = false
            }

            // Gamepad combo: Select + Start toggles overlay menu
            if (selectHeld && startHeld) {
                onToggleOverlay()
                selectHeld = false
                startHeld = false
            }
            return true
        }

        return false
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE
        ) {
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            // Clear hat D-pad bits
            currentBitmask = currentBitmask and ((1 shl BTN_LEFT) or (1 shl BTN_RIGHT) or (1 shl BTN_UP) or (1 shl BTN_DOWN)).inv()

            if (hatX < -0.5f) currentBitmask = currentBitmask or (1 shl BTN_LEFT)
            if (hatX > 0.5f) currentBitmask = currentBitmask or (1 shl BTN_RIGHT)
            if (hatY < -0.5f) currentBitmask = currentBitmask or (1 shl BTN_UP)
            if (hatY > 0.5f) currentBitmask = currentBitmask or (1 shl BTN_DOWN)

            return true
        }
        return false
    }

    private fun mapKeyCodeToRetro(keyCode: Int): Int {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> BTN_UP
            KeyEvent.KEYCODE_DPAD_DOWN -> BTN_DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> BTN_LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> BTN_RIGHT

            // Fire TV Remote Center button maps to A, Play/Pause maps to B
            KeyEvent.KEYCODE_DPAD_CENTER -> BTN_A
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> BTN_B
            KeyEvent.KEYCODE_MEDIA_PLAY -> BTN_B
            KeyEvent.KEYCODE_MEDIA_PAUSE -> BTN_B

            // Gamepad Buttons (Xbox / PS / 8BitDo)
            KeyEvent.KEYCODE_BUTTON_A -> BTN_A
            KeyEvent.KEYCODE_BUTTON_B -> BTN_B
            KeyEvent.KEYCODE_BUTTON_X -> BTN_X
            KeyEvent.KEYCODE_BUTTON_Y -> BTN_Y
            KeyEvent.KEYCODE_BUTTON_L1 -> BTN_L
            KeyEvent.KEYCODE_BUTTON_R1 -> BTN_R
            KeyEvent.KEYCODE_BUTTON_L2 -> BTN_L2
            KeyEvent.KEYCODE_BUTTON_R2 -> BTN_R2
            KeyEvent.KEYCODE_BUTTON_THUMBL -> BTN_L3
            KeyEvent.KEYCODE_BUTTON_THUMBR -> BTN_R3
            KeyEvent.KEYCODE_BUTTON_SELECT -> BTN_SELECT
            KeyEvent.KEYCODE_BUTTON_START -> BTN_START

            else -> -1
        }
    }
}
