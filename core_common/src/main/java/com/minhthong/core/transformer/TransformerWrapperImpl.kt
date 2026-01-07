package com.minhthong.core.transformer

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.nio.ByteBuffer
import kotlin.coroutines.resumeWithException

@OptIn(UnstableApi::class)
internal class TransformerWrapperImpl(
    private val context: Context,
    mainDispatcher: CoroutineDispatcher
): TransformerWrapper {

    companion object {
        private const val GET_PROGRESS_DELAY = 250L
    }

    private val transformerScope = CoroutineScope(SupervisorJob() + mainDispatcher)

    override suspend fun cutAudio(
        startMls: Long,
        endMls: Long,
        uri: Uri,
        onProgressChange: (Int) -> Unit,
    ): String = suspendCancellableCoroutine { cont ->
        val input = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMls)
                    .setEndPositionMs(endMls)
                    .build()
            )
            .build()

        val outputFile = File(
            context.filesDir,
            "audio_cut_${System.currentTimeMillis()}.mp3",
        )

        var progressJob: Job? = null

        val transformerListener = object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult
            ) {
                progressJob?.cancel()
                cont.resume(
                    value = outputFile.absolutePath,
                    onCancellation = { _, _, _ -> }
                )
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exception: ExportException
            ) {
                progressJob?.cancel()
                cont.resumeWithException(exception)
            }
        }

        val transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()

        transformerScope.launch {
            transformer.start(input, outputFile.absolutePath)
        }

        progressJob = transformer.monitorProgress(
            onProgressChange = onProgressChange
        )
    }

    override suspend fun createMashup(
        uriList: List<Uri>,
        onProgressChange: (Int) -> Unit,
    ): String = suspendCancellableCoroutine { cont ->
        val editedItems = uriList.map { uri ->
            EditedMediaItem.Builder(
                MediaItem.fromUri(uri)
            ).build()
        }

        val sequence = EditedMediaItemSequence.Builder(setOf(TRACK_TYPE_AUDIO))
            .addItems(editedItems)
            .build()

        val composition = Composition.Builder(sequence).build()

        val outputFile = File(
            context.filesDir,
            "mashup_${System.currentTimeMillis()}.mp3",
        )

        var progressJob: Job? = null

        val transformerListener = object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult
            ) {
                progressJob?.cancel()
                cont.resume(
                    value = outputFile.absolutePath,
                    onCancellation = { _, _, _ -> }
                )
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exception: ExportException
            ) {
                progressJob?.cancel()
                cont.resumeWithException(exception)
            }
        }

        val transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()

        transformerScope.launch {
            transformer.start(composition, outputFile.absolutePath)
        }

        progressJob = transformer.monitorProgress(
            onProgressChange = onProgressChange
        )
    }

    private fun Transformer.monitorProgress(
        onProgressChange: (Int) -> Unit
    ) = transformerScope.launch {
        val progressHolder = ProgressHolder()

        while (true) {
            val state = getProgress(progressHolder)

            when (state) {
                Transformer.PROGRESS_STATE_AVAILABLE -> {
                    onProgressChange(progressHolder.progress)
                }

                Transformer.PROGRESS_STATE_UNAVAILABLE -> Unit

                Transformer.PROGRESS_STATE_NOT_STARTED -> Unit

                Transformer.PROGRESS_STATE_WAITING_FOR_AVAILABILITY -> Unit
            }

            delay(GET_PROGRESS_DELAY)
        }
    }

    override suspend fun createMashupWithCrossfade(
        uriList: List<Uri>,
        durations: List<Long>, // Duration in microseconds
        crossfadeDurationMs: Long,
        onProgressChange: (Int) -> Unit,
    ): String = suspendCancellableCoroutine { cont ->

        val editedItems = uriList.mapIndexed { index, uri ->
            val effects = buildList {
                when (index) {
                    0 -> {
                        add(createFadeInProcessor())
                        add(createCrossfadeOutProcessor(crossfadeDurationMs, durations[index]))
                    }
                    uriList.size - 1 -> {
                        add(createCrossfadeInProcessor(crossfadeDurationMs))
                        add(createFadeOutProcessor(trackDurationUs = durations[index]))
                    }
                    else -> {
                        add(createCrossfadeInProcessor(crossfadeDurationMs))
                        add(createCrossfadeOutProcessor(crossfadeDurationMs, durations[index]))
                    }
                }
            }

            EditedMediaItem.Builder(MediaItem.fromUri(uri))
                .setEffects(Effects(effects, emptyList()))
                .build()
        }

        val sequence = EditedMediaItemSequence.Builder(setOf(TRACK_TYPE_AUDIO))
            .addItems(editedItems)
            .build()

        val composition = Composition.Builder(sequence).build()

        val outputFile = File(
            context.filesDir,
            "mashup_${System.currentTimeMillis()}.mp3",
        )

        var progressJob: Job? = null
        var transformer: Transformer? = null

        cont.invokeOnCancellation {
            progressJob?.cancel()
            transformer?.cancel()
            outputFile.delete()
        }

        val transformerListener = object : Transformer.Listener {
            override fun onCompleted(
                composition: Composition,
                exportResult: ExportResult
            ) {
                progressJob?.cancel()
                if (cont.isActive) {
                    cont.resume(outputFile.absolutePath) { _, _, _ -> }
                }
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exception: ExportException
            ) {
                progressJob?.cancel()
                outputFile.delete()
                if (cont.isActive) {
                    cont.resumeWithException(exception)
                }
            }
        }

        transformer = Transformer.Builder(context)
            .addListener(transformerListener)
            .build()

        transformerScope.launch {
            transformer.start(composition, outputFile.absolutePath)
        }

        progressJob = transformer.monitorProgress(
            onProgressChange = onProgressChange
        )
    }

    private fun createCrossfadeInProcessor(crossfadeDurationMs: Long): AudioProcessor {
        return object : BaseAudioProcessor() {
            private var framesProcessed = 0L
            private var crossfadeFrames = 0L
            private var channelCount = 0

            override fun onConfigure(
                inputAudioFormat: AudioProcessor.AudioFormat
            ): AudioProcessor.AudioFormat {
                channelCount = inputAudioFormat.channelCount
                crossfadeFrames = (crossfadeDurationMs * inputAudioFormat.sampleRate / 1000L)
                return inputAudioFormat
            }

            override fun queueInput(inputBuffer: ByteBuffer) {
                val remaining = inputBuffer.remaining()
                val outputBuffer = replaceOutputBuffer(remaining)

                while (inputBuffer.hasRemaining()) {
                    val fadeFactor = if (framesProcessed < crossfadeFrames) {
                        (framesProcessed.toFloat() / crossfadeFrames).coerceIn(0f, 1f)
                    } else {
                        1f
                    }

                    repeat(channelCount) {
                        val sample = inputBuffer.getShort()
                        val adjusted = (sample * fadeFactor).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        outputBuffer.putShort(adjusted.toShort())
                    }

                    framesProcessed++
                }

                outputBuffer.flip()
            }

            override fun onReset() {
                framesProcessed = 0L
                crossfadeFrames = 0L
                channelCount = 0
            }
        }
    }

    private fun createFadeInProcessor(
        durationUs: Long = 1_000_000L
    ): AudioProcessor {
        return object : BaseAudioProcessor() {
            private var framesProcessed = 0L
            private var totalFadeFrames = 0L
            private var channelCount = 0

            override fun onConfigure(
                inputAudioFormat: AudioProcessor.AudioFormat
            ): AudioProcessor.AudioFormat {
                channelCount = inputAudioFormat.channelCount
                totalFadeFrames = (durationUs * inputAudioFormat.sampleRate / 1_000_000L)
                    .coerceAtLeast(1)
                return inputAudioFormat
            }

            override fun queueInput(inputBuffer: ByteBuffer) {
                val remaining = inputBuffer.remaining()
                val outputBuffer = replaceOutputBuffer(remaining)

                while (inputBuffer.hasRemaining()) {
                    val fadeFactor = if (framesProcessed < totalFadeFrames) {
                        (framesProcessed.toFloat() / totalFadeFrames).coerceIn(0f, 1f)
                    } else {
                        1f
                    }

                    repeat(channelCount) {
                        val sample = inputBuffer.getShort()
                        val adjusted = (sample * fadeFactor).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        outputBuffer.putShort(adjusted.toShort())
                    }

                    framesProcessed++
                }

                outputBuffer.flip()
            }

            override fun onReset() {
                framesProcessed = 0L
                totalFadeFrames = 0L
                channelCount = 0
            }
        }
    }

    private fun createCrossfadeOutProcessor(
        crossfadeDurationMs: Long,
        trackDurationUs: Long
    ): AudioProcessor {
        return object : BaseAudioProcessor() {
            private var framesProcessed = 0L
            private var crossfadeFrames = 0L
            private var fadeStartFrame = 0L
            private var channelCount = 0

            override fun onConfigure(
                inputAudioFormat: AudioProcessor.AudioFormat
            ): AudioProcessor.AudioFormat {
                channelCount = inputAudioFormat.channelCount
                crossfadeFrames = (crossfadeDurationMs * inputAudioFormat.sampleRate / 1000L)
                val trackTotalFrames = (trackDurationUs * inputAudioFormat.sampleRate / 1_000_000L)
                fadeStartFrame = (trackTotalFrames - crossfadeFrames).coerceAtLeast(0)
                return inputAudioFormat
            }

            override fun queueInput(inputBuffer: ByteBuffer) {
                val remaining = inputBuffer.remaining()
                val outputBuffer = replaceOutputBuffer(remaining)

                while (inputBuffer.hasRemaining()) {
                    val fadeFactor = if (framesProcessed >= fadeStartFrame) {
                        val framesIntoFade = framesProcessed - fadeStartFrame
                        val remainingFadeFrames = crossfadeFrames - framesIntoFade
                        (remainingFadeFrames.toFloat() / crossfadeFrames).coerceIn(0f, 1f)
                    } else {
                        1f
                    }

                    repeat(channelCount) {
                        val sample = inputBuffer.getShort()
                        val adjusted = (sample * fadeFactor).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        outputBuffer.putShort(adjusted.toShort())
                    }

                    framesProcessed++
                }

                outputBuffer.flip()
            }

            override fun onReset() {
                framesProcessed = 0L
                crossfadeFrames = 0L
                fadeStartFrame = 0L
                channelCount = 0
            }
        }
    }

    private fun createFadeOutProcessor(
        durationUs: Long = 1_000_000L,
        trackDurationUs: Long
    ): AudioProcessor {
        return object : BaseAudioProcessor() {
            private var framesProcessed = 0L
            private var totalFadeFrames = 0L
            private var fadeStartFrame = 0L
            private var channelCount = 0

            override fun onConfigure(
                inputAudioFormat: AudioProcessor.AudioFormat
            ): AudioProcessor.AudioFormat {
                channelCount = inputAudioFormat.channelCount
                totalFadeFrames = (durationUs * inputAudioFormat.sampleRate / 1_000_000L)
                    .coerceAtLeast(1)
                val trackTotalFrames = (trackDurationUs * inputAudioFormat.sampleRate / 1_000_000L)
                fadeStartFrame = (trackTotalFrames - totalFadeFrames).coerceAtLeast(0)
                return inputAudioFormat
            }

            override fun queueInput(inputBuffer: ByteBuffer) {
                val remaining = inputBuffer.remaining()
                val outputBuffer = replaceOutputBuffer(remaining)

                while (inputBuffer.hasRemaining()) {
                    val fadeFactor = if (framesProcessed >= fadeStartFrame) {
                        val framesIntoFade = framesProcessed - fadeStartFrame
                        val remainingFadeFrames = totalFadeFrames - framesIntoFade
                        (remainingFadeFrames.toFloat() / totalFadeFrames).coerceIn(0f, 1f)
                    } else {
                        1f
                    }

                    repeat(channelCount) {
                        val sample = inputBuffer.getShort()
                        val adjusted = (sample * fadeFactor).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        outputBuffer.putShort(adjusted.toShort())
                    }

                    framesProcessed++
                }

                outputBuffer.flip()
            }

            override fun onReset() {
                framesProcessed = 0L
                totalFadeFrames = 0L
                fadeStartFrame = 0L
                channelCount = 0
            }
        }
    }
}