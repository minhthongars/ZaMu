package com.minhthong.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.minhthong.core.service.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale


object Utils {
    fun Long.toDurationString(): String {
        val totalSeconds = this / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60

        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    fun Float.toDurationString(): String {
        val mls = this.toLong()
        return mls.toDurationString()
    }

    fun Long.toMbString(): String {
        val mb = this / (1024.0 * 1024.0)
        return String.format(Locale.getDefault(), "%.2f MB", mb)
    }

    fun Fragment.collectFlowSafely(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        collect: suspend CoroutineScope.() -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state) {
                collect()
            }
        }
    }

    fun ImageView.loadImage(url: String) {
        Glide.with(context)
            .load(url)
            .into(this)
    }

    fun Bitmap?.toByteArray(): ByteArray? {
        if (this == null) return null
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun ByteArray?.toBitmap(): Bitmap? {
        if (this == null) return null
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    fun startPlaybackService(context: Context?) {
        val intent = Intent(context, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intent)
        } else {
            context?.startService(intent)
        }
    }

    fun stopPlaybackService(context: Context?) {
        val intent = Intent(context, MusicService::class.java)
        context?.stopService(intent)
    }
}