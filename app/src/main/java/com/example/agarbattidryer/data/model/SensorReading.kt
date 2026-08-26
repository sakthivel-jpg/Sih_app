package com.example.agarbattidryer.data.model

/**
 * Real-time environmental readings from the chamber sensors.
 */
data class SensorReading(
    val temperatureCelsius: Float,
    val humidityPercentage: Float,
    val timestamp: Long = System.currentTimeMillis()
)
