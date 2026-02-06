package com.magneticflappy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var bird: Bird
    private val pipes = mutableListOf<Pipe>()
    private var score = 0
    private var highScore = 0
    private var gameState = GameState.READY
    private var frameCount = 0
    
    // Background items
    private data class Cloud(var x: Float, val y: Float, val radius: Float, val speed: Float)
    private val clouds = mutableListOf<Cloud>()
    private val cloudPaint = Paint().apply {
        color = 0x80FFFFFF.toInt() // Semi-transparent white
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val pipeSpawnInterval = 90 // frames between pipes
    
    // Magnetic sensor detection
    var magneticFieldDetected = false
    private var lastJumpTime = 0L
    private val jumpCooldown = 300L // milliseconds
    
    // Magnetic threshold (150f default = "low sensitivity" / "closer magnet")
    // Can be adjusted from Settings
    var threshold = 150f
    
    private val skyPaint = Paint().apply {
        color = 0xFF87CEEB.toInt()
        style = Paint.Style.FILL
    }
    
    private val groundPaint = Paint().apply {
        color = 0xFF8BC34A.toInt()
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    private val scorePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, 0xFF000000.toInt())
    }
    
    private val gameOverPaint = Paint().apply {
        color = 0x80000000.toInt()
        style = Paint.Style.FILL
    }
    
    enum class GameState {
        READY, PLAYING, GAME_OVER
    }
    
    init {
        // Load high score
        val prefs = context.getSharedPreferences("MagneticBirdPrefs", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bird = Bird(w, h)
        
        // Generate clouds
        clouds.clear()
        for (i in 0..5) {
            clouds.add(
                Cloud(
                    x = (Math.random() * w).toFloat(),
                    y = (Math.random() * h * 0.5f).toFloat(), // Top half of screen
                    radius = (40 + Math.random() * 40).toFloat(),
                    speed = (0.5f + Math.random() * 1.0f).toFloat()
                )
            )
        }
        
        startGame()
    }
    
    private fun startGame() {
        bird.reset()
        pipes.clear()
        score = 0
        gameState = GameState.READY
        frameCount = 0
        
        // Fix: Remove any existing callbacks to prevent duplicate loops
        removeCallbacks(gameLoop)
        // Start game loop
        post(gameLoop)
    }
    
    private val gameLoop = object : Runnable {
        override fun run() {
            update()
            invalidate()
            postDelayed(this, 16) // ~60 FPS
        }
    }
    
    fun onMagneticFieldChanged(strength: Float) {
        // Prevent crash if sensor event fires before view is laid out
        if (!::bird.isInitialized) return

        // Detect strong magnetic field
        // Increased threshold to 150f (was 50f) to make it "less sensitive"
        // This requires the magnet to be much closer to the sensor
        if (!::bird.isInitialized) return
        
        if (strength > threshold) {
            when (gameState) {
                GameState.READY -> {
                    gameState = GameState.PLAYING
                    bird.lift()
                }
                GameState.PLAYING -> {
                    // Continuous lift (Jetpack style)
                    bird.lift()
                }
                GameState.GAME_OVER -> {
                    // Restart handled by touch
                }
            }
        }
    }
    
    private fun update() {
        if (gameState == GameState.PLAYING) {
            bird.update()
            
            // Update pipes
            for (pipe in pipes) {
                pipe.update()
                
                // Check collision
                if (checkCollision(pipe)) {
                    gameState = GameState.GAME_OVER
                    if (score > highScore) {
                        highScore = score
                        val prefs = context.getSharedPreferences("MagneticBirdPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putInt("high_score", highScore).apply()
                    }
                }
                
                // Check score
                if (!pipe.scored && pipe.x + 60 < bird.x) {
                    pipe.scored = true
                    score++
                }
            }
            
            // Remove off-screen pipes
            pipes.removeAll { it.isOffScreen() }
            
            // Spawn new pipes
            frameCount++
            if (frameCount % pipeSpawnInterval == 0) {
                pipes.add(Pipe(width, height))
            }
            
            // Check if bird hit ground or ceiling
            if (bird.y <= bird.radius || bird.y >= height - bird.radius) {
                gameState = GameState.GAME_OVER
                if (score > highScore) {
                    highScore = score
                    val prefs = context.getSharedPreferences("MagneticBirdPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("high_score", highScore).apply()
                }
            }
        }
        
        // Update clouds (always move)
        for (cloud in clouds) {
            cloud.x -= cloud.speed
            if (cloud.x + cloud.radius < 0) {
                cloud.x = width + cloud.radius
            }
        }
    }
    
    private fun checkCollision(pipe: Pipe): Boolean {
        val birdBounds = bird.getBounds()
        return RectF.intersects(birdBounds, pipe.getTopPipeBounds()) ||
               RectF.intersects(birdBounds, pipe.getBottomPipeBounds())
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw sky
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)
        
        // Draw clouds
        for (cloud in clouds) {
            canvas.drawCircle(cloud.x, cloud.y, cloud.radius, cloudPaint)
            // Draw a couple overlapping circles to make it puffy
            canvas.drawCircle(cloud.x - cloud.radius*0.8f, cloud.y + cloud.radius*0.2f, cloud.radius*0.7f, cloudPaint)
            canvas.drawCircle(cloud.x + cloud.radius*0.8f, cloud.y + cloud.radius*0.2f, cloud.radius*0.7f, cloudPaint)
        }
        
        // Draw ground
        val groundHeight = height * 0.1f
        canvas.drawRect(0f, height - groundHeight, width.toFloat(), height.toFloat(), groundPaint)
        
        // Draw pipes
        for (pipe in pipes) {
            pipe.draw(canvas)
        }
        
        // Draw bird
        bird.draw(canvas)
        
        // Draw score
        if (gameState == GameState.PLAYING) {
            canvas.drawText("$score", width / 2f, 120f, scorePaint)
        }
        
        // Draw ready message
        if (gameState == GameState.READY) {
            canvas.drawText("Bring a magnet near", width / 2f, height / 2f - 60f, textPaint)
            canvas.drawText("the phone to jump!", width / 2f, height / 2f + 20f, textPaint)
            
            // Draw High Score
            canvas.drawText("High Score: $highScore", width / 2f, height / 2f + 150f, scorePaint)
        }
        
        // Draw game over
        if (gameState == GameState.GAME_OVER) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gameOverPaint)
            canvas.drawText("Game Over!", width / 2f, height / 2f - 100f, scorePaint)
            canvas.drawText("Score: $score", width / 2f, height / 2f, textPaint)
            canvas.drawText("Best: $highScore", width / 2f, height / 2f + 80f, textPaint)
            canvas.drawText("Tap to Restart", width / 2f, height / 2f + 160f, textPaint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameState == GameState.GAME_OVER) {
                startGame()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
