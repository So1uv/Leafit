package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сутність досягнення користувача.
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,       // унікальний ключ, напр. "first_workout"
    val title: String,
    val description: String,
    val iconName: String = "emoji_events",
    val unlockedAt: Long? = null      // null = ще не отримано
)
