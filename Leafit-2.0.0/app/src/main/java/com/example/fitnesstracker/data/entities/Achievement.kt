package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String = "emoji_events",
    val unlockedAt: Long? = null
)

fun Achievement.localizedTitle(context: android.content.Context): String {
    val rid = context.resources.getIdentifier(title, "string", context.packageName)
    if (rid != 0) return context.getString(rid)
    val legacyMap = mapOf(
        "Перше тренування" to "ach_first_workout_title",
        "10 тренувань" to "ach_ten_workouts_title",
        "Водний тиждень" to "ach_water_week_title",
        "Сонний тиждень" to "ach_sleep_week_title",
        "Норма калорій" to "ach_calorie_goal_title",
        "Профіль готовий" to "ach_profile_complete_title",
        "Перша страва" to "ach_first_meal_title",
        "Перший сон" to "ach_first_sleep_title"
    )
    val key = legacyMap[title]
    if (key != null) {
        val rid2 = context.resources.getIdentifier(key, "string", context.packageName)
        if (rid2 != 0) return context.getString(rid2)
    }
    return title
}

fun Achievement.localizedDesc(context: android.content.Context): String {
    val rid = context.resources.getIdentifier(description, "string", context.packageName)
    if (rid != 0) return context.getString(rid)
    val legacyMap = mapOf(
        "Завершено перше тренування" to "ach_first_workout_desc",
        "Завершено 10 тренувань" to "ach_ten_workouts_desc",
        "Виконано норму води 7 днів поспіль" to "ach_water_week_desc",
        "Зафіксовано сон 7 днів поспіль" to "ach_sleep_week_desc",
        "Вперше виконано денну норму калорій" to "ach_calorie_goal_desc",
        "Заповнено всі дані профілю" to "ach_profile_complete_desc",
        "Додано перший запис харчування" to "ach_first_meal_desc",
        "Додано перший запис сну" to "ach_first_sleep_desc"
    )
    val key = legacyMap[description]
    if (key != null) {
        val rid2 = context.resources.getIdentifier(key, "string", context.packageName)
        if (rid2 != 0) return context.getString(rid2)
    }
    return description
}
