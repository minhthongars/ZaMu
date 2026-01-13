package com.minhthong.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.collections.get
import kotlin.math.ceil
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlin.collections.get
import kotlin.math.min

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
        bitmapList: List<Bitmap?>,
        key: String,
        compressForSmallDisplay: Boolean = false
    ): Bitmap? {
        val bitmaps = bitmapList.filterNotNull()
        if (bitmaps.isEmpty()) return null

        val finalKey = key + compressForSmallDisplay.toString()
        val cache = bitmapCache[finalKey]
        if (cache != null) {
            return cache
        }

        val columns = when (bitmaps.size) {
            1 -> 1
            2, 4 -> 2
            3, 5, 6 -> 3
            else -> 3
        }

        val rows = ceil(bitmaps.size / columns.toFloat()).toInt()

        val cellWidth = if (compressForSmallDisplay) {
            min(bitmaps.minOf { it.width }, 120)
        } else {
            bitmaps.minOf { it.width }
        }

        val cellHeight = if (compressForSmallDisplay) {
            min(bitmaps.minOf { it.height }, 120)
        } else {
            bitmaps.minOf { it.height }
        }

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
        bitmapCache[finalKey] = result

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

    fun ImageView.setBitmapImages(
        bitmaps: List<Bitmap?>,
        key: String,
        compressForSmallDisplay: Boolean = false
    ) {
        if (bitmaps.size == 1) {
            setImageBitmap(bitmaps.first())
        } else if (bitmaps.size > 1) {
            val bitmap = mergeBitmapsGrid(
                bitmapList = bitmaps,
                key = key,
                compressForSmallDisplay = compressForSmallDisplay
            )
            setImageBitmap(bitmap)
        }
    }

    fun clearCache() {
        bitmapCache.clear()
    }
}