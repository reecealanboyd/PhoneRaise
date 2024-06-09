package com.example.helloandroid

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.helloandroid.ui.theme.HelloandroidTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    // State variables to hold the gyroscope values
    private var xRotation by mutableStateOf(0f)
    private var yRotation by mutableStateOf(0f)
    private var zRotation by mutableStateOf(0f)
    private var isPhoneRaised by mutableStateOf(false)

    private var lastUpdateTime = 0L
    private var lastMovementTime = 0L
    private val stillnessThreshold = 2000L // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the SensorManager and Gyroscope sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Check if the device has the necessary sensors
        if (gyroscope == null) {
            Log.e("PhoneRaiseLogger", "This device does not have a gyroscope sensor.")
        }

        setContent {
            HelloandroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Andy, Jiang, Matt & Sagar",
                        xRotation = xRotation,
                        yRotation = yRotation,
                        zRotation = zRotation,
                        isPhoneRaised = isPhoneRaised,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the listener for the gyroscope sensor
        gyroscope?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val currentTime = System.currentTimeMillis()
            if (lastUpdateTime != 0L) {
                val deltaTime = (currentTime - lastUpdateTime) / 1000.0f // Convert milliseconds to seconds

                // Check if there is any significant rotation
                val hasSignificantRotation = event.values.any { Math.abs(it) > 0.01 }

                if (hasSignificantRotation) {
                    lastMovementTime = currentTime
                    xRotation += event.values[0] * deltaTime
                    yRotation += event.values[1] * deltaTime
                    zRotation += event.values[2] * deltaTime
                } else if (currentTime - lastMovementTime > stillnessThreshold) {
                    // If still for 2 seconds, reset rotations
                    xRotation = 0f
                    yRotation = 0f
                    zRotation = 0f
                }

                // Update isPhoneRaised based on xRotation
                isPhoneRaised = xRotation >= 1.0f

                // Log the gyroscope data
                Log.d("PhoneRaiseLogger", "X Rotation: $xRotation, Y Rotation: $yRotation, Z Rotation: $zRotation, Phone Raised: $isPhoneRaised")
            }
            lastUpdateTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something if the sensor accuracy changes
    }
}

@Composable
fun Greeting(name: String, xRotation: Float, yRotation: Float, zRotation: Float, isPhoneRaised: Boolean, modifier: Modifier = Modifier) {
    val roundedXRotation = String.format("%.2f", xRotation)
    val roundedYRotation = String.format("%.2f", yRotation)
    val roundedZRotation = String.format("%.2f", zRotation)
    Text(
        text = "Hello $name!\nPhone Raised: $isPhoneRaised\nX Rotation: $roundedXRotation\nY Rotation: $roundedYRotation\nZ Rotation: $roundedZRotation",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloandroidTheme {
        Greeting("Android", 0f, 0f, 0f, false)
    }
}
