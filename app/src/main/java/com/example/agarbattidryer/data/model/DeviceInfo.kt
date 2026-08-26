package com.example.agarbattidryer.data.model

/**
 * Information regarding a connected Agarbatti Dryer machine.
 */
data class DeviceInfo(
    val id: String,
    val name: String,
    val model: String = "Solar Dryer v1",
    val signalStrengthDbm: Int? = -55,
    val isDefault: Boolean = false
)
