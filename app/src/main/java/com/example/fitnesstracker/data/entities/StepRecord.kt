package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey val dayStart: Long,
    val steps: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
