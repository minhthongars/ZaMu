package com.minhthong.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.minhthong.core.service.MusicService

class NotificationDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, MusicService::class.java)
        context.stopService(serviceIntent)
    }
}