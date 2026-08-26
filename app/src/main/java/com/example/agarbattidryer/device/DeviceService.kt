package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction layer for communicating with the Agarbatti Dryer hardware.
 * The UI never talks to BLE or network sockets directly; it only interacts
 * with this interface via the Repository.
 */
interface DeviceService {
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
