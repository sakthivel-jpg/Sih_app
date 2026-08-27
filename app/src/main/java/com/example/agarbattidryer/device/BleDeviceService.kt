package com.example.agarbattidryer.device

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceInfo
import com.example.agarbattidryer.data.model.DeviceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class BleDeviceService(private val context: Context) : DeviceService {

    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHAR_UUID_STATUS = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a7")
    private val CHAR_UUID_SENSOR = UUID.fromString("7b7a0003-7b7a-4f8a-9a10-000000000003")
    private val CHAR_UUID_COMMAND = UUID.fromString("7b7a0004-7b7a-4f8a-9a10-000000000004")
    private val CHAR_UUID_CONFIG = UUID.fromString("7b7a0005-7b7a-4f8a-9a10-000000000005")

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

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null

    private var cmdCharacteristic: BluetoothGattCharacteristic? = null

    override suspend fun connect(deviceId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return false
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        
        _connectionState.value = ConnectionState.CONNECTING
        val device = bluetoothAdapter.getRemoteDevice(deviceId)
        
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        return true
    }

    override suspend fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            // Already lack permission, just clear local state
        } else {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        }
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _activeDevice.value = null
        _temperature.value = null
        _humidity.value = null
    }

    override suspend fun startDrying(): Boolean {
        return sendCommand("START_DRYING")
    }

    override suspend fun stopDrying(): Boolean {
        return sendCommand("STOP_DRYING")
    }
    
    private fun sendCommand(cmd: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val gatt = bluetoothGatt ?: return false
        val char = cmdCharacteristic ?: return false
        char.value = cmd.toByteArray()
        return gatt.writeCharacteristic(char)
    }

    override suspend fun getAvailableDevices(): List<DeviceInfo> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyList()
        }
        return listOf(DeviceInfo("00:11:22:33:44:55", "AGARBATTI-DRYER-01"))
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                _connectionState.value = ConnectionState.CONNECTED
                _activeDevice.value = DeviceInfo(gatt.device.address, gatt.device.name ?: "Unknown")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
                _deviceStatus.value = DeviceStatus.ERROR
                _temperature.value = null
                _humidity.value = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    cmdCharacteristic = service.getCharacteristic(CHAR_UUID_COMMAND)
                    val sensorChar = service.getCharacteristic(CHAR_UUID_SENSOR)
                    val statusChar = service.getCharacteristic(CHAR_UUID_STATUS)
                    
                    // Enable notifications
                    sensorChar?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        gatt.setCharacteristicNotification(it, true)
                        val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.getStringValue(0)
            if (characteristic.uuid == CHAR_UUID_SENSOR) {
                try {
                    val tempMatch = Regex("\"temperature\":\\s*([0-9.]+)").find(value)
                    val humMatch = Regex("\"humidity\":\\s*([0-9.]+)").find(value)
                    val stateMatch = Regex("\"state\":\\s*\"([A-Z_]+)\"").find(value)
                    val timeMatch = Regex("\"elapsedSeconds\":\\s*([0-9]+)").find(value)

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
                    Log.e("BLE", "JSON parse error", e)
                }
            }
        }
    }
}
