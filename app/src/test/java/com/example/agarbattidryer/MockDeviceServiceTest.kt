package com.example.agarbattidryer

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceStatus
import com.example.agarbattidryer.device.MockDeviceService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockDeviceServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun initialState_isDisconnectedAndReady() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        assertEquals(ConnectionState.DISCONNECTED, deviceService.connectionState.value)
        assertEquals(DeviceStatus.READY, deviceService.deviceStatus.value)
        org.junit.Assert.assertNull(deviceService.activeDevice.value)
        assertEquals(0L, deviceService.dryingDurationSeconds.value)
    }

    @Test
    fun startDrying_transitionsToDryingAndIncrementsTimer() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        deviceService.connect("MOCK")
        val started = deviceService.startDrying()
        assertTrue(started)
        assertEquals(DeviceStatus.DRYING, deviceService.deviceStatus.value)

        advanceTimeBy(3500L)
        assertTrue(deviceService.dryingDurationSeconds.value >= 3L)
    }

    @Test
    fun stopDrying_haltsTimerAndResetsStatusToReady() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        deviceService.connect("MOCK")
        deviceService.startDrying()
        advanceTimeBy(2000L)

        val stopped = deviceService.stopDrying()
        assertTrue(stopped)
        assertEquals(DeviceStatus.READY, deviceService.deviceStatus.value)
    }

    @Test
    fun connectAndDisconnect_updatesConnectionState() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        deviceService.connect("ESP32-001")
        assertEquals(ConnectionState.CONNECTED, deviceService.connectionState.value)

        deviceService.disconnect()
        assertEquals(ConnectionState.DISCONNECTED, deviceService.connectionState.value)
    }
}
