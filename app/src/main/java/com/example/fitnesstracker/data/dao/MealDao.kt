package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE date >= :dayStart AND date < :dayEnd ORDER BY date ASC")
    fun getMealsForDay(dayStart: Long, dayEnd: Long): Flow<List<Meal>>

    @Query("SELECT * FROM meals ORDER BY date ASC")
    suspend fun getAllMealsOnce(): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal): Long

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Query("SELECT DISTINCT date FROM meals ORDER BY date DESC")
    fun getAvailableDates(): Flow<List<Long>>

    @Query("DELETE FROM meals")
    suspend fun deleteAll()

    @Query("SELECT * FROM saved_meals ORDER BY name ASC")
    fun getSavedMeals(): Flow<List<com.example.fitnesstracker.data.entities.SavedMeal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaved(meal: com.example.fitnesstracker.data.entities.SavedMeal): Long

    @Delete
    suspend fun deleteSaved(meal: com.example.fitnesstracker.data.entities.SavedMeal)
}
