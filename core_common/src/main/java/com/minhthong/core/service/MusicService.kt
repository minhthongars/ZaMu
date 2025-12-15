package com.minhthong.core.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.session.R
import com.minhthong.core.model.PlayerEntity
import com.minhthong.core.model.TrackEntity
import com.minhthong.core.player.PlayerManager
import com.minhthong.core.util.Utils
import com.minhthong.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "music_playback"
        const val CHANNEL_NAME = "Music Playback"
        const val NOTIFICATION_ID = 1001
    }

    @Inject
    internal lateinit var playerManager: PlayerManager

    @Inject
    internal lateinit var navigator: Navigation

    private val exoPlayer
        get() = playerManager.getPlayer()

    private lateinit var mediaSession: MediaSessionCompat
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var isForeground = false

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            playerManager.play()
        }

        override fun onPause() {
            playerManager.play()
        }

        override fun onSkipToNext() {
            playerManager.moveToNext()
        }

        override fun onSkipToPrevious() {
            playerManager.moveToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            playerManager.seek(pos)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        observePlayerState()

        initMediaSession()

        showNotification(null)
    }

    override fun onBind(intent: Intent?): IBinder = LocalBinder()

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (exoPlayer.isPlaying.not()) {
            stopSelf()
        }
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, CHANNEL_NAME).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }

    private fun buildPlaybackState(
        positionMls: Long?
    ): PlaybackStateCompat {
        val info = playerManager.playerInfoFlow.value
        val isPlaying = info?.isPlaying == true

        val state = if (isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        }
        else {
            PlaybackStateCompat.STATE_PAUSED
        }

        val currentPosition = positionMls ?: playerManager.currentProgressMlsFlow.value

        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        return PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, currentPosition, 1f)
            .build()

    }

    private fun buildNotification(
        playerInfo: PlayerEntity?
    ): Notification {
        val info = playerInfo ?: playerManager.playerInfoFlow.value
        val trackInfo = info?.trackInfo?.entity

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.media_session_service_notification_ic_music_note)
            .setContentTitle(trackInfo?.title)
            .setContentText(trackInfo?.artist)
            .setContentIntent(contentIntent())
            .setDeleteIntent(deleteIntent())
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .build()
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, navigator.getActivity()).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun deleteIntent(): PendingIntent {
        val intent = Intent(this, NotificationDismissReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updatePlaybackState(positionMls: Long?) {
        val state = buildPlaybackState(positionMls)
        mediaSession.setPlaybackState(state)
    }

    private fun updateNotification(playerEntity: PlayerEntity?) {
        val track = playerEntity?.trackInfo?.entity ?: return

        mediaSession.setMetadata(buildMetadata(track = track))

        NotificationManagerCompat.from(this)
            .notify(
                NOTIFICATION_ID,
                buildNotification(playerEntity)
            )
    }

    private fun buildMetadata(track: TrackEntity): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                Utils.getAlbumArt(baseContext, track.uri)
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                track.artist
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                track.title
            )
            .putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                track.durationMs
            )
            .build()
    }

    private fun observePlayerState() {
        serviceScope.launch {
            playerManager.playerInfoFlow.collect { playerInfo ->
                if (playerInfo == null) {
                    hideNotification()
                } else {
                    showNotification(info = playerInfo)

                    updatePlaybackState(positionMls = null)
                    updateNotification(playerEntity = playerInfo)
                }
            }
        }

        serviceScope.launch {
            playerManager.currentProgressMlsFlow.collect { mls ->
                updatePlaybackState(positionMls = mls)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun showNotification(info: PlayerEntity?) {
        if (!isForeground) {
            startForeground(NOTIFICATION_ID, buildNotification(info))
            isForeground = true
        } else {
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, buildNotification(info))
        }
    }

    private fun hideNotification() {
        if (isForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForeground = false
        }
    }

    private inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}