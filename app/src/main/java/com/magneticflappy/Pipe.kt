package com.magneticflappy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Pipe(private var screenWidth: Int, private var screenHeight: Int) {
    var x: Float = screenWidth.toFloat()
    private val width: Float = 120f
    private val gap: Float = 400f
    private val speed: Float = 6f
    
    // Random gap position
    var gapY: Float = (screenHeight * 0.3f + Math.random() * screenHeight * 0.4f).toFloat()
    
    var scored = false
    
    private val paint = Paint().apply {
        color = 0xFF4CAF50.toInt() // Green
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val borderPaint = Paint().apply {
        color = 0xFF2E7D32.toInt() // Dark green
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    fun update() {
        x -= speed
    }
    
    fun draw(canvas: Canvas) {
        val capHeight = 40f
        val capOverhang = 10f
        
        // Pipe color highlight (simple 3D effect)
        val highlightWidth = width * 0.15f
        val highlightX = x + width * 0.1f
        val highlightPaint = Paint(paint).apply {
            color = 0xFF81C784.toInt() // Lighter green for highlight
        }
        
        // --- Top Pipe ---
        val topPipeHeight = gapY - gap / 2
        // Main body
        val topPipe = RectF(x, 0f, x + width, topPipeHeight - capHeight)
        canvas.drawRect(topPipe, paint)
        // Highlight
        canvas.drawRect(highlightX, 0f, highlightX + highlightWidth, topPipeHeight - capHeight, highlightPaint)
        // Border
        canvas.drawRect(topPipe, borderPaint)
        
        // Cap (at the bottom of the top pipe)
        val topCap = RectF(x - capOverhang, topPipeHeight - capHeight, x + width + capOverhang, topPipeHeight)
        canvas.drawRect(topCap, paint)
        canvas.drawRect(highlightX - capOverhang, topPipeHeight - capHeight, highlightX + highlightWidth, topPipeHeight, highlightPaint)
        canvas.drawRect(topCap, borderPaint)
        
        // --- Bottom Pipe ---
        val bottomPipeY = gapY + gap / 2
        // Main body
        val bottomPipe = RectF(x, bottomPipeY + capHeight, x + width, screenHeight.toFloat())
        canvas.drawRect(bottomPipe, paint)
        // Highlight
        canvas.drawRect(highlightX, bottomPipeY + capHeight, highlightX + highlightWidth, screenHeight.toFloat(), highlightPaint)
        // Border
        canvas.drawRect(bottomPipe, borderPaint)
        
        // Cap (at the top of the bottom pipe)
        val bottomCap = RectF(x - capOverhang, bottomPipeY, x + width + capOverhang, bottomPipeY + capHeight)
        canvas.drawRect(bottomCap, paint)
        canvas.drawRect(highlightX - capOverhang, bottomPipeY, highlightX + highlightWidth, bottomPipeY + capHeight, highlightPaint)
        canvas.drawRect(bottomCap, borderPaint)
    }
    
    fun isOffScreen(): Boolean {
        return x + width < 0
    }
    
    fun getTopPipeBounds(): RectF {
        return RectF(x, 0f, x + width, gapY - gap / 2)
    }
    
    fun getBottomPipeBounds(): RectF {
        return RectF(x, gapY + gap / 2, x + width, screenHeight.toFloat())
    }
    
    fun reset() {
        x = screenWidth.toFloat()
        gapY = (screenHeight * 0.3f + Math.random() * screenHeight * 0.4f).toFloat()
        scored = false
    }
}
