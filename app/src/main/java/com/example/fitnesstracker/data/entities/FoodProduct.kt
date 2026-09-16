package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_products")
data class FoodProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val caloriesPer100: Float = 0f,
    val proteinsPer100: Float = 0f,
    val fatsPer100: Float = 0f,
    val carbsPer100: Float = 0f
)
