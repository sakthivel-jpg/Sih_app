package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DynamicDeviceService(
    private val wifiService: WifiDeviceService
) : DeviceService {
    
    // We only have one mode now
    private val _currentMode = MutableStateFlow("WIFI")

    override val connectionState: StateFlow<ConnectionState> = wifiService.connectionState

    override val deviceStatus: StateFlow<DeviceStatus> = wifiService.deviceStatus

    override val temperature: StateFlow<Float?> = wifiService.temperature

    override val humidity: StateFlow<Float?> = wifiService.humidity

    override val dryingDurationSeconds: StateFlow<Long> = wifiService.dryingDurationSeconds

    override val activeDevice: StateFlow<DeviceInfo?> = wifiService.activeDevice

    override suspend fun connect(deviceId: String): Boolean = wifiService.connect(deviceId)
    
    override suspend fun disconnect() = wifiService.disconnect()
    
    override suspend fun startDrying(): Boolean = wifiService.startDrying()
    
    override suspend fun stopDrying(): Boolean = wifiService.stopDrying()
    
    override suspend fun getAvailableDevices(): List<DeviceInfo> = wifiService.getAvailableDevices()
}
