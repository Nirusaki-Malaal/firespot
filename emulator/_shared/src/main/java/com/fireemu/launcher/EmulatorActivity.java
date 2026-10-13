package com.fireemu.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fireemu.launcher.input.InputManager;
import com.fireemu.launcher.jni.LibretroBridge;
import com.fireemu.launcher.settings.PerformanceManager;
import com.fireemu.launcher.storage.RomScanner;

import java.io.File;

public class EmulatorActivity extends Activity implements SurfaceHolder.Callback, InputManager.OverlayMenuListener {
    private String console;
    private String romPath;
    private SurfaceView surfaceView;
    private LinearLayout overlayMenuContainer;
    private InputManager inputManager;
    private PerformanceManager perfManager;

    private AudioTrack audioTrack;
    private Thread emulationThread;
    private volatile boolean isRunning = false;
    private volatile boolean isPausedForMenu = false;
    private volatile boolean fastForward = false;
    private volatile boolean showFps = false;

    private int currentSaveSlot = 1;
    private int currentPreset = PerformanceManager.PRESET_BALANCED;
    private int currentScalingMode = 0; // 0: Aspect, 1: Integer, 2: Stretch
    private long backDownTimestamp = 0L;

    private Button btnMenuResume;
    private Button btnMenuSave;
    private Button btnMenuLoad;
    private Button btnMenuSlot;
    private Button btnMenuPreset;
    private Button btnMenuReset;
    private Button btnMenuExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator);

        console = getIntent().getStringExtra("EXTRA_CONSOLE");
        if (console == null) console = getIntent().getStringExtra("EXTRA_CONSOLE_ID");
        if (console == null) console = RomScanner.CONSOLE_NES;

        romPath = getIntent().getStringExtra("EXTRA_ROM_PATH");
        if (romPath == null) romPath = "";

        perfManager = new PerformanceManager(this);
        currentPreset = perfManager.getPresetForConsole(console);
        inputManager = new InputManager(this, this);

        surfaceView = findViewById(R.id.emulator_surface);
        surfaceView.getHolder().addCallback(this);

        overlayMenuContainer = findViewById(R.id.overlay_menu_container);
        setupOverlayMenu();
        setupAudio();
    }

    private void setupAudio() {
        int sampleRate = 48000;
        int minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        );
        int bufSize = Math.max(minBuf, 1600 * 2 * 2);

        audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize,
            AudioTrack.MODE_STREAM
        );

        LibretroBridge.setAudioCallback((samples, numSamples) -> {
            if (audioTrack != null && isRunning && !isPausedForMenu) {
                audioTrack.write(samples, 0, numSamples);
            }
        });
    }

    private void setupOverlayMenu() {
        btnMenuResume = findViewById(R.id.btn_menu_resume);
        btnMenuSave = findViewById(R.id.btn_menu_save);
        btnMenuLoad = findViewById(R.id.btn_menu_load);
        btnMenuSlot = findViewById(R.id.btn_menu_slot);
        btnMenuPreset = findViewById(R.id.btn_menu_preset);
        btnMenuReset = findViewById(R.id.btn_menu_reset);
        btnMenuExit = findViewById(R.id.btn_menu_exit);

        btnMenuResume.setOnClickListener(v -> hideOverlayMenu());

        btnMenuSave.setOnClickListener(v -> {
            String statePath = getStateFilePath(currentSaveSlot);
            boolean ok = LibretroBridge.nativeSaveState(statePath);
            Toast.makeText(this, ok ? "Saved to Slot " + currentSaveSlot : "Save State Failed", Toast.LENGTH_SHORT).show();
            updateSlotButtonLabels();
            hideOverlayMenu();
        });

        btnMenuLoad.setOnClickListener(v -> {
            String statePath = getStateFilePath(currentSaveSlot);
            boolean ok = LibretroBridge.nativeLoadState(statePath);
            Toast.makeText(this, ok ? "Loaded Slot " + currentSaveSlot : "Load State Failed", Toast.LENGTH_SHORT).show();
            hideOverlayMenu();
        });

        btnMenuSlot.setOnClickListener(v -> {
            currentSaveSlot = (currentSaveSlot >= 4) ? 1 : currentSaveSlot + 1;
            updateSlotButtonLabels();
        });

        btnMenuPreset.setOnClickListener(v -> {
            currentPreset = (currentPreset + 1) % 3;
            perfManager.setPresetForConsole(console, currentPreset);
            updatePresetButtonLabel();
        });
        updatePresetButtonLabel();
        updateSlotButtonLabels();

        btnMenuReset.setOnClickListener(v -> {
            LibretroBridge.nativeReset();
            Toast.makeText(this, "Console Reset", Toast.LENGTH_SHORT).show();
            hideOverlayMenu();
        });

        btnMenuExit.setOnClickListener(v -> {
            saveAutoState();
            finish();
        });
    }

    private void updateSlotButtonLabels() {
        btnMenuSlot.setText("Save Slot: " + currentSaveSlot + " of 4");
        File file = new File(getStateFilePath(currentSaveSlot));
        if (file.exists()) {
            btnMenuSave.setText("Overwrite Slot " + currentSaveSlot);
            btnMenuLoad.setText("Load State (Slot " + currentSaveSlot + ")");
            btnMenuLoad.setEnabled(true);
        } else {
            btnMenuSave.setText("Save State (Slot " + currentSaveSlot + " - Empty)");
            btnMenuLoad.setText("Load State (Slot " + currentSaveSlot + " - Empty)");
            btnMenuLoad.setEnabled(false);
        }
    }

    private void updatePresetButtonLabel() {
        String name = "Balanced (60fps)";
        if (currentPreset == PerformanceManager.PRESET_PERFORMANCE) name = "Performance";
        if (currentPreset == PerformanceManager.PRESET_QUALITY) name = "Quality (HQ)";
        btnMenuPreset.setText("Preset: " + name);
    }

    private String getStateFilePath(int slot) {
        File dir = new File(getFilesDir(), "states/" + console);
        if (!dir.exists()) dir.mkdirs();
        String romName = new File(romPath).getName();
        return new File(dir, romName + ".state" + slot).getAbsolutePath();
    }

    private void saveAutoState() {
        String autoPath = getStateFilePath(99);
        LibretroBridge.nativeSaveState(autoPath);
    }

    private void loadAutoState() {
        String autoPath = getStateFilePath(99);
        File file = new File(autoPath);
        if (file.exists() && file.length() > 0) {
            boolean ok = LibretroBridge.nativeLoadState(autoPath);
            if (ok) {
                runOnUiThread(() -> Toast.makeText(this, "Resumed Auto-State", Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Override
    public void onToggleOverlayMenu() {
        runOnUiThread(() -> {
            if (overlayMenuContainer.getVisibility() == View.VISIBLE) {
                hideOverlayMenu();
            } else {
                showOverlayMenu();
            }
        });
    }

    private void showOverlayMenu() {
        isPausedForMenu = true;
        overlayMenuContainer.setVisibility(View.VISIBLE);
        updateSlotButtonLabels();
        btnMenuResume.requestFocus();
    }

    private void hideOverlayMenu() {
        overlayMenuContainer.setVisibility(View.GONE);
        isPausedForMenu = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        String coreLib = getCoreLibraryForConsole(console);
        LibretroBridge.nativeInitCore(coreLib);
        LibretroBridge.nativeSetSurface(holder.getSurface(), 1280, 720);
        LibretroBridge.nativeLoadGame(romPath, RomScanner.getBiosDirectory());

        loadAutoState();
        startEmulationLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LibretroBridge.nativeSetSurface(holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopEmulationLoop();
    }

    private void startEmulationLoop() {
        isRunning = true;
        if (audioTrack != null) audioTrack.play();

        emulationThread = new Thread(() -> {
            long targetFrameNs = 16666666L; // 60 FPS
            while (isRunning) {
                long frameStart = System.nanoTime();
                if (!isPausedForMenu) {
                    int input = inputManager.getCurrentInputState();
                    LibretroBridge.nativeRunFrame(input);
                    if (fastForward) {
                        LibretroBridge.nativeRunFrame(input);
                    }
                }

                long frameDuration = System.nanoTime() - frameStart;
                long sleepNs = (fastForward ? (targetFrameNs / 2) : targetFrameNs) - frameDuration;
                if (sleepNs > 0) {
                    try {
                        Thread.sleep(sleepNs / 1000000L, (int)(sleepNs % 1000000L));
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        emulationThread.setPriority(Thread.MAX_PRIORITY);
        emulationThread.start();
    }

    private void stopEmulationLoop() {
        isRunning = false;
        if (emulationThread != null) {
            try {
                emulationThread.join(500);
            } catch (InterruptedException ignored) {}
            emulationThread = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
        }
    }

    private String getCoreLibraryForConsole(String c) {
        String nativeDir = getApplicationInfo().nativeLibraryDir;
        switch (c) {
            case RomScanner.CONSOLE_NES:
                return nativeDir + "/libretro_fceumm.so";
            case RomScanner.CONSOLE_GAMEBOY:
                return nativeDir + "/libretro_gambatte.so";
            case RomScanner.CONSOLE_GBA:
                return nativeDir + "/libretro_mgba.so";
            case RomScanner.CONSOLE_PS1:
                return nativeDir + "/libretro_pcsx_rearmed.so";
            case RomScanner.CONSOLE_PSP:
                return nativeDir + "/libretro_ppsspp.so";
            default:
                return nativeDir + "/libretro_fceumm.so";
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (overlayMenuContainer.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hideOverlayMenu();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        // Long-press Back button opens overlay
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getRepeatCount() == 0) {
                backDownTimestamp = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - backDownTimestamp > 600) {
                showOverlayMenu();
                backDownTimestamp = Long.MAX_VALUE;
                return true;
            }
            return true;
        }

        if (inputManager.handleKeyEvent(event)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (overlayMenuContainer.getVisibility() == View.VISIBLE) {
            return super.onKeyUp(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (backDownTimestamp != Long.MAX_VALUE && System.currentTimeMillis() - backDownTimestamp < 600) {
                showOverlayMenu();
            }
            return true;
        }
        if (inputManager.handleKeyEvent(event)) return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (inputManager.handleMotionEvent(event)) return true;
        return super.onGenericMotionEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAutoState();
        stopEmulationLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surfaceView.getHolder().getSurface().isValid() && !isRunning) {
            startEmulationLoop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopEmulationLoop();
        LibretroBridge.nativeDestroy();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        saveAutoState();
    }
}
