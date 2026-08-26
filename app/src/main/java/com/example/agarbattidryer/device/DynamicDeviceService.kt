package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class DeviceMode {
    MOCK,
    BLE,
    WIFI
}

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicDeviceService(
    private val mockService: MockDeviceService,
    private val bleService: BleDeviceService,
    private val wifiService: WifiDeviceService
) : DeviceService {

    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _currentMode = MutableStateFlow(DeviceMode.MOCK)
    val currentMode: StateFlow<DeviceMode> = _currentMode

    private val activeService = MutableStateFlow<DeviceService>(mockService)

    fun setMode(mode: DeviceMode) {
        _currentMode.value = mode
        val newService = when (mode) {
            DeviceMode.MOCK -> mockService
            DeviceMode.BLE -> bleService
            DeviceMode.WIFI -> wifiService
        }
        
        // Disconnect old before switching (fire and forget)
        val oldService = activeService.value
        if (oldService != newService) {
            scope.launch(Dispatchers.IO) {
                oldService.disconnect()
            }
        }
        
        activeService.value = newService
    }

    override val connectionState: StateFlow<ConnectionState> = 
        activeService.flatMapLatest { it.connectionState }
            .stateIn(scope, SharingStarted.Eagerly, ConnectionState.DISCONNECTED)

    override val deviceStatus: StateFlow<DeviceStatus> = 
        activeService.flatMapLatest { it.deviceStatus }
            .stateIn(scope, SharingStarted.Eagerly, DeviceStatus.READY)

    override val temperature: StateFlow<Float> = 
        activeService.flatMapLatest { it.temperature }
            .stateIn(scope, SharingStarted.Eagerly, 0.0f)

    override val humidity: StateFlow<Float> = 
        activeService.flatMapLatest { it.humidity }
            .stateIn(scope, SharingStarted.Eagerly, 0.0f)

    override val dryingDurationSeconds: StateFlow<Long> = 
        activeService.flatMapLatest { it.dryingDurationSeconds }
            .stateIn(scope, SharingStarted.Eagerly, 0L)

    override val activeDevice: StateFlow<DeviceInfo?> = 
        activeService.flatMapLatest { it.activeDevice }
            .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun connect(deviceId: String): Boolean = activeService.value.connect(deviceId)
    override suspend fun disconnect() = activeService.value.disconnect()
    override suspend fun startDrying(): Boolean = activeService.value.startDrying()
    override suspend fun stopDrying(): Boolean = activeService.value.stopDrying()
    override suspend fun getAvailableDevices(): List<DeviceInfo> = activeService.value.getAvailableDevices()
}
