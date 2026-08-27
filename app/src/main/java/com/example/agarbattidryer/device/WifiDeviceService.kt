package com.example.agarbattidryer.device

import android.content.Context
import android.util.Log
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class WifiDeviceService(context: Context) : DeviceService {

    private val prefs = context.getSharedPreferences("wifi_device_prefs", Context.MODE_PRIVATE)

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
    
    private var isPolling = false
    private var pollingJob: Job? = null
    private var deviceIp = ""
    private var devicePort = "80"
    
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Restore active device on startup
        val savedIp = prefs.getString("ip", null)
        val savedPort = prefs.getString("port", "80")
        if (savedIp != null) {
            deviceIp = savedIp
            devicePort = savedPort ?: "80"
            _activeDevice.value = DeviceInfo(deviceIp, "AGARBATTI-DRYER-01")
            _connectionState.value = ConnectionState.RECONNECTING
            startPolling()
        }
    }

    override suspend fun connect(deviceId: String): Boolean {
        val parts = deviceId.split(":")
        deviceIp = parts.getOrNull(0) ?: ""
        devicePort = parts.getOrNull(1) ?: "80"
        
        _connectionState.value = ConnectionState.CONNECTING
        
        return withContext(Dispatchers.IO) {
            try {
                // Initial explicit connect attempt
                val healthResponse = getRequest("http://$deviceIp:$devicePort/health")
                if (healthResponse.isNotEmpty()) {
                    val json = JSONObject(healthResponse)
                    if (json.optString("status") == "OK") {
                        val deviceName = json.optString("device", "ESP32-WIFI")
                        
                        // Save to prefs
                        prefs.edit()
                            .putString("ip", deviceIp)
                            .putString("port", devicePort)
                            .apply()
                            
                        _connectionState.value = ConnectionState.CONNECTED
                        _activeDevice.value = DeviceInfo(deviceIp, deviceName)
                        startPolling()
                        return@withContext true
                    }
                }
                
                // If explicit connect fails, clear prefs so we don't auto-reconnect
                handleUserDisconnection()
                false
            } catch (e: Exception) {
                Log.e("WiFi", "Connect error", e)
                handleUserDisconnection()
                false
            }
        }
    }

    override suspend fun disconnect() {
        // Explicit user disconnect
        handleUserDisconnection()
    }

    private fun handleUserDisconnection() {
        stopPolling()
        timerJob?.cancel()
        timerJob = null
        prefs.edit().clear().apply()
        
        _connectionState.value = ConnectionState.DISCONNECTED
        _activeDevice.value = null
        _temperature.value = null
        _humidity.value = null
        _dryingDurationSeconds.value = 0L
    }

    override suspend fun startDrying(): Boolean {
        return withContext(Dispatchers.IO) {
            val response = postRequest("http://$deviceIp:$devicePort/drying/start")
            if (response.isNotEmpty()) {
                try {
                    val json = JSONObject(response)
                    if (json.optBoolean("success", false)) {
                        updateStatusFromJson(json.optString("status", ""))
                        true
                    } else false
                } catch (e: Exception) {
                    false
                }
            } else false
        }
    }

    override suspend fun stopDrying(): Boolean {
        return withContext(Dispatchers.IO) {
            val response = postRequest("http://$deviceIp:$devicePort/drying/stop")
            if (response.isNotEmpty()) {
                try {
                    val json = JSONObject(response)
                    if (json.optBoolean("success", false)) {
                        updateStatusFromJson(json.optString("status", ""))
                        true
                    } else false
                } catch (e: Exception) {
                    false
                }
            } else false
        }
    }

    override suspend fun getAvailableDevices(): List<DeviceInfo> {
        return emptyList() // Manual IP entry only
    }

    private var timerJob: Job? = null

    private fun updateStatusFromJson(state: String) {
        val newStatus = when (state) {
            "DRYING" -> DeviceStatus.DRYING
            "STOPPED" -> DeviceStatus.STOPPED
            "COMPLETED" -> DeviceStatus.COMPLETED
            "ERROR" -> DeviceStatus.ERROR
            else -> DeviceStatus.READY
        }
        
        if (newStatus == DeviceStatus.DRYING) {
            if (timerJob == null) {
                var startTime = prefs.getLong("drying_start_time", 0L)
                if (startTime == 0L) {
                    startTime = System.currentTimeMillis()
                    prefs.edit().putLong("drying_start_time", startTime).apply()
                }
                
                timerJob = scope.launch {
                    while (true) {
                        val elapsedMillis = System.currentTimeMillis() - startTime
                        _dryingDurationSeconds.value = elapsedMillis / 1000
                        delay(1000)
                    }
                }
            }
        } else {
            timerJob?.cancel()
            timerJob = null
            prefs.edit().remove("drying_start_time").apply()
            // Leave _dryingDurationSeconds.value as is, so the final duration remains visible
        }

        _deviceStatus.value = newStatus
    }

    private fun stopPolling() {
        isPolling = false
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun startPolling() {
        // Prevent duplicate polling loops
        if (isPolling) return
        
        isPolling = true
        pollingJob = scope.launch {
            while (isPolling) {
                try {
                    val jsonString = getRequest("http://$deviceIp:$devicePort/status")
                    if (jsonString.isEmpty()) {
                        handleNetworkFailure()
                    } else {
                        val json = JSONObject(jsonString)
                        
                        _connectionState.value = ConnectionState.CONNECTED
                        
                        if (json.isNull("temperature")) {
                            _temperature.value = null
                        } else {
                            _temperature.value = json.optDouble("temperature").toFloat()
                        }

                        if (json.isNull("humidity")) {
                            _humidity.value = null
                        } else {
                            _humidity.value = json.optDouble("humidity").toFloat()
                        }
                        
                        updateStatusFromJson(json.optString("status", "READY"))
                    }
                } catch (e: Exception) {
                    Log.e("WiFi", "Polling error", e)
                    handleNetworkFailure()
                }
                delay(2000)
            }
        }
    }
    
    private fun handleNetworkFailure() {
        // Do not clear prefs. Set state to RECONNECTING.
        _connectionState.value = ConnectionState.RECONNECTING
        _temperature.value = null
        _humidity.value = null
    }

    private fun getRequest(urlString: String): String {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun postRequest(urlString: String): String {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.setRequestProperty("Content-Length", "0")
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
