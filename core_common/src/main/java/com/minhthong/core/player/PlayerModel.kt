package com.minhthong.core.player

import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.minhthong.core.model.PlaylistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.nio.ByteBuffer
import kotlin.math.abs

@UnstableApi
open class PlayerModel {

    companion object {
        private const val WINDOW_MS = 16
        private const val MAX_SHORT = Short.MAX_VALUE.toFloat()
    }

    protected lateinit var exoPlayer: ExoPlayer

    protected lateinit var subExoPlayer: ExoPlayer

    protected lateinit var waveformProcessor: WaveformAudioProcessor

    protected lateinit var subWaveformProcessor: WaveformAudioProcessor

    protected lateinit var playerScope: CoroutineScope

    protected var updatePlayingPositionJob: Job? = null

    protected var updateBufferPositionJob: Job? = null

    protected var mediaTransitionJob: Job? = null

    protected var currentPlaylistItems = emptyList<PlaylistItemEntity>()

    protected var currentItemIndex = -1

    protected var isLooping = false

    protected var shouldResumeOnFocusGain = false

    protected var isDucked = false

    protected class WaveformAudioProcessor(
        private val onSamplesReady: (FloatArray) -> Unit
    ) : BaseAudioProcessor() {

        private var chunkSize = 0
        private var pendingSamples = FloatArray(0)
        private var sampleIndex = 0

        override fun onConfigure(
            inputAudioFormat: AudioProcessor.AudioFormat
        ): AudioProcessor.AudioFormat {
            val sampleRate = inputAudioFormat.sampleRate

            chunkSize = (sampleRate * WINDOW_MS) / 1000
            pendingSamples = FloatArray(chunkSize)

            return inputAudioFormat
        }

        override fun queueInput(inputBuffer: ByteBuffer) {
            val remaining = inputBuffer.remaining()
            val outputBuffer = replaceOutputBuffer(remaining)

            while (inputBuffer.hasRemaining()) {
                val sample = inputBuffer.getShort()
                val normalized = (sample.toFloat() / MAX_SHORT).coerceIn(-1f, 1f)

                pendingSamples[sampleIndex] = abs(normalized)
                sampleIndex++

                if (sampleIndex >= chunkSize) {
                    onSamplesReady(pendingSamples.copyOf())
                    sampleIndex = 0
                }

                outputBuffer.putShort(sample)
            }

            outputBuffer.flip()
        }

        override fun onReset() {
            pendingSamples = FloatArray(0)
            sampleIndex = 0
        }
    }
}