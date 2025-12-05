//package com.minhthong.player.presentation.service
//
//import android.content.Context
//import android.os.Bundle
//import androidx.media3.session.CommandButton
//import androidx.media3.session.MediaNotification
//import androidx.media3.session.MediaSession
//import com.google.common.collect.ImmutableList
//import androidx.media3.session.R
//
//class MediaNotificationProvider(
//    private val context: Context,
//    private val mediaSession: MediaSession
//) : MediaNotification.Provider {
//
//    private val smallIcon = R.drawable.media_session_service_notification_ic_music_note
//
//    override fun createNotification(
//        mediaSession: MediaSession,
//        mediaButtonPreferences: ImmutableList<CommandButton>,
//        actionFactory: MediaNotification.ActionFactory,
//        onNotificationChangedCallback: MediaNotification.Provider.Callback
//    ): MediaNotification {
//
//    }
//
//    override fun handleCustomCommand(
//        session: MediaSession,
//        action: String,
//        extras: Bundle
//    ): Boolean {
//
//    }
//
//
//    companion object {
//        const val NOTIFICATION_ID = 123
//        const val CHANNEL_ID = "media_playback"
//    }
//}
