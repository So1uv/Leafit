package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val gender: String = "other",
    val age: Int = 0,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val avatarUri: String? = null,
    val chestCm: Float? = null,
    val waistCm: Float? = null,
    val hipsCm: Float? = null,
    val bicepCm: Float? = null
)
