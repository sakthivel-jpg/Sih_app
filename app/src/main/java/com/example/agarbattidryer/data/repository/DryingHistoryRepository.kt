package com.example.agarbattidryer.data.repository

import com.example.agarbattidryer.data.local.DryingBatchDao
import com.example.agarbattidryer.data.local.DryingBatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import androidx.lifecycle.asFlow

class DryingHistoryRepository(private val dao: DryingBatchDao) {

    fun observeHistory(): Flow<List<DryingBatchEntity>> = dao.getAllBatches().asFlow()

    suspend fun startBatch(
        startTime: Long,
        durationSeconds: Long,
        startTemperature: Float,
        startHumidity: Float
    ): Long = withContext(Dispatchers.IO) {
        val batch = DryingBatchEntity().apply {
            this.startTime = startTime
            this.endTime = null
            this.durationSeconds = durationSeconds
            this.startTemperature = startTemperature
            this.endTemperature = null
            this.startHumidity = startHumidity
            this.endHumidity = null
            this.status = "STARTED" // Will be updated to COMPLETED or STOPPED
        }
        return@withContext dao.insertBatch(batch)
    }

    suspend fun stopBatch(
        batchId: Long,
        endTime: Long,
        durationSeconds: Long,
        endTemperature: Float,
        endHumidity: Float,
        status: String
    ) = withContext(Dispatchers.IO) {
        val batch = dao.getBatchById(batchId)
        if (batch != null) {
            batch.endTime = endTime
            batch.durationSeconds = durationSeconds
            batch.endTemperature = endTemperature
            batch.endHumidity = endHumidity
            batch.status = status
            dao.updateBatch(batch)
        }
    }

    suspend fun deleteBatch(batchId: Long) = withContext(Dispatchers.IO) {
        dao.deleteBatch(batchId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.deleteAllBatches()
    }
}
