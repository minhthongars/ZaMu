package com.minhthong.zamu

import android.app.Application
import com.minhthong.zamu.core.player.PlayerManager
import com.minhthong.zamu.core.player.PlayerManagerImpl
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZamuApplication: Application() {

    @Inject
    internal lateinit var playerManager: PlayerManager

    override fun onCreate() {
        super.onCreate()

        playerManager.initialize(context = this)
    }
}