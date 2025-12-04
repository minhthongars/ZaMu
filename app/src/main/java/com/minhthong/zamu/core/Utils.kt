package com.minhthong.zamu.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

object Utils {
    fun getAlbumArt(
        context: Context,
        mp3Uri: Uri,
        maxSize: Int = 512
    ): Bitmap? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(context, mp3Uri)
            val embeddedPicture = mmr.embeddedPicture ?: return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size, options)

            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)

            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size, options)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                mmr.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

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
}