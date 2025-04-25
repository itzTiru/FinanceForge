package com.example.financetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

data class BarChartEntry(val label: String, val value: Float, val color: Int)

class CustomBarChart @JvmOverloads constructor(
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
    private var entries: List<BarChartEntry> = emptyList()

    fun setData(entries: List<BarChartEntry>) {
        this.entries = entries
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (entries.isEmpty()) return

        val maxValue = entries.maxOf { it.value.coerceAtLeast(0f) }
        val minValue = entries.minOf { it.value.coerceAtMost(0f) }
        val valueRange = maxValue - minValue
        if (valueRange == 0f) return

        val padding = 50f
        val barWidth = (width - padding * 2) / entries.size.toFloat() * 0.4f
        val spacing = (width - padding * 2) / entries.size.toFloat() * 0.2f
        val chartHeight = height - padding * 2

        // Draw zero line
        val zeroY = if (minValue < 0) {
            height - padding - (chartHeight * (maxValue / valueRange))
        } else {
            height - padding
        }
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        canvas.drawLine(padding, zeroY, width - padding, zeroY, paint)

        entries.forEachIndexed { index, entry ->
            val x = padding + index * (barWidth + spacing) + spacing / 2
            val barHeight = (entry.value / valueRange) * chartHeight
            val topY = if (entry.value >= 0) {
                zeroY - barHeight
            } else {
                zeroY
            }
            val bottomY = if (entry.value >= 0) {
                zeroY
            } else {
                zeroY - barHeight
            }

            paint.color = entry.color
            canvas.drawRect(x, topY, x + barWidth, bottomY, paint)

            // Draw label
            val labelX = x + barWidth / 2
            val labelY = height - padding / 2
            canvas.save()
            canvas.rotate(-45f, labelX, labelY)
            canvas.drawText(entry.label, labelX, labelY, textPaint)
            canvas.restore()
        }
    }
}