package com.minhthong.zamu.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.transformer.TransformerWrapper
import com.minhthong.feature_mashup_api.repository.MashupRepository
import com.minhthong.feature_mashup_api.worker.TransformerWorker

class TransformerWorkerFactory(
    private val transformer: TransformerWrapper,
    private val mashupRepository: MashupRepository,
    private val playerManager: PlayerManager
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            TransformerWorker::class.java.name -> {
                TransformerWorker(
                    appContext,
                    workerParameters,
                    transformer,
                    mashupRepository,
                    playerManager
                )
            }

            else -> {
                null
            }
        }
    }
}