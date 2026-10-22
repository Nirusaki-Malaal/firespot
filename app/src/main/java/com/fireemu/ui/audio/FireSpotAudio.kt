package com.fireemu.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import android.view.View
import com.fireemu.R

/** Small, local-only UI feedback. Game audio remains owned by the emulator activity. */
object FireSpotAudio {
    @Volatile private var soundsEnabled = true
    @Volatile private var clickLoaded = false
    private var clickId = 0
    private var soundPool: SoundPool? = null
    private var ambient: MediaPlayer? = null
    private var speechDucking = false

    @Synchronized
    fun setSpeechDucking(active: Boolean) {
        speechDucking = active
        val volume = if (active) 0.02f else 0.10f
        ambient?.setVolume(volume, volume)
    }

    @Synchronized
    private fun ensureClick(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attrs).build().also { pool ->
            pool.setOnLoadCompleteListener { _, sample, status ->
                if (sample == clickId && status == 0) clickLoaded = true
            }
            clickId = pool.load(context.applicationContext, R.raw.ui_click, 1)
        }
    }

    fun setSoundsEnabled(enabled: Boolean) {
        soundsEnabled = enabled
    }

    fun click(context: Context, view: View) {
        if (!soundsEnabled) return
        ensureClick(context)
        if (clickLoaded) soundPool?.play(clickId, 0.38f, 0.38f, 1, 0, 1f)
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun navigate(context: Context) {
        if (!soundsEnabled) return
        ensureClick(context)
        if (clickLoaded) soundPool?.play(clickId, 0.18f, 0.18f, 0, 0, 0.92f)
    }

    @Synchronized
    fun setAmbientEnabled(context: Context, enabled: Boolean) {
        if (!enabled) {
            ambient?.let { player ->
                if (player.isPlaying) player.pause()
                player.seekTo(0)
                player.release()
            }
            ambient = null
            return
        }
        if (ambient == null) {
            ambient = MediaPlayer.create(context.applicationContext, R.raw.scizzie_aquatic_ambience)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(if (speechDucking) 0.02f else 0.10f, if (speechDucking) 0.02f else 0.10f)
            }
        }
        ambient?.let { if (!it.isPlaying) it.start() }
    }

    @Synchronized
    fun pauseAmbient() {
        ambient?.let { if (it.isPlaying) it.pause() }
    }
}
