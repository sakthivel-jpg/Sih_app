package com.example.agarbattidryer.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drying_batches")
public class DryingBatchEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long startTime;
    public Long endTime;
    public long durationSeconds;
    public Float startTemperature;
    public Float endTemperature;
    public Float startHumidity;
    public Float endHumidity;
    public String status; // "COMPLETED" or "STOPPED"
}
