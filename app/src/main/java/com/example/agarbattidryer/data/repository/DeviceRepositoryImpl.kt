package com.example.agarbattidryer.data.repository

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import com.example.agarbattidryer.device.DeviceService
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [DeviceRepository] delegating to the injected [DeviceService].
 */
class DeviceRepositoryImpl(
    private val deviceService: com.example.agarbattidryer.device.DeviceService
) : DeviceRepository {

    fun getDynamicService(): com.example.agarbattidryer.device.DynamicDeviceService {
        return deviceService as com.example.agarbattidryer.device.DynamicDeviceService
    }

    override val connectionState: StateFlow<ConnectionState> = deviceService.connectionState
    override val deviceStatus: StateFlow<DeviceStatus> = deviceService.deviceStatus
    override val temperature: StateFlow<Float?> = deviceService.temperature
    override val humidity: StateFlow<Float?> = deviceService.humidity
    override val dryingDurationSeconds: StateFlow<Long> = deviceService.dryingDurationSeconds
    override val activeDevice: StateFlow<DeviceInfo?> = deviceService.activeDevice

    override suspend fun connect(deviceId: String): Boolean =
        deviceService.connect(deviceId)

    override suspend fun disconnect() =
        deviceService.disconnect()

    override suspend fun startDrying(): Boolean =
        deviceService.startDrying()

    override suspend fun stopDrying(): Boolean =
        deviceService.stopDrying()

    override suspend fun getAvailableDevices(): List<DeviceInfo> =
        deviceService.getAvailableDevices()
}
