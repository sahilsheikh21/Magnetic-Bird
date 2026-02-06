package com.magneticflappy

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import kotlin.math.sqrt

class MainActivity : Activity(), SensorEventListener {
    
    private lateinit var gameView: GameView
    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        gameView = findViewById(R.id.gameView)
        setupSettings()
        
        // Initialize sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        
        if (magneticSensor == null) {
            Toast.makeText(
                this,
                "No magnetic sensor found on this device!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        magneticSensor?.also { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // Calculate magnetic field strength
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val magneticStrength = sqrt(x * x + y * y + z * z)
            
            // Pass to game view
            gameView.onMagneticFieldChanged(magneticStrength)
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this app
    }
    
    private fun setupSettings() {
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val settingsOverlay = findViewById<View>(R.id.settingsOverlay)
        val btnCloseSettings = findViewById<Button>(R.id.btnCloseSettings)
        val seekBarSensitivity = findViewById<SeekBar>(R.id.seekBarSensitivity)
        val textSensitivity = findViewById<TextView>(R.id.textSensitivity)
        
        // Load saved sensitivity
        val prefs = getSharedPreferences("MagneticBirdPrefs", Context.MODE_PRIVATE)
        val savedSensitivity = prefs.getInt("sensitivity_percent", 30) // Default 30%
        
        // Apply initial sensitivity
        updateGameSensitivity(savedSensitivity)
        seekBarSensitivity.progress = savedSensitivity
        textSensitivity.text = "$savedSensitivity%"
        
        btnSettings.setOnClickListener {
            settingsOverlay.visibility = View.VISIBLE
        }
        
        btnCloseSettings.setOnClickListener {
            settingsOverlay.visibility = View.GONE
        }
        
        seekBarSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSensitivity.text = "$progress%"
                updateGameSensitivity(progress)
                
                // Save preference
                prefs.edit().putInt("sensitivity_percent", progress).apply()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun updateGameSensitivity(percent: Int) {
        // Map 0-100% to Threshold Range
        // 0% (Low Sens) = requires magnet CLOSE = High Threshold (~200uT)
        // 100% (High Sens) = triggers FAR = Low Threshold (~30uT)
        
        // Linear interpolation:
        // Threshold = MaxThreshold - (percent/100 * (MaxThreshold - MinThreshold))
        val maxThreshold = 200f
        val minThreshold = 30f
        
        val newThreshold = maxThreshold - (percent / 100f * (maxThreshold - minThreshold))
        gameView.threshold = newThreshold
    }
}
