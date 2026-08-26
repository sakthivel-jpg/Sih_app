package com.example.agarbattidryer.data.repository

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository isolating the ViewModels from device communication details.
 */
interface DeviceRepository {
    val connectionState: StateFlow<ConnectionState>
    val deviceStatus: StateFlow<DeviceStatus>
    val temperature: StateFlow<Float>
    val humidity: StateFlow<Float>
    val dryingDurationSeconds: StateFlow<Long>
    val activeDevice: StateFlow<DeviceInfo?>

    suspend fun connect(deviceId: String): Boolean
    suspend fun disconnect()
    suspend fun startDrying(): Boolean
    suspend fun stopDrying(): Boolean
    suspend fun getAvailableDevices(): List<DeviceInfo>
}
