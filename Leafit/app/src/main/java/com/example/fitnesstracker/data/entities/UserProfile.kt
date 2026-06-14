package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сутність профілю користувача.
 * Зберігає ім'я, антропометричні дані та аватарку.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val gender: String = "Не вказано",  // "Чоловік" / "Жінка"
    val age: Int = 0,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val avatarUri: String? = null
)
