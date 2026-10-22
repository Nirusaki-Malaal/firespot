package com.fireemu.game

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.fireemu.FireEmuApp
import com.fireemu.domain.models.ConsoleType
import com.fireemu.domain.models.VideoScaling
import com.fireemu.nativebridge.LibretroBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmulatorActivity : Activity(), SurfaceHolder.Callback, android.hardware.input.InputManager.InputDeviceListener {

    companion object {
        const val EXTRA_CONSOLE_ID = "EXTRA_CONSOLE_ID"
        const val EXTRA_ROM_PATH = "EXTRA_ROM_PATH"
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"
        const val EXTRA_RESUME = "EXTRA_RESUME"
        const val EXTRA_GAME_TITLE = "EXTRA_GAME_TITLE"
    }

    private var consoleId: String = ""
    private var romPath: String = ""
    private var gameId: Long = 0L
    private var gameTitle: String = ""

    private lateinit var surfaceView: SurfaceView
    private lateinit var overlayScroll: ScrollView
    private lateinit var overlayContainer: LinearLayout
    private lateinit var tvFps: TextView
    private lateinit var audioEngine: AudioEngine
    private lateinit var inputHandler: InputHandler

    private var emulationThread: Thread? = null
    @Volatile private var isRunning = false
    @Volatile private var isPausedForMenu = false
    @Volatile private var fastForward = false
    @Volatile private var showFps = false
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var coreReady = false
    private var sessionRecorded = false
    private var frameDurationNs = 16_666_666L

    private var selectedSlot = 1
    private var currentScaling = VideoScaling.ASPECT_RATIO
    private var sessionStartTime = 0L

    // Overlay buttons
    private lateinit var btnResume: Button
    private lateinit var btnSaveState: Button
    private lateinit var btnLoadState: Button
    private lateinit var btnSlotSelector: Button
    private lateinit var btnScaling: Button
    private lateinit var btnFastForward: Button
    private lateinit var btnToggleFps: Button
    private lateinit var btnReset: Button
    private lateinit var btnExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        consoleId = intent.getStringExtra(EXTRA_CONSOLE_ID) ?: "nes"
        romPath = intent.getStringExtra(EXTRA_ROM_PATH) ?: ""
        gameId = intent.getLongExtra(EXTRA_GAME_ID, 0L)
        gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: File(romPath).nameWithoutExtension

        sessionStartTime = System.currentTimeMillis()

        setupLayout()

        audioEngine = AudioEngine()

        inputHandler = InputHandler {
            runOnUiThread {
                if (overlayContainer.visibility == View.VISIBLE) {
                    hideOverlay()
                } else {
                    showOverlay()
                }
            }
        }
        (getSystemService(INPUT_SERVICE) as android.hardware.input.InputManager).registerInputDeviceListener(this, null)
        loadPreferences()
    }

    private fun loadPreferences() {
        activityScope.launch {
            val repo = FireEmuApp.instance.settingsRepository
            val settings = repo.getSettings().first()
            currentScaling = settings.videoScaling
            showFps = settings.showFps
            tvFps.visibility = if(showFps) View.VISIBLE else View.GONE
            btnScaling.text = "Scaling: ${currentScaling.label}"
            LibretroBridge.nativeSetScaling(currentScaling.id)
            inputHandler.setBindings(repo.getKeyBindings())
        }
    }

    private fun setupLayout() {
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        surfaceView = SurfaceView(this).apply {
            holder.addCallback(this@EmulatorActivity)
            isFocusable = false
        }
        rootLayout.addView(
            surfaceView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        // FPS Counter text view
        tvFps = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.MONOSPACE
            setPadding(24, 24, 24, 24)
            visibility = View.GONE
        }
        rootLayout.addView(
            tvFps,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            )
        )

        // In-game Pause Overlay Menu
        overlayScroll = ScrollView(this).apply {
            visibility = View.GONE
            isFillViewport = true
        }

        overlayContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E6131313")) // 90% dark overlay
            setPadding(48, 36, 48, 36)
            visibility = View.GONE
        }

        buildOverlayViews(overlayContainer)
        overlayScroll.addView(
            overlayContainer,
            ViewGroup.LayoutParams(
                (420 * resources.displayMetrics.density).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        rootLayout.addView(
            overlayScroll,
            FrameLayout.LayoutParams(
                (420 * resources.displayMetrics.density).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END
            )
        )

        setContentView(rootLayout)
    }

    private fun buildOverlayViews(container: LinearLayout) {
        val badgeView = TextView(this).apply {
            text = "FIRESPOT / PAUSED"
            setTextColor(Color.parseColor("#BABABA"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 4)
        }
        container.addView(badgeView)

        val titleView = TextView(this).apply {
            text = gameTitle.uppercase()
            setTextColor(Color.WHITE)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 4)
        }
        container.addView(titleView)

        val subTitleView = TextView(this).apply {
            text = "${ConsoleType.fromId(consoleId).displayName} / ${FireEmuApp.instance.downloads.core(ConsoleType.fromId(consoleId))?.title ?: "Libretro"}"
            setTextColor(Color.parseColor("#B0B0B0"))
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }
        container.addView(subTitleView)

        btnResume = createOverlayButton("Resume playing") { hideOverlay() }
        btnSlotSelector = createOverlayButton("State Slot: $selectedSlot") { cycleSlot() }
        btnSaveState = createOverlayButton("Save state (Slot $selectedSlot)") { performSaveState() }
        btnLoadState = createOverlayButton("Load state (Slot $selectedSlot)") { performLoadState() }
        btnScaling = createOverlayButton("Scaling: ${currentScaling.label}") { cycleScaling() }
        btnFastForward = createOverlayButton("Fast Forward: OFF") { toggleFastForward() }
        btnToggleFps = createOverlayButton("Toggle FPS Overlay") { toggleFps() }
        btnReset = createOverlayButton("Reset Game") { performReset() }
        btnExit = createOverlayButton("Save & Exit to FireSpot") { performExit() }
        btnExit.setTextColor(Color.WHITE)

        container.addView(btnResume)
        container.addView(btnExit)
        container.addView(createOverlayButton("Controls guide") {
            android.app.AlertDialog.Builder(this).setTitle("Game controls")
                .setMessage("D-pad: move\nSelect: A\nPlay/Pause: B\nFast Forward / gamepad Start: Start\nRewind / gamepad Select: Select\nMenu / Back: pause\nKeyboard: arrows / WASD move; Z / J = A; X / K = B; Enter = Start; Right Shift = Select; Escape = pause; C/V = X/Y; Q/E = L/R.\nSaved remaps apply to gameplay. System Menu and Back remain reserved.")
                .setPositiveButton("Back", null).show()
        })
        container.addView(btnSlotSelector)
        container.addView(btnSaveState)
        container.addView(btnLoadState)
        container.addView(btnScaling)
        container.addView(btnToggleFps)
        container.addView(btnReset)
    }

    private fun createOverlayButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 15f
            isFocusable = true
            isFocusableInTouchMode = false
            setBackgroundResource(com.fireemu.R.drawable.card_focus_selector)
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (40 * resources.displayMetrics.density).toInt()
            )
            lp.bottomMargin = (6 * resources.displayMetrics.density).toInt()
            layoutParams = lp
            setOnClickListener { onClick() }
        }
    }

    private fun showOverlay() {
        inputHandler.clear()
        isPausedForMenu = true
        audioEngine.pause()
        overlayScroll.visibility = View.VISIBLE
        overlayContainer.visibility = View.VISIBLE
        btnResume.requestFocusFromTouch()
        updateSlotInfo()
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
        overlayScroll.visibility = View.GONE
        inputHandler.clear()
        isPausedForMenu = false
        audioEngine.start()
    }

    private fun cycleSlot() {
        selectedSlot = if (selectedSlot >= 4) 1 else selectedSlot + 1
        updateSlotInfo()
    }

    private fun updateSlotInfo() {
        btnSlotSelector.text = "State Slot: $selectedSlot"
        val stateFile = File(getStatePath(selectedSlot))
        if (stateFile.exists()) {
            val date = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(stateFile.lastModified()))
            btnSaveState.text = "Overwrite Slot $selectedSlot ($date)"
            btnLoadState.text = "Load State Slot $selectedSlot ($date)"
            btnLoadState.isEnabled = true
        } else {
            btnSaveState.text = "Save State (Slot $selectedSlot - Empty)"
            btnLoadState.text = "Load State (Slot $selectedSlot - Empty)"
            btnLoadState.isEnabled = false
        }
    }

    private fun cycleScaling() {
        currentScaling = when (currentScaling) {
            VideoScaling.ASPECT_RATIO -> VideoScaling.INTEGER_SCALE
            VideoScaling.INTEGER_SCALE -> VideoScaling.STRETCH_FULLSCREEN
            VideoScaling.STRETCH_FULLSCREEN -> VideoScaling.ASPECT_RATIO
        }
        LibretroBridge.nativeSetScaling(currentScaling.id)
        activityScope.launch { FireEmuApp.instance.settingsRepository.setVideoScaling(currentScaling) }
        btnScaling.text = "Scaling: ${currentScaling.label}"
        Toast.makeText(this, "Scaling: ${currentScaling.label}", Toast.LENGTH_SHORT).show()
    }

    private fun toggleFastForward() {
        fastForward = !fastForward
        btnFastForward.text = "Fast Forward: ${if (fastForward) "2x (Active)" else "OFF"}"
    }

    private fun toggleFps() {
        showFps = !showFps
        tvFps.visibility = if (showFps) View.VISIBLE else View.GONE
        activityScope.launch { FireEmuApp.instance.settingsRepository.setFpsVisible(showFps) }
    }

    private fun performSaveState() {
        val path = getStatePath(selectedSlot)
        val ok = LibretroBridge.nativeSaveState(path)
        Toast.makeText(this, if (ok) "Slot $selectedSlot Saved" else "Save Failed", Toast.LENGTH_SHORT).show()
        updateSlotInfo()
        hideOverlay()
    }

    private fun performLoadState() {
        val path = getStatePath(selectedSlot)
        val ok = LibretroBridge.nativeLoadState(path)
        Toast.makeText(this, if (ok) "Slot $selectedSlot Loaded" else "Load Failed", Toast.LENGTH_SHORT).show()
        hideOverlay()
    }

    private fun performReset() {
        LibretroBridge.nativeReset()
        Toast.makeText(this, "Console Reset", Toast.LENGTH_SHORT).show()
        hideOverlay()
    }

    private fun performExit() {
        saveAutoState()
        recordSessionTime()
        finish()
    }

    private fun getStatePath(slot: Int): String {
        val dir = File(filesDir, "states/$consoleId")
        if (!dir.exists()) dir.mkdirs()
        val romBase = File(romPath).name
        return File(dir, "$romBase.state$slot").absolutePath
    }

    private fun saveAutoState() {
        val autoPath = getStatePath(99)
        LibretroBridge.nativeBatteryRam(batteryPath(), true)
        val next = File("$autoPath.next")
        if(LibretroBridge.nativeSaveState(next.absolutePath)) {
            try {
                val current=File(autoPath);val first=File("$autoPath.bak1");val second=File("$autoPath.bak2")
                if(first.isFile) first.copyTo(second,overwrite=true)
                if(current.isFile) current.copyTo(first,overwrite=true)
                check(next.renameTo(current))
            } catch(_:Exception) {next.delete()}
        }
    }

    private fun batteryPath():String {
        val dir=File(filesDir,"battery/$consoleId").apply {mkdirs()}
        val id=java.security.MessageDigest.getInstance("SHA-256").digest(romPath.toByteArray()).joinToString("") {"%02x".format(it)}
        return File(dir,"$id.srm").absolutePath
    }

    private fun checkAndLoadAutoState() {
        val autoPath = getStatePath(99)
        val candidates=listOf(autoPath,"$autoPath.bak1","$autoPath.bak2")
        for((index,path) in candidates.withIndex()) {
            val file=File(path)
            if(file.isFile && file.length()>0 && LibretroBridge.nativeLoadState(path)) {
                android.util.Log.i("FireSpotRuntime",if(index==0) "Autosave restored" else "Recovered autosave backup $index")
                Toast.makeText(this,if(index==0) "Auto-State Resumed" else "Recovered an earlier autosave",Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this,"Save could not be restored. Starting the game with cartridge progress.",Toast.LENGTH_LONG).show()
    }

    private fun recordSessionTime() {
        if (gameId > 0 && !sessionRecorded) {
            sessionRecorded = true;
            val elapsedSec = (System.currentTimeMillis() - sessionStartTime) / 1000
            if (elapsedSec > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FireEmuApp.instance.updatePlayStatsUseCase(gameId, elapsedSec)
                    } catch (ignored: Exception) {}
                }
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val console = ConsoleType.fromId(consoleId)
        try {
            if (!coreReady) {
                val cores = FireEmuApp.instance.coreRepository
                check(cores.isCoreAvailable(console)) { "Console support is not installed. Return to setup to add it." }
                check(LibretroBridge.nativeInitCore(cores.getCorePath(console))) { "Console software could not load on this device." }
                check(LibretroBridge.nativeLoadGame(romPath, cores.getBiosDirectory())) { "The game could not be loaded." }
                val timing = checkNotNull(LibretroBridge.nativeGetTiming())
                check(timing[0].isFinite() && timing[0] in 1.0..240.0 && timing[1].isFinite() && timing[1] in 8000.0..192000.0) { "Invalid console timing." }
                frameDurationNs = (1_000_000_000.0 / timing[0]).toLong()
                audioEngine.init(timing[1].toInt())
                coreReady = true
                LibretroBridge.nativeBatteryRam(batteryPath(), false)
                if (intent.getBooleanExtra(EXTRA_RESUME, false)) checkAndLoadAutoState()
            }
            check(LibretroBridge.nativeSetSurface(holder.surface, 1280, 720))
            startEmulationLoop()
        } catch (error: Exception) {
            showLaunchError(error.message ?: "The game could not start.")
        } catch (error: LinkageError) {
            showLaunchError("The native runtime could not load on this device.")
        }
    }

    private fun showLaunchError(message: String) {
        android.util.Log.e("FireSpotRuntime", message)
        android.app.AlertDialog.Builder(this).setTitle("Unable to start game")
            .setMessage(message).setPositiveButton("Back to library") { _, _ -> finish() }
            .setOnCancelListener { finish() }.show()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        LibretroBridge.nativeSetSurface(holder.surface, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopEmulationLoop()
    }

    private fun startEmulationLoop() {
        if (!coreReady || isRunning) return
        isRunning = true
        audioEngine.start()

        emulationThread = Thread {
            val targetFrameNs = frameDurationNs
            var frameCount = 0
            var fpsTimer = System.currentTimeMillis()

            while (isRunning) {
                val frameStart = System.nanoTime()

                if (!isPausedForMenu) {
                    val input = inputHandler.getBitmask()
                    LibretroBridge.nativeRunFrame(input)

                    frameCount++
                }

                val now = System.currentTimeMillis()
                if (now - fpsTimer >= 1000) {
                    val fps = frameCount
                    frameCount = 0
                    fpsTimer = now
                    if (showFps) {
                        runOnUiThread {
                            tvFps.text = "FPS: $fps"
                        }
                    }
                }

                val elapsedNs = System.nanoTime() - frameStart
                val sleepNs = targetFrameNs - elapsedNs
                if (sleepNs > 0) {
                    try {
                        Thread.sleep(sleepNs / 1_000_000L, (sleepNs % 1_000_000L).toInt())
                    } catch (ignored: InterruptedException) {}
                }
            }
        }.apply {
            priority = Thread.MAX_PRIORITY
            name = "FireEmu-EmulationThread"
            start()
        }
    }

    private fun stopEmulationLoop() {
        isRunning = false
        try {
            emulationThread?.join(500)
        } catch (ignored: InterruptedException) {}
        emulationThread = null
        audioEngine.pause()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!::inputHandler.isInitialized) return super.dispatchKeyEvent(event)
        if (overlayContainer.visibility == View.VISIBLE) {
            if (event.keyCode in listOf(KeyEvent.KEYCODE_BACK,KeyEvent.KEYCODE_ESCAPE,KeyEvent.KEYCODE_F1,KeyEvent.KEYCODE_MENU)) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) hideOverlay()
                return true
            }
            return super.dispatchKeyEvent(event)
        }
        // Capture before an invisible/focused View can consume game buttons.
        if (inputHandler.handleKeyEvent(event)) return true
        return super.dispatchKeyEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (overlayContainer.visibility == View.VISIBLE) return super.onGenericMotionEvent(event)
        if (inputHandler.handleMotionEvent(event)) return true
        return super.onGenericMotionEvent(event)
    }

    override fun onPause() {
        super.onPause()
        inputHandler.clear()
        stopEmulationLoop()
        if (coreReady) saveAutoState()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && ::inputHandler.isInitialized) inputHandler.clear()
    }

    override fun onResume() {
        super.onResume()
        if (coreReady && surfaceView.holder.surface.isValid && !isRunning) {
            startEmulationLoop()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (coreReady) saveAutoState()
    }

    override fun onInputDeviceAdded(deviceId:Int) {}
    override fun onInputDeviceChanged(deviceId:Int) { if(::inputHandler.isInitialized) inputHandler.clear() }
    override fun onInputDeviceRemoved(deviceId:Int) { if(::inputHandler.isInitialized) inputHandler.clear() }

    override fun onDestroy() {
        (getSystemService(INPUT_SERVICE) as android.hardware.input.InputManager).unregisterInputDeviceListener(this)
        super.onDestroy()
        recordSessionTime()
        stopEmulationLoop()
        activityScope.cancel()
        audioEngine.release()
        try { LibretroBridge.nativeDestroy() } catch (_: LinkageError) { }
    }
}
