package com.minhthong.core.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import javax.inject.Singleton

@Singleton
class AudioFocusManager(
    private val context: Context
) {

    interface Callback {
        fun onFocusGained()
        fun onFocusLost()
        fun onFocusLostTransient()
        fun onDuck()
    }

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var focusRequest: AudioFocusRequest? = null

    fun requestAudioFocus(callback: Callback): Boolean {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val listener = AudioManager.OnAudioFocusChangeListener { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> callback.onFocusGained()
                AudioManager.AUDIOFOCUS_LOSS -> callback.onFocusLost()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> callback.onFocusLostTransient()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> callback.onDuck()
            }
        }

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener(listener)
            .setWillPauseWhenDucked(true)
            .build()

        focusRequest = request

        val result = audioManager.requestAudioFocus(request)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}






