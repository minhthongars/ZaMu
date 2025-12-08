package com.minhthong.core.service

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.minhthong.core.player.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var playerManager: PlayerManager

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, playerManager.getPlayer()).build()

        addSession(mediaSession)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        with(mediaSession) {
            player.release()
            release()
        }
        playerManager.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (mediaSession.player.isPlaying.not()) {
            stopSelf()
        }
    }
}