package com.minhthong.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.collections.get

object BitmapUtils {

    private val bitmapCache = mutableMapOf<String, Bitmap>()

    suspend fun getAlbumArtFromRemote(url: String?): Bitmap? {
        val cache = bitmapCache[url]
        if (cache != null) {
            return cache
        }

        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())

                bitmapCache[url!!] = bitmap

                bitmap
            } catch (_: Exception) {
                null
            }
        }
    }

    fun getAlbumArt(
        context: Context,
        mp3Uri: Uri,
        maxSize: Int = 512
    ): Bitmap? {
        val key = mp3Uri.toString() + maxSize
        val cache = bitmapCache[key]
        if (cache != null) {
            return cache
        }

        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(context, mp3Uri)
            val embeddedPicture = mmr.embeddedPicture ?: return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)

            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size, options)

            bitmapCache[key] = bitmap

            bitmap
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

    fun clearCache() {
        bitmapCache.clear()
    }
}