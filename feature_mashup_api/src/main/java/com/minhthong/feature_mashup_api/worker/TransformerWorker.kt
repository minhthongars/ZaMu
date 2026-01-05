package com.minhthong.feature_mashup_api.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.minhthong.core.R
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.transformer.TransformerWrapper
import com.minhthong.feature_mashup_api.repository.MashupRepository
import java.util.UUID
import com.minhthong.core.common.Result as AppResult

class TransformerWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
    private val transformer: TransformerWrapper,
    private val mashupRepository: MashupRepository,
    private val playerManager: PlayerManager
): CoroutineWorker(appContext, workerParams) {

    companion object {

        private const val CHANNEL_ID = "AudioTransformService"
        private const val CHANNEL_NAME = "Audio Transform"

        const val KEY_CUT_FROM_MLS = "cut_from"
        const val KEY_CUT_TO_MLS = "cut_to"

        fun createCutAudioWorker(
            context: Context,
            startMls: Long,
            endMls: Long
        ) {
            val workId = UUID.randomUUID()
            val uploadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<TransformerWorker>()
                    .setInputData(
                        workDataOf(
                            KEY_CUT_TO_MLS to endMls,
                            KEY_CUT_FROM_MLS to startMls,
                        )
                    )
                    .setId(workId)
                    .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(uploadWorkRequest)
        }
    }

    private var notificationId: Int = 0

    override suspend fun doWork(): Result {
        generateNotificationId()
        createNotificationChannel()
        setForeground(createForegroundInfo())

        return cutAudio()
    }

    private fun generateNotificationId() {
        notificationId = (System.currentTimeMillis() / 1000).toInt()
    }

    private suspend fun cutAudio(): Result {
        val controllerInfo = playerManager.controllerInfoFlow.value ?: return Result.failure()
        val trackEntity = controllerInfo.playingItem

        val startMls = inputData.getLong(KEY_CUT_FROM_MLS, 0)
        val endMls = inputData.getLong(KEY_CUT_TO_MLS, 0)

        val filePath = transformer.cutAudio(
            startMls = startMls,
            endMls = endMls,
            uri = trackEntity.uri,
            onProgressChange = { currentProgress ->
                updateNotification(currentProgress)
            }
        )

        val result = mashupRepository.insertCut(
            uriString = filePath.toUri().toString(),
            duration = controllerInfo.duration,
            avatarBitmap = trackEntity.avatarImage,
            name = trackEntity.title,
            performer = trackEntity.artist,
            startPosition = startMls,
            endPosition = endMls
        )

        return when(result) {
            is AppResult.Success -> Result.success()

            is AppResult.Error -> Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }

        return ForegroundInfo(
            notificationId,
            createNotification(0),
            foregroundServiceType
        )
    }

    private fun createNotification(progress: Int): Notification {
        val notificationBuilder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sample)
            .setContentTitle("Audio processing")
            .setContentText("Please wait...")
            .setOnlyAlertOnce(false)
            .setSilent(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setProgress(100, progress, false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE
            )
        }

        return notificationBuilder.build()
    }

    private fun updateNotification(progress: Int) {
        val manager = appContext.getSystemService(NotificationManager::class.java)
        val notification = createNotification(progress = progress)

        manager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = appContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}