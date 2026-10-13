package com.fireemu.launcher.input;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputManager {
    // Retro Joypad Bitmask Constants (Matches Libretro standards)
    public static final int RETRO_DEVICE_ID_JOYPAD_B        = 0;
    public static final int RETRO_DEVICE_ID_JOYPAD_Y        = 1;
    public static final int RETRO_DEVICE_ID_JOYPAD_SELECT   = 2;
    public static final int RETRO_DEVICE_ID_JOYPAD_START    = 3;
    public static final int RETRO_DEVICE_ID_JOYPAD_UP       = 4;
    public static final int RETRO_DEVICE_ID_JOYPAD_DOWN     = 5;
    public static final int RETRO_DEVICE_ID_JOYPAD_LEFT     = 6;
    public static final int RETRO_DEVICE_ID_JOYPAD_RIGHT    = 7;
    public static final int RETRO_DEVICE_ID_JOYPAD_A        = 8;
    public static final int RETRO_DEVICE_ID_JOYPAD_X        = 9;
    public static final int RETRO_DEVICE_ID_JOYPAD_L        = 10;
    public static final int RETRO_DEVICE_ID_JOYPAD_R        = 11;
    public static final int RETRO_DEVICE_ID_JOYPAD_L2       = 12;
    public static final int RETRO_DEVICE_ID_JOYPAD_R2       = 13;
    public static final int RETRO_DEVICE_ID_JOYPAD_L3       = 14;
    public static final int RETRO_DEVICE_ID_JOYPAD_R3       = 15;

    public interface OverlayMenuListener {
        void onToggleOverlayMenu();
    }

    private final SharedPreferences prefs;
    private final OverlayMenuListener overlayListener;
    private int currentInputState = 0;
    private boolean selectHeld = false;
    private boolean startHeld = false;

    public InputManager(Context context, OverlayMenuListener listener) {
        this.prefs = context.getSharedPreferences("fireemu_input_map", Context.MODE_PRIVATE);
        this.overlayListener = listener;
    }

    public int getCurrentInputState() {
        return currentInputState;
    }

    public boolean handleKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;

        // Check for Fire TV Remote Menu button -> In-Game Overlay
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isDown && overlayListener != null) {
                overlayListener.onToggleOverlayMenu();
            }
            return true;
        }

        int retroButton = mapKeyCodeToRetro(keyCode);
        if (retroButton >= 0) {
            if (isDown) {
                currentInputState |= (1 << retroButton);
                if (retroButton == RETRO_DEVICE_ID_JOYPAD_SELECT) selectHeld = true;
                if (retroButton == RETRO_DEVICE_ID_JOYPAD_START) startHeld = true;
            } else {
                currentInputState &= ~(1 << retroButton);
                if (retroButton == RETRO_DEVICE_ID_JOYPAD_SELECT) selectHeld = false;
                if (retroButton == RETRO_DEVICE_ID_JOYPAD_START) startHeld = false;
            }

            // Gamepad combo: Select + Start toggles overlay menu
            if (selectHeld && startHeld && overlayListener != null) {
                overlayListener.onToggleOverlayMenu();
                selectHeld = false;
                startHeld = false;
            }
            return true;
        }

        return false;
    }

    public boolean handleMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            
            // Process D-Pad Hat Axes (Standard Bluetooth controllers)
            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            // Clear previous D-Pad bits from hat
            currentInputState &= ~((1 << RETRO_DEVICE_ID_JOYPAD_LEFT) |
                                  (1 << RETRO_DEVICE_ID_JOYPAD_RIGHT) |
                                  (1 << RETRO_DEVICE_ID_JOYPAD_UP) |
                                  (1 << RETRO_DEVICE_ID_JOYPAD_DOWN));

            if (hatX < -0.5f) currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_LEFT);
            if (hatX > 0.5f)  currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_RIGHT);
            if (hatY < -0.5f) currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_UP);
            if (hatY > 0.5f)  currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_DOWN);

            // Analog thumbstick fallback to D-Pad for consoles without analog
            float stickX = event.getAxisValue(MotionEvent.AXIS_X);
            float stickY = event.getAxisValue(MotionEvent.AXIS_Y);
            if (stickX < -0.6f) currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_LEFT);
            if (stickX > 0.6f)  currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_RIGHT);
            if (stickY < -0.6f) currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_UP);
            if (stickY > 0.6f)  currentInputState |= (1 << RETRO_DEVICE_ID_JOYPAD_DOWN);

            return true;
        }
        return false;
    }

    private int mapKeyCodeToRetro(int keyCode) {
        // Check user custom mapping first
        int custom = prefs.getInt("key_" + keyCode, -1);
        if (custom != -1) return custom;

        // Default Fire TV Remote and Gamepad bindings
        switch (keyCode) {
            // Fire TV Remote D-Pad & Gamepad D-Pad
            case KeyEvent.KEYCODE_DPAD_UP:
                return RETRO_DEVICE_ID_JOYPAD_UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return RETRO_DEVICE_ID_JOYPAD_DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return RETRO_DEVICE_ID_JOYPAD_LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return RETRO_DEVICE_ID_JOYPAD_RIGHT;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                return RETRO_DEVICE_ID_JOYPAD_A; // Remote Center Button = Primary Action A

            // Remote Media Play/Pause -> Secondary Action B / Turbo Toggle
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                return RETRO_DEVICE_ID_JOYPAD_B;

            // Bluetooth Gamepads (Xbox, PlayStation, 8BitDo, Generic HID)
            case KeyEvent.KEYCODE_BUTTON_A:
                return RETRO_DEVICE_ID_JOYPAD_B; // Nintendo B / Xbox A
            case KeyEvent.KEYCODE_BUTTON_B:
                return RETRO_DEVICE_ID_JOYPAD_A; // Nintendo A / Xbox B
            case KeyEvent.KEYCODE_BUTTON_X:
                return RETRO_DEVICE_ID_JOYPAD_Y;
            case KeyEvent.KEYCODE_BUTTON_Y:
                return RETRO_DEVICE_ID_JOYPAD_X;
            case KeyEvent.KEYCODE_BUTTON_L1:
                return RETRO_DEVICE_ID_JOYPAD_L;
            case KeyEvent.KEYCODE_BUTTON_R1:
                return RETRO_DEVICE_ID_JOYPAD_R;
            case KeyEvent.KEYCODE_BUTTON_L2:
                return RETRO_DEVICE_ID_JOYPAD_L2;
            case KeyEvent.KEYCODE_BUTTON_R2:
                return RETRO_DEVICE_ID_JOYPAD_R2;
            case KeyEvent.KEYCODE_BUTTON_START:
                return RETRO_DEVICE_ID_JOYPAD_START;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                return RETRO_DEVICE_ID_JOYPAD_SELECT;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                return RETRO_DEVICE_ID_JOYPAD_L3;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                return RETRO_DEVICE_ID_JOYPAD_R3;

            default:
                return -1;
        }
    }

    public void setCustomMapping(int keyCode, int retroButton) {
        prefs.edit().putInt("key_" + keyCode, retroButton).apply();
    }
}
