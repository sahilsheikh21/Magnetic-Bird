package com.magneticflappy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Bird(private var screenWidth: Int, private var screenHeight: Int) {
    var x: Float = screenWidth * 0.3f
    var y: Float = screenHeight * 0.5f
    var velocity: Float = 0f
    val radius: Float = 30f
    
    private val gravity = 0.6f
    private val liftPower = -1.2f // Additive upward force per frame
    private val maxVelocity = 15f
    
    private val paint = Paint().apply {
        color = 0xFFFFC107.toInt() // Yellow
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val eyePaint = Paint().apply {
        color = 0xFF000000.toInt() // Black
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Apply upward force (Jetpack style)
    fun lift() {
        velocity += liftPower
    }
    
    fun update() {
        velocity += gravity
        
        // Limit velocity
        if (velocity > maxVelocity) velocity = maxVelocity
        if (velocity < -maxVelocity) velocity = -maxVelocity
        
        y += velocity
        
        // Prevent going off screen
        if (y < radius) {
            y = radius
            // Don't kill velocity completely so we don't stick to ceiling weirdly,
            // but damping it helps.
            velocity = 0f
        }
        if (y > screenHeight - radius) {
            y = screenHeight - radius
            velocity = 0f
        }
    }
    
    fun draw(canvas: Canvas) {
        // Draw bird body
        canvas.drawCircle(x, y, radius, paint)
        
        // Draw eye
        val eyeX = x + radius * 0.3f
        val eyeY = y - radius * 0.2f
        canvas.drawCircle(eyeX, eyeY, radius * 0.2f, eyePaint)
    }
    
    fun getBounds(): RectF {
        return RectF(x - radius, y - radius, x + radius, y + radius)
    }
    
    fun reset() {
        y = screenHeight * 0.5f
        velocity = 0f
    }
}
