package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сутність запису сну.
 * Зберігає час вкладання, пробудження, якість та теги.
 */
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedTime: Long = System.currentTimeMillis(),      // timestamp
    val wakeTime: Long = System.currentTimeMillis(),     // timestamp
    val qualityScore: Int = 3,                           // 1..5
    val tags: String = "",                               // через кому: "Хропіння,Кошмари"
    val note: String = ""
)
