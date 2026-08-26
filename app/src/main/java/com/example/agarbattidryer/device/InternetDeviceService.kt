package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cloud / REST / MQTT implementation for remote Wi-Fi monitoring of ESP32.
 * Will be populated in Phase 6 without altering UI code.
 */
class InternetDeviceService : DeviceService {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceStatus = MutableStateFlow(DeviceStatus.DISCONNECTED)
    override val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _temperature = MutableStateFlow(0.0f)
    override val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow(0.0f)
    override val humidity: StateFlow<Float> = _humidity.asStateFlow()

    private val _dryingDurationSeconds = MutableStateFlow(0L)
    override val dryingDurationSeconds: StateFlow<Long> = _dryingDurationSeconds.asStateFlow()

    private val _activeDevice = MutableStateFlow<DeviceInfo?>(null)
    override val activeDevice: StateFlow<DeviceInfo?> = _activeDevice.asStateFlow()

    override suspend fun connect(deviceId: String): Boolean = false
    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    override suspend fun startDrying(): Boolean = false
    override suspend fun stopDrying(): Boolean = false
    override suspend fun getAvailableDevices(): List<DeviceInfo> = emptyList()
}
