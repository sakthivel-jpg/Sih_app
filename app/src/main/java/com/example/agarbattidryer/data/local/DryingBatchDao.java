package com.example.agarbattidryer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DryingBatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBatch(DryingBatchEntity batch);

    @Update
    void updateBatch(DryingBatchEntity batch);

    @Query("SELECT * FROM drying_batches ORDER BY startTime DESC")
    LiveData<List<DryingBatchEntity>> getAllBatches();

    @Query("SELECT * FROM drying_batches WHERE id = :batchId")
    DryingBatchEntity getBatchById(long batchId);

    @Query("DELETE FROM drying_batches WHERE id = :batchId")
    void deleteBatch(long batchId);

    @Query("DELETE FROM drying_batches")
    void deleteAllBatches();
}
