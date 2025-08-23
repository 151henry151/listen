package com.romp.listen.app.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Custom view for displaying real-time audio levels
 */
class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private var currentLevel = 0f
    private var targetLevel = 0f
    private val animationSpeed = 0.1f
    
    // Bar configuration
    private val barCount = 20
    private val barSpacing = 4f
    private val barWidth = 8f
    private val maxBarHeight = 100f
    
    // Colors
    private val lowLevelColor = Color.parseColor("#4CAF50") // Green
    private val mediumLevelColor = Color.parseColor("#FF9800") // Orange
    private val highLevelColor = Color.parseColor("#F44336") // Red
    
    companion object {
        private const val TAG = "AudioVisualizerView"
    }
    
    /** Update the audio level (0.0 to 1.0) */
    fun updateLevel(level: Float) {
        targetLevel = level.coerceIn(0f, 1f)
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Animate current level towards target
        currentLevel += (targetLevel - currentLevel) * animationSpeed
        
        val centerX = width / 2f
        val centerY = height / 2f
        val totalWidth = barCount * (barWidth + barSpacing) - barSpacing
        val startX = centerX - totalWidth / 2
        
        // Draw bars
        for (i in 0 until barCount) {
            val barX = startX + i * (barWidth + barSpacing)
            
            // Calculate bar height based on current level and position
            val positionFactor = 1f - abs(i - barCount / 2f) / (barCount / 2f)
            val barHeight = currentLevel * maxBarHeight * positionFactor
            
            // Determine color based on level
            val color = when {
                currentLevel < 0.3f -> lowLevelColor
                currentLevel < 0.7f -> mediumLevelColor
                else -> highLevelColor
            }
            
            paint.color = color
            
            // Draw bar
            val barTop = centerY + barHeight / 2
            val barBottom = centerY - barHeight / 2
            canvas.drawRect(barX, barTop, barX + barWidth, barBottom, paint)
        }
        
        // Continue animation
        if (abs(targetLevel - currentLevel) > 0.01f) {
            postInvalidateOnAnimation()
        }
    }
    
    /** Get color for a specific level */
    private fun getColorForLevel(level: Float): Int {
        return when {
            level < 0.3f -> lowLevelColor
            level < 0.7f -> mediumLevelColor
            else -> highLevelColor
        }
    }
    
    /** Reset the visualizer */
    fun reset() {
        currentLevel = 0f
        targetLevel = 0f
        invalidate()
    }
} 