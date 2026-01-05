package com.minhthong.zamu.application

import android.app.Application
import androidx.work.Configuration
import com.minhthong.core.player.PlayerManager
import com.minhthong.zamu.di.TransformerWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZamuApplication: Application(), Configuration.Provider {

    @Inject
    internal lateinit var playerManager: PlayerManager

    @Inject
    internal lateinit var transformerWorkerFactory: TransformerWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(transformerWorkerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        playerManager.initialize()
    }
}