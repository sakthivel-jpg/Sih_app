package com.example.agarbattidryer

import com.example.agarbattidryer.data.model.ConnectionState
import com.example.agarbattidryer.data.model.DeviceStatus
import com.example.agarbattidryer.device.MockDeviceService
import android.content.Context
import com.example.agarbattidryer.data.repository.DeviceRepositoryImpl
import com.example.agarbattidryer.data.repository.DryingHistoryRepository
import com.example.agarbattidryer.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_reflectsReadyState() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        val repository = DeviceRepositoryImpl(deviceService)
        val historyRepository = DryingHistoryRepository(FakeDryingBatchDao())
        val viewModel = HomeViewModel(repository, historyRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DeviceStatus.READY, state.deviceStatus)
        assertEquals(ConnectionState.DISCONNECTED, state.connectionState)
        assertFalse(state.isDrying)
        assertFalse(state.canStart)
        assertFalse(state.canStop)
    }

    @Test
    fun onStartDrying_triggersDryingState() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        val repository = DeviceRepositoryImpl(deviceService)
        val historyRepository = DryingHistoryRepository(FakeDryingBatchDao())
        val viewModel = HomeViewModel(repository, historyRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        deviceService.connect("MOCK_ID")
        advanceUntilIdle()

        viewModel.onStartDrying()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DeviceStatus.DRYING, state.deviceStatus)
        assertTrue(state.isDrying)
        assertTrue(state.canStop)
        assertFalse(state.canStart)
    }

    @Test
    fun onStopDrying_returnsToReadyState() = runTest(testDispatcher) {
        val deviceService = MockDeviceService(scope = backgroundScope)
        val repository = DeviceRepositoryImpl(deviceService)
        val historyRepository = DryingHistoryRepository(FakeDryingBatchDao())
        val viewModel = HomeViewModel(repository, historyRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        deviceService.connect("MOCK_ID")
        advanceUntilIdle()

        viewModel.onStartDrying()
        advanceUntilIdle()

        viewModel.onStopDrying()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DeviceStatus.READY, state.deviceStatus)
        assertFalse(state.isDrying)
        assertTrue(state.canStart)
        assertFalse(state.canStop)
    }
}
