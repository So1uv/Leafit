package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сутність прийому їжі.
 * Зберігає назву страви, тег прийому, БЖУ та калорії.
 */
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),   // timestamp дня (midnight)
    val mealType: String = "Сніданок",             // Сніданок / Обід / Перекус / Вечеря
    val name: String = "",
    val calories: Float = 0f,
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val carbs: Float = 0f
)
