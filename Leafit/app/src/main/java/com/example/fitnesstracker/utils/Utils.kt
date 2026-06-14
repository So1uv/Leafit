package com.example.fitnesstracker.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Утилітні функції для роботи з датами та розрахунками.
 */
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

    fun todayEnd(): Long = todayStart() + 24 * 60 * 60 * 1000L

    fun weekStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
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

    fun dayEnd(timestamp: Long): Long = dayStart(timestamp) + 24 * 60 * 60 * 1000L

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))

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
    /** ІМТ */
    fun bmi(weightKg: Float, heightCm: Float): Float {
        val h = heightCm / 100f
        return weightKg / (h * h)
    }

    fun bmiCategory(bmi: Float): String = when {
        bmi < 18.5f -> "Недостатня вага"
        bmi < 25f   -> "Норма"
        bmi < 30f   -> "Надлишкова вага"
        else        -> "Ожиріння"
    }

    /**
     * Денна квота калорій (формула Міффліна–Сан Жеора, седентарний коефіцієнт 1.2)
     */
    fun dailyCalories(weightKg: Float, heightCm: Float, age: Int, gender: String): Float {
        val bmr = when (gender) {
            "Чоловік" -> 10 * weightKg + 6.25f * heightCm - 5 * age + 5
            "Жінка"   -> 10 * weightKg + 6.25f * heightCm - 5 * age - 161
            else      -> 10 * weightKg + 6.25f * heightCm - 5 * age - 78 // середнє між +5 та -161
        }
        return bmr * 1.2f
    }

    /** Приблизні калорії за тренування (МЕТ ходьба 3.5) */
    fun workoutCalories(weightKg: Float, durationSeconds: Long): Float {
        val minutes = durationSeconds / 60f
        return 3.5f * weightKg * minutes / 200f * 5f
    }
}
