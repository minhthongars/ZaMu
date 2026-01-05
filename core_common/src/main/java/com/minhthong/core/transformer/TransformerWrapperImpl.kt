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
import kotlinx.coroutines.Dispatchers
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
    private val mainDispatcher: CoroutineDispatcher
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
}