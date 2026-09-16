package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedTime: Long = System.currentTimeMillis(),
    val wakeTime: Long = System.currentTimeMillis(),
    val qualityScore: Int = 3,
    val tags: String = "",
    val note: String = ""
)
