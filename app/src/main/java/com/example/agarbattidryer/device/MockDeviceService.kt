package com.example.agarbattidryer.device

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Mock implementation of [DeviceService] that simulates an ESP32 micro-controller
 * attached to solar-powered agarbatti drying chamber sensors.
 *
 * Provides realistic temperature and humidity fluctuation curves,
 * accurate timer progression, and simulated hardware state transitions.
 */
class MockDeviceService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : DeviceService {

    private val defaultDevice = DeviceInfo(
        id = "ESP32-001",
        name = "AGARBATTI-DRYER-01",
        model = "Solar Solaris Chamber 1",
        signalStrengthDbm = -52,
        isDefault = true
    )

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceStatus = MutableStateFlow(DeviceStatus.READY)
    override val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _temperature = MutableStateFlow<Float?>(null)
    override val temperature: StateFlow<Float?> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow<Float?>(null)
    override val humidity: StateFlow<Float?> = _humidity.asStateFlow()

    private val _dryingDurationSeconds = MutableStateFlow(0L)
    override val dryingDurationSeconds: StateFlow<Long> = _dryingDurationSeconds.asStateFlow()

    private val _activeDevice = MutableStateFlow<DeviceInfo?>(null)
    override val activeDevice: StateFlow<DeviceInfo?> = _activeDevice.asStateFlow()

    private var simulationJob: Job? = null
    private var timerJob: Job? = null

    init {
        startAmbientSensorSimulation()
    }

    private fun startAmbientSensorSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var step = 0
            while (isActive) {
                delay(2000L)
                step++
                val isDrying = _deviceStatus.value == DeviceStatus.DRYING
                
                if (isDrying) {
                    // During drying: chamber heats up slightly and humidity drops progressively
                    val targetTemp = 33.5f + (step % 5) * 0.2f + (Random.nextFloat() * 0.2f - 0.1f)
                    val currentTemp = _temperature.value ?: 32.0f
                    _temperature.value = (currentTemp * 0.85f + targetTemp * 0.15f)
                        .coerceIn(31.5f, 36.0f)
                        .roundTo1Decimal()

                    val targetHumidity = (58.0f - (step * 0.15f)).coerceAtLeast(42.0f) + (Random.nextFloat() * 0.4f - 0.2f)
                    val currentHumidity = _humidity.value ?: 58.0f
                    _humidity.value = (currentHumidity * 0.85f + targetHumidity * 0.15f)
                        .coerceIn(40.0f, 65.0f)
                        .roundTo1Decimal()
                } else {
                    // Ambient idle fluctuations around 32.0°C and 58%
                    val jitterTemp = 32.0f + (Random.nextFloat() * 0.4f - 0.2f)
                    _temperature.value = jitterTemp.roundTo1Decimal()

                    val jitterHumidity = 58.0f + (Random.nextFloat() * 0.8f - 0.4f)
                    _humidity.value = jitterHumidity.roundTo1Decimal()
                }
            }
        }
    }

    override suspend fun connect(deviceId: String): Boolean {
        _connectionState.value = ConnectionState.CONNECTING
        delay(600L) // Simulate network/BLE handshake
        _connectionState.value = ConnectionState.CONNECTED
        _activeDevice.value = defaultDevice.copy(id = deviceId)
        _deviceStatus.value = DeviceStatus.READY
        return true
    }

    override suspend fun disconnect() {
        stopDrying()
        _connectionState.value = ConnectionState.DISCONNECTED
        _activeDevice.value = null
        _deviceStatus.value = DeviceStatus.DISCONNECTED
        _temperature.value = null
        _humidity.value = null
    }

    override suspend fun startDrying(): Boolean {
        if (_connectionState.value != ConnectionState.CONNECTED) return false
        
        _deviceStatus.value = DeviceStatus.DRYING
        _dryingDurationSeconds.value = 0L

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _deviceStatus.value == DeviceStatus.DRYING) {
                delay(1000L)
                _dryingDurationSeconds.value += 1L
            }
        }
        return true
    }

    override suspend fun stopDrying(): Boolean {
        timerJob?.cancel()
        timerJob = null
        _deviceStatus.value = DeviceStatus.READY
        return true
    }

    override suspend fun getAvailableDevices(): List<DeviceInfo> {
        return listOf(
            defaultDevice,
            DeviceInfo(
                id = "ESP32-002",
                name = "AGARBATTI-DRYER-02",
                model = "Solar Solaris Chamber 2",
                signalStrengthDbm = -68,
                isDefault = false
            ),
            DeviceInfo(
                id = "ESP32-003",
                name = "AGARBATTI-DRYER-03",
                model = "Solar Solaris Chamber 3",
                signalStrengthDbm = -82,
                isDefault = false
            )
        )
    }

    fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        timerJob?.cancel()
        timerJob = null
    }

    private fun Float.roundTo1Decimal(): Float =
        Math.round(this * 10f) / 10f
}
