package com.example.financetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class ChartEntry(val label: String, val value: Float, val color: Int)

class CustomPieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    private val oval = RectF()
    private var entries: List<ChartEntry> = emptyList()

    fun setData(entries: List<ChartEntry>) {
        this.entries = entries
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = entries.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f * 0.8f

        oval.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = 0f
        entries.forEach { entry ->
            val sweepAngle = (entry.value / total) * 360f
            paint.color = entry.color
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint)

            // Draw label
            val angle = startAngle + sweepAngle / 2
            val rad = Math.toRadians(angle.toDouble())
            val labelX = (centerX + radius * 0.7f * Math.cos(rad)).toFloat()
            val labelY = (centerY + radius * 0.7f * Math.sin(rad)).toFloat() + textPaint.textSize / 3
            canvas.drawText(entry.label, labelX, labelY, textPaint)

            startAngle += sweepAngle
        }

        // Draw center hole
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * 0.4f, paint)
    }
}