package com.example.fitnesstracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun todayEnd(): Long = dayEnd(System.currentTimeMillis())

    fun weekStart(): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis

    }

    fun dayStart(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun dayEnd(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = dayStart(timestamp)
        add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(Date(timestamp))

    fun formatTime(timestamp: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatDateTime(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
        else "%02d:%02d".format(m, s)
    }

    fun getDayOfWeek(timestamp: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    }
}

object HealthCalc {

    fun bmi(weightKg: Float, heightCm: Float): Float {
        val h = heightCm / 100f
        return weightKg / (h * h)
    }

    fun bmiCategory(bmi: Float): String = when {
        bmi < 18.5f -> "underweight"
        bmi < 25f   -> "normal"
        bmi < 30f   -> "overweight"
        else        -> "obese"
    }

    fun dailyCalories(weightKg: Float, heightCm: Float, age: Int, gender: String): Float {
        val bmr = when (gender) {
            "male" -> 10 * weightKg + 6.25f * heightCm - 5 * age + 5
            "female"   -> 10 * weightKg + 6.25f * heightCm - 5 * age - 161
            else      -> 10 * weightKg + 6.25f * heightCm - 5 * age - 78
        }
        return bmr * 1.2f
    }

    fun workoutCalories(weightKg: Float, durationSeconds: Long): Float {
        val minutes = durationSeconds / 60f
        return 3.5f * weightKg * minutes / 200f * 5f
    }
}
