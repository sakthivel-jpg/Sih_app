package com.example.agarbattidryer

import com.example.agarbattidryer.data.local.DryingBatchEntity
import com.example.agarbattidryer.data.repository.DryingHistoryRepository
import com.example.agarbattidryer.ui.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeDryingBatchDao
    private lateinit var repository: DryingHistoryRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeDryingBatchDao()
        repository = DryingHistoryRepository(fakeDao)
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialHistory_isEmpty() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.historyBatches.collect()
        }
        advanceUntilIdle()

        assertTrue(viewModel.historyBatches.value.isEmpty())
    }

    @Test
    fun historyOrdering_isNewestFirst() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.historyBatches.collect()
        }

        repository.startBatch(100L, 0, 30f, 60f)
        repository.startBatch(200L, 0, 31f, 61f)
        advanceUntilIdle()

        val batches = viewModel.historyBatches.value
        assertEquals(2, batches.size)
        assertEquals(200L, batches[0].startTime) // The newest one should be first
        assertEquals(100L, batches[1].startTime)
    }

    @Test
    fun deleteBatch_removesBatch() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.historyBatches.collect()
        }

        val id = repository.startBatch(100L, 0, 30f, 60f)
        advanceUntilIdle()

        assertEquals(1, viewModel.historyBatches.value.size)

        viewModel.deleteBatch(id)
        advanceUntilIdle()
        Thread.sleep(100)
        advanceUntilIdle()

        assertTrue(viewModel.historyBatches.value.isEmpty())
    }

    @Test
    fun clearHistory_removesAllBatches() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.historyBatches.collect()
        }

        repository.startBatch(100L, 0, 30f, 60f)
        repository.startBatch(200L, 0, 31f, 61f)
        advanceUntilIdle()

        assertEquals(2, viewModel.historyBatches.value.size)

        viewModel.clearHistory()
        advanceUntilIdle()
        Thread.sleep(100)
        advanceUntilIdle()

        assertTrue(viewModel.historyBatches.value.isEmpty())
    }
}
