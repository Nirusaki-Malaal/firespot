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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmulatorActivity : Activity(), SurfaceHolder.Callback {

    companion object {
        const val EXTRA_CONSOLE_ID = "EXTRA_CONSOLE_ID"
        const val EXTRA_ROM_PATH = "EXTRA_ROM_PATH"
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"
        const val EXTRA_GAME_TITLE = "EXTRA_GAME_TITLE"
    }

    private var consoleId: String = ""
    private var romPath: String = ""
    private var gameId: Long = 0L
    private var gameTitle: String = ""

    private lateinit var surfaceView: SurfaceView
    private lateinit var overlayContainer: LinearLayout
    private lateinit var tvFps: TextView
    private lateinit var audioEngine: AudioEngine
    private lateinit var inputHandler: InputHandler

    private var emulationThread: Thread? = null
    @Volatile private var isRunning = false
    @Volatile private var isPausedForMenu = false
    @Volatile private var fastForward = false
    @Volatile private var showFps = false

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
        audioEngine.init()

        inputHandler = InputHandler {
            runOnUiThread {
                if (overlayContainer.visibility == View.VISIBLE) {
                    hideOverlay()
                } else {
                    showOverlay()
                }
            }
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
            setTextColor(Color.YELLOW)
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
        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        overlayContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E60F131A")) // 90% dark overlay
            setPadding(48, 36, 48, 36)
            visibility = View.GONE
        }

        buildOverlayViews(overlayContainer)
        scrollView.addView(
            overlayContainer,
            ViewGroup.LayoutParams(
                (420 * resources.displayMetrics.density).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        rootLayout.addView(
            scrollView,
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
            text = "⚡ FIRESPOT IN-GAME"
            setTextColor(Color.parseColor("#FFB800"))
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
            text = "${ConsoleType.fromId(consoleId).displayName} • Low-Latency Direct Core"
            setTextColor(Color.parseColor("#A7B0C4"))
            textSize = 13f
            setPadding(0, 0, 0, 24)
        }
        container.addView(subTitleView)

        btnResume = createOverlayButton("▶  Resume Playing") { hideOverlay() }
        btnSlotSelector = createOverlayButton("State Slot: $selectedSlot") { cycleSlot() }
        btnSaveState = createOverlayButton("💾 Save State (Slot $selectedSlot)") { performSaveState() }
        btnLoadState = createOverlayButton("↺ Load State (Slot $selectedSlot)") { performLoadState() }
        btnScaling = createOverlayButton("Scaling: ${currentScaling.label}") { cycleScaling() }
        btnFastForward = createOverlayButton("Fast Forward: OFF") { toggleFastForward() }
        btnToggleFps = createOverlayButton("Toggle FPS Overlay") { toggleFps() }
        btnReset = createOverlayButton("Reset Game") { performReset() }
        btnExit = createOverlayButton("Save & Exit to FireSpot") { performExit() }
        btnExit.setTextColor(Color.parseColor("#FF3D57"))

        container.addView(btnResume)
        container.addView(btnSlotSelector)
        container.addView(btnSaveState)
        container.addView(btnLoadState)
        container.addView(btnScaling)
        container.addView(btnFastForward)
        container.addView(btnToggleFps)
        container.addView(btnReset)
        container.addView(btnExit)
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
                (48 * resources.displayMetrics.density).toInt()
            )
            lp.bottomMargin = (10 * resources.displayMetrics.density).toInt()
            layoutParams = lp
            setOnClickListener { onClick() }
        }
    }

    private fun showOverlay() {
        isPausedForMenu = true
        audioEngine.pause()
        overlayContainer.visibility = View.VISIBLE
        btnResume.requestFocus()
        updateSlotInfo()
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
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
        LibretroBridge.nativeSaveState(autoPath)
    }

    private fun checkAndLoadAutoState() {
        val autoPath = getStatePath(99)
        val autoFile = File(autoPath)
        if (autoFile.exists() && autoFile.length() > 0) {
            val ok = LibretroBridge.nativeLoadState(autoPath)
            if (ok) {
                runOnUiThread {
                    Toast.makeText(this, "Auto-State Resumed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recordSessionTime() {
        if (gameId > 0) {
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
        val coreLibPath = File(applicationInfo.nativeLibraryDir, console.coreLibName).absolutePath

        LibretroBridge.nativeInitCore(coreLibPath)
        LibretroBridge.nativeSetSurface(holder.surface, 1280, 720)
        val biosDir = File("/sdcard/ROMs/bios").absolutePath
        LibretroBridge.nativeLoadGame(romPath, biosDir)

        checkAndLoadAutoState()
        startEmulationLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        LibretroBridge.nativeSetSurface(holder.surface, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopEmulationLoop()
    }

    private fun startEmulationLoop() {
        isRunning = true
        audioEngine.start()

        emulationThread = Thread {
            val targetFrameNs = 16_666_666L // 60 FPS
            var frameCount = 0
            var fpsTimer = System.currentTimeMillis()

            while (isRunning) {
                val frameStart = System.nanoTime()

                if (!isPausedForMenu) {
                    val input = inputHandler.getBitmask()
                    LibretroBridge.nativeRunFrame(input)
                    if (fastForward) {
                        LibretroBridge.nativeRunFrame(input)
                    }
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
                val sleepNs = if (fastForward) (targetFrameNs / 2) - elapsedNs else targetFrameNs - elapsedNs
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (overlayContainer.visibility == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideOverlay()
                return true
            }
            return super.onKeyDown(keyCode, event)
        }
        if (inputHandler.handleKeyEvent(event)) return true
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (overlayContainer.visibility == View.VISIBLE) {
            return super.onKeyUp(keyCode, event)
        }
        if (inputHandler.handleKeyEvent(event)) return true
        return super.onKeyUp(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (inputHandler.handleMotionEvent(event)) return true
        return super.onGenericMotionEvent(event)
    }

    override fun onPause() {
        super.onPause()
        saveAutoState()
        stopEmulationLoop()
    }

    override fun onResume() {
        super.onResume()
        if (surfaceView.holder.surface.isValid && !isRunning) {
            startEmulationLoop()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        saveAutoState()
    }

    override fun onDestroy() {
        super.onDestroy()
        recordSessionTime()
        stopEmulationLoop()
        audioEngine.release()
        LibretroBridge.nativeDestroy()
    }
}
