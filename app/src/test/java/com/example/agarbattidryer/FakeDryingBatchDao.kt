package com.example.agarbattidryer

import com.example.agarbattidryer.data.local.DryingBatchDao
import com.example.agarbattidryer.data.local.DryingBatchEntity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FakeDryingBatchDao : DryingBatchDao {
    private val batches = mutableListOf<DryingBatchEntity>()
    private val batchesLiveData = MutableLiveData<List<DryingBatchEntity>>(emptyList())
    private var nextId = 1L

    override fun insertBatch(batch: DryingBatchEntity): Long {
        val newBatch = DryingBatchEntity().apply {
            this.id = nextId++
            this.startTime = batch.startTime
            this.endTime = batch.endTime
            this.durationSeconds = batch.durationSeconds
            this.startTemperature = batch.startTemperature
            this.endTemperature = batch.endTemperature
            this.startHumidity = batch.startHumidity
            this.endHumidity = batch.endHumidity
            this.status = batch.status
        }
        batches.add(0, newBatch) // Newest first
        batchesLiveData.value = batches.toList()
        return newBatch.id
    }

    override fun updateBatch(batch: DryingBatchEntity) {
        val index = batches.indexOfFirst { it.id == batch.id }
        if (index != -1) {
            batches[index] = batch
            batchesLiveData.value = batches.toList()
        }
    }

    override fun getAllBatches(): LiveData<List<DryingBatchEntity>> {
        return batchesLiveData
    }

    override fun getBatchById(batchId: Long): DryingBatchEntity? {
        return batches.find { it.id == batchId }
    }

    override fun deleteBatch(batchId: Long) {
        batches.removeAll { it.id == batchId }
        batchesLiveData.value = batches.toList()
    }

    override fun deleteAllBatches() {
        batches.clear()
        batchesLiveData.value = emptyList()
    }
}
