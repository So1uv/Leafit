package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сутність тренування.
 * Зберігає тривалість, дистанцію, кроки та калорії.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val distanceMeters: Float = 0f,
    val steps: Int = 0,
    val caloriesBurned: Float = 0f,
    val note: String = ""
)
