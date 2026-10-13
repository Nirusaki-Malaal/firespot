package com.fireemu.game

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.fireemu.nativebridge.LibretroBridge

class AudioEngine(private val bufferSizeSamples: Int = 1600) {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 48000

    fun init() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufSize = Math.max(minBuf, bufferSizeSamples * 2 * 2)

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize,
            AudioTrack.MODE_STREAM
        )

        LibretroBridge.setAudioCallback { samples, numSamples ->
            val track = audioTrack
            if (track != null && track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.write(samples, 0, numSamples)
            }
        }
    }

    fun start() {
        audioTrack?.play()
    }

    fun pause() {
        audioTrack?.pause()
        audioTrack?.flush()
    }

    fun release() {
        LibretroBridge.setAudioCallback(null)
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (ignored: Exception) {}
        audioTrack = null
    }
}
