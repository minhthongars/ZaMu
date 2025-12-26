package com.minhthong.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.collections.get
import kotlin.math.ceil
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

object BitmapUtils {

    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val mmr = MediaMetadataRetriever()

    suspend fun getAlbumArtFromRemote(url: String?): Bitmap? {
        val cache = bitmapCache[url]
        if (cache != null) {
            return cache
        }

        return withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url)
                    .openConnection()
                    .getInputStream())

                bitmapCache[url!!] = bitmap

                bitmap
            } catch (_: Exception) {
                null
            }
        }
    }

    fun getAlbumArt(
        context: Context,
        mp3Uri: Uri?,
    ): Bitmap? {
        val key = mp3Uri.toString()
        val cache = bitmapCache[key]
        if (cache != null) {
            return cache
        }

        return try {
            mmr.setDataSource(context, mp3Uri)
            val embeddedPicture = mmr.embeddedPicture ?: return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            //options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)

            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeByteArray(
                embeddedPicture, 0, embeddedPicture.size, options
            )

            bitmapCache[key] = bitmap

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun mergeBitmapsGrid(
        bitmaps: List<Bitmap>
    ): Bitmap {
        val columns = if (bitmaps.size > 4) {
            3
        } else {
            2
        }

        require(bitmaps.isNotEmpty())
        require(columns > 0)

        val rows = ceil(bitmaps.size / columns.toFloat()).toInt()

        val cellWidth = bitmaps.minOf { it.width }
        val cellHeight = bitmaps.minOf { it.height }

        val totalWidth = columns * cellWidth
        val totalHeight = rows * cellHeight

        val result = createBitmap(totalWidth, totalHeight)

        val canvas = Canvas(result)

        bitmaps.forEachIndexed { index, bitmap ->
            val col = index % columns
            val row = index / columns

            val left = col * cellWidth
            val top = row * cellHeight

            val dstRect = Rect(
                left,
                top,
                left + cellWidth,
                top + cellHeight
            )

            val srcRect = centerCropSrcRect(
                bitmap.width,
                bitmap.height,
                cellWidth,
                cellHeight
            )

            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }

        return result
    }

    private fun centerCropSrcRect(
        srcW: Int,
        srcH: Int,
        dstW: Int,
        dstH: Int
    ): Rect {
        val srcRatio = srcW.toFloat() / srcH
        val dstRatio = dstW.toFloat() / dstH

        return if (srcRatio > dstRatio) {
            val newWidth = (srcH * dstRatio).toInt()
            val x = (srcW - newWidth) / 2
            Rect(x, 0, x + newWidth, srcH)
        } else {
            val newHeight = (srcW / dstRatio).toInt()
            val y = (srcH - newHeight) / 2
            Rect(0, y, srcW, y + newHeight)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
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