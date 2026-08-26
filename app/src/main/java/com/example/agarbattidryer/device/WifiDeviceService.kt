package com.example.agarbattidryer.device

import android.util.Log
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class WifiDeviceService : DeviceService {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceStatus = MutableStateFlow(DeviceStatus.READY)
    override val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

    private val _temperature = MutableStateFlow(0.0f)
    override val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow(0.0f)
    override val humidity: StateFlow<Float> = _humidity.asStateFlow()

    private val _dryingDurationSeconds = MutableStateFlow(0L)
    override val dryingDurationSeconds: StateFlow<Long> = _dryingDurationSeconds.asStateFlow()

    private val _activeDevice = MutableStateFlow<DeviceInfo?>(null)
    override val activeDevice: StateFlow<DeviceInfo?> = _activeDevice.asStateFlow()
    
    private var isPolling = false
    private var deviceIp = ""

    override suspend fun connect(deviceId: String): Boolean {
        // deviceId is expected to be IP address in this context
        deviceIp = deviceId
        _connectionState.value = ConnectionState.CONNECTING
        
        return withContext(Dispatchers.IO) {
            try {
                val response = getRequest("http://$deviceIp/api/status")
                if (response.isNotEmpty()) {
                    _connectionState.value = ConnectionState.CONNECTED
                    _activeDevice.value = DeviceInfo(deviceIp, "ESP32-WIFI")
                    startPolling()
                    true
                } else {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    false
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.DISCONNECTED
                false
            }
        }
    }

    override suspend fun disconnect() {
        isPolling = false
        _connectionState.value = ConnectionState.DISCONNECTED
        _activeDevice.value = null
    }

    override suspend fun startDrying(): Boolean {
        return postRequest("http://$deviceIp/api/start")
    }

    override suspend fun stopDrying(): Boolean {
        return postRequest("http://$deviceIp/api/stop")
    }

    override suspend fun getAvailableDevices(): List<DeviceInfo> {
        return listOf(DeviceInfo("192.168.4.1", "Local ESP32"))
    }

    private fun startPolling() {
        isPolling = true
        thread {
            while (isPolling) {
                try {
                    val json = getRequest("http://$deviceIp/api/sensor")
                    val tempMatch = Regex("\"temperature\":\\s*([0-9.]+)").find(json)
                    val humMatch = Regex("\"humidity\":\\s*([0-9.]+)").find(json)
                    val stateMatch = Regex("\"state\":\\s*\"([A-Z_]+)\"").find(json)
                    val timeMatch = Regex("\"elapsedSeconds\":\\s*([0-9]+)").find(json)

                    tempMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { _temperature.value = it }
                    humMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { _humidity.value = it }
                    timeMatch?.groupValues?.get(1)?.toLongOrNull()?.let { _dryingDurationSeconds.value = it }
                    
                    stateMatch?.groupValues?.get(1)?.let { state ->
                        _deviceStatus.value = when (state) {
                            "DRYING" -> DeviceStatus.DRYING
                            "STOPPED" -> DeviceStatus.STOPPED
                            "COMPLETED" -> DeviceStatus.COMPLETED
                            "ERROR" -> DeviceStatus.ERROR
                            else -> DeviceStatus.READY
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WiFi", "Polling error", e)
                }
                Thread.sleep(2000)
            }
        }
    }

    private fun getRequest(urlString: String): String {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    private fun postRequest(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val responseCode = connection.responseCode
            responseCode == 200
        } catch (e: Exception) {
            false
        }
    }
}
