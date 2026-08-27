package com.example.agarbattidryer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceStatus
import com.example.agarbattidryer.data.repository.DeviceRepository
import com.example.agarbattidryer.data.repository.DryingHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val deviceRepository: DeviceRepository,
    private val historyRepository: DryingHistoryRepository
) : ViewModel() {

    private var currentBatchId: Long? = null
    private val _userMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            deviceRepository.deviceStatus.collect { status ->
                if (status == DeviceStatus.COMPLETED) {
                    currentBatchId?.let { batchId ->
                        historyRepository.stopBatch(
                            batchId = batchId,
                            endTime = System.currentTimeMillis(),
                            durationSeconds = deviceRepository.dryingDurationSeconds.value,
                            endTemperature = deviceRepository.temperature.value,
                            endHumidity = deviceRepository.humidity.value,
                            status = "COMPLETED"
                        )
                        currentBatchId = null
                        _userMessage.value = "Drying completed automatically!"
                    }
                }
            }
        }
    }

    private val telemetryFlow = combine(
        deviceRepository.deviceStatus,
        deviceRepository.connectionState,
        deviceRepository.temperature,
        deviceRepository.humidity
    ) { status, connection, temp, hum ->
        DeviceTelemetry(status, connection, temp, hum)
    }

    private val sessionFlow = combine(
        deviceRepository.dryingDurationSeconds,
        deviceRepository.activeDevice,
        _userMessage
    ) { duration, device, message ->
        DeviceSession(duration, device?.name ?: "AGARBATTI-DRYER-01", message)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        telemetryFlow,
        sessionFlow
    ) { telemetry, session ->
        val isDrying = telemetry.status == DeviceStatus.DRYING
        val isConnected = telemetry.connection == ConnectionState.CONNECTED

        HomeUiState(
            deviceStatus = telemetry.status,
            connectionState = telemetry.connection,
            temperature = if (isConnected) telemetry.temperature else null,
            humidity = if (isConnected) telemetry.humidity else null,
            dryingDurationSeconds = session.duration,
            activeDeviceName = session.deviceName,
            isDrying = isDrying,
            canStart = isConnected && !isDrying,
            canStop = isConnected && isDrying,
            userMessage = session.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HomeUiState()
    )

    fun onStartDrying() {
        viewModelScope.launch {
            if (deviceRepository.deviceStatus.value == DeviceStatus.DRYING) return@launch
            
            val success = deviceRepository.startDrying()
            if (!success) {
                _userMessage.value = "Unable to start. Please check machine connection."
            } else {
                currentBatchId = historyRepository.startBatch(
                    startTime = System.currentTimeMillis(),
                    durationSeconds = 0,
                    startTemperature = deviceRepository.temperature.value,
                    startHumidity = deviceRepository.humidity.value
                )
            }
        }
    }

    fun onStopDrying() {
        viewModelScope.launch {
            if (deviceRepository.deviceStatus.value != DeviceStatus.DRYING) return@launch
            
            deviceRepository.stopDrying()
            
            currentBatchId?.let { batchId ->
                historyRepository.stopBatch(
                    batchId = batchId,
                    endTime = System.currentTimeMillis(),
                    durationSeconds = deviceRepository.dryingDurationSeconds.value,
                    endTemperature = deviceRepository.temperature.value,
                    endHumidity = deviceRepository.humidity.value,
                    status = "STOPPED"
                )
                currentBatchId = null
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    companion object {
        fun provideFactory(
            deviceRepository: DeviceRepository,
            historyRepository: DryingHistoryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(deviceRepository, historyRepository) as T
            }
        }
    }
}

private data class DeviceTelemetry(
    val status: DeviceStatus,
    val connection: ConnectionState,
    val temperature: Float?,
    val humidity: Float?
)

private data class DeviceSession(
    val duration: Long,
    val deviceName: String,
    val message: String?
)
