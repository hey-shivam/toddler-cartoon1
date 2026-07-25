package com.example.toddlercartoons

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * A simple, calm circular countdown indicator. Deliberately plain — no flashing,
 * no color changes to red/alarm tones, no sudden movement. It just quietly shrinks,
 * so the end of the session is visible and predictable well before it happens.
 */
class CountdownRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var maxValue: Int = 1
    var currentProgress: Int = 1
        private set

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7FB3D5") // calm blue, not alarming
        style = Paint.Style.STROKE
        strokeWidth = 18f
        strokeCap = Paint.Cap.ROUND
    }

    private val bounds = RectF()

    fun setMax(max: Int) {
        maxValue = if (max <= 0) 1 else max
        currentProgress = maxValue
        invalidate()
    }

    fun setProgress(value: Int) {
        currentProgress = value.coerceIn(0, maxValue)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 20f
        bounds.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(bounds, 0f, 360f, false, backgroundPaint)
        val sweep = 360f * (currentProgress.toFloat() / maxValue.toFloat())
        canvas.drawArc(bounds, -90f, sweep, false, progressPaint)
    }
}
