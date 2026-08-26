package com.example.agarbattidryer.ui.home

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceStatus

data class HomeUiState(
    val deviceStatus: DeviceStatus = DeviceStatus.READY,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val temperature: Float? = null,
    val humidity: Float? = null,
    val dryingDurationSeconds: Long = 0L,
    val activeDeviceName: String? = null,
    val isDrying: Boolean = false,
    val canStart: Boolean = true,
    val canStop: Boolean = false,
    val userMessage: String? = null
)
