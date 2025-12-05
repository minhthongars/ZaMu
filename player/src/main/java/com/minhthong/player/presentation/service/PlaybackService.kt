package com.minhthong.player.presentation.service

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.minhthong.core.player.PlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService: MediaSessionService() {

    companion object {
        private const val ACTION_FAVORITES = "favorites_action"
    }

    @Inject
    internal lateinit var playerManager: PlayerManager

    private val customCommandFavorites = SessionCommand(ACTION_FAVORITES, Bundle.EMPTY)

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = playerManager.getPlayer()

//        val favoriteButton =
//            CommandButton.Builder(CommandButton.ICON_HEART_UNFILLED)
//                .setDisplayName("Save to favorites")
//                .setSessionCommand(customCommandFavorites)
//                .build()

        mediaSession =
            MediaSession.Builder(this, player)
                //.setCallback(MediaSessionCallback())
                //.setMediaButtonPreferences(ImmutableList.of(favoriteButton))
                .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            playerManager.release()
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(customCommandFavorites)
                        .build()
                )
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_FAVORITES) {
                // Do custom logic here
                //saveToFavorites(session.player.currentMediaItem)
                Toast.makeText(baseContext, "Không có gì", Toast.LENGTH_SHORT).show()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }
}