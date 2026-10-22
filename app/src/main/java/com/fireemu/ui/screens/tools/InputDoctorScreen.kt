package com.fireemu.ui.screens.tools

import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fireemu.MainActivity
import com.fireemu.ui.components.TvButton
import com.fireemu.ui.components.etchedBackdrop
import com.fireemu.ui.components.glassFinish
import com.fireemu.ui.theme.*

/** Screen-scoped observer: never intercepts input during normal navigation or gameplay. */
@Composable
fun InputDoctorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as MainActivity
    val manager = context.getSystemService(android.content.Context.INPUT_SERVICE) as InputManager
    var devices by remember { mutableStateOf(InputDevice.getDeviceIds().toList().mapNotNull { InputDevice.getDevice(it) }) }
    var capturing by remember { mutableStateOf(false) }
    var held by remember { mutableStateOf(emptySet<String>()) }
    var lastKey by remember { mutableStateOf("No buttons pressed yet") }
    var axes by remember { mutableStateOf("Move a stick or press a trigger to see its raw reading.") }
    fun observe(event: android.view.InputEvent): Boolean {
        if (!capturing) return false
        when (event) {
            is KeyEvent -> {
                if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    if (event.action == KeyEvent.ACTION_UP) { capturing = false; held = emptySet() }
                    return true
                }
                val name = "${event.deviceId}: ${KeyEvent.keyCodeToString(event.keyCode)}"
                lastKey = "$name · ${if (event.action == KeyEvent.ACTION_DOWN) "down" else "up"}"
                held = if (event.action == KeyEvent.ACTION_DOWN) held + name else held - name
            }
            is MotionEvent -> {
                axes = event.device?.motionRanges?.filter {
                    (it.source and InputDevice.SOURCE_CLASS_JOYSTICK) != 0
                }?.joinToString("\n") {
                    "${MotionEvent.axisToString(it.axis)}  ${"%.3f".format(event.getAxisValue(it.axis))}  · flat ${"%.3f".format(it.flat)}"
                }?.ifBlank { "No joystick axes reported by this device." } ?: "Device disconnected."
            }
        }
        return true
    }
    val observer by rememberUpdatedState<(android.view.InputEvent) -> Boolean> { observe(it) }
    DisposableEffect(manager, activity) {
        val listener = object : InputManager.InputDeviceListener {
            fun refresh() { devices = InputDevice.getDeviceIds().toList().mapNotNull { InputDevice.getDevice(it) }; held = emptySet() }
            override fun onInputDeviceAdded(deviceId: Int) = refresh()
            override fun onInputDeviceRemoved(deviceId: Int) = refresh()
            override fun onInputDeviceChanged(deviceId: Int) = refresh()
        }
        manager.registerInputDeviceListener(listener, android.os.Handler(android.os.Looper.getMainLooper()))
        activity.inputInspector = { observer(it) }
        val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) { capturing = false; held = emptySet() }
        }
        activity.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            activity.inputInspector = null
            manager.unregisterInputDeviceListener(listener)
            activity.lifecycle.removeObserver(lifecycleObserver)
        }
    }
    BackHandler(capturing) { capturing = false; held = emptySet() }
    Column(Modifier.fillMaxSize().onPreviewKeyEvent { observe(it.nativeKeyEvent) }.etchedBackdrop()
        .verticalScroll(rememberScrollState()).padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Controller & keyboard tester", color = FireSpotTextPrimary, fontSize = 28.sp)
        Text("Check a remote, USB controller or wireless keyboard. Pair Bluetooth devices in your TV’s system settings.", color = FireSpotTextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TvButton(if (capturing) "Stop testing" else "Start live test", { capturing = !capturing; held = emptySet() })
            TvButton("Back", onBack)
        }
        Text(if (capturing) "LIVE · Press Back or Escape to stop. Other buttons stay inside this tester." else "Navigation mode · Start a live test to capture buttons and sticks.", color = FireSpotTextPrimary)
        Text(lastKey, color = FireSpotTextSecondary)
        Text("Held: ${held.joinToString().ifBlank { "None" }}", color = FireSpotTextPrimary)
        Text(axes, color = FireSpotTextSecondary, fontSize = 13.sp)
        Text("CONNECTED INPUT DEVICES · ${devices.size}", color = FireSpotTextSubtitle, fontSize = 12.sp)
        devices.forEach { device ->
            Column(Modifier.fillMaxWidth().glassFinish().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(device.name, color = FireSpotTextPrimary, fontSize = 17.sp)
                Text("ID ${device.id} · Vendor ${device.vendorId} · Product ${device.productId} · ${if (device.isVirtual) "Virtual" else "Physical"}", color = FireSpotTextSecondary, fontSize = 12.sp)
                Text("${if (device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) "Keyboard · " else ""}${device.motionRanges.size} motion axes", color = FireSpotTextSecondary, fontSize = 12.sp)
            }
        }
        Text("Raw values help spot drift. This screen reports Android’s readings; it does not change calibration or Bluetooth pairing.", color = FireSpotTextSubtitle, fontSize = 12.sp)
    }
}
