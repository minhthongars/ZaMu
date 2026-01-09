package com.minhthong.player.presentation.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val waveformPaint = Paint().apply {
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private var samples: FloatArray = FloatArray(0)

    fun setSamples(newSamples: FloatArray) {
        samples = newSamples
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (samples.isEmpty()) {
            return
        }

        val w = width.toFloat()
        val h = height.toFloat()

        val cx = w / 2f
        val cy = h / 2f

        val baseRadius = minOf(w, h) / 2f

        val angleStep = 360f / samples.size

        samples.forEachIndexed { index, amplitude ->
            val amp = amplitude.coerceIn(0f, 1f)
            val angle = Math.toRadians(angleStep * index.toDouble())

            val endR = baseRadius + amp * 100

            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()

            val startX = cx + baseRadius * cosA
            val startY = cy + baseRadius * sinA

            val endX = cx + endR * cosA
            val endY = cy + endR * sinA

            waveformPaint.color = getRandomColorInt()
            canvas.drawLine(startX, startY, endX, endY, waveformPaint)
        }
    }

    private fun getRandomColorInt(): Int {
        return Color.argb(
            250,
            Random.nextInt(256),
            100,
            Random.nextInt(256)
        )
    }

}