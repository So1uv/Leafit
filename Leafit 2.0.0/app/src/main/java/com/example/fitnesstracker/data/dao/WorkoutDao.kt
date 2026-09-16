package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAll(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE startTime >= :dayStart AND startTime < :dayEnd")
    fun getForDay(dayStart: Long, dayEnd: Long): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: Workout): Long

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Workout?

    @Query("SELECT id FROM workouts WHERE instr(metrics, :token) > 0 LIMIT 1")
    suspend fun findLiveSession(token: String): Long?

    @Transaction
    suspend fun insertLiveOnce(workout: Workout, token: String): Long {
        require(token.length >= 32)
        return findLiveSession(token) ?: insert(workout)
    }

    @Update
    suspend fun update(workout: Workout): Int

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE startTime >= :weekStart")
    fun totalCaloriesThisWeek(weekStart: Long): Flow<Float?>

    @Query("SELECT startTime FROM workouts WHERE startTime >= :weekStart ORDER BY startTime ASC")
    fun workoutDaysThisWeek(weekStart: Long): Flow<List<Long>>

    @Query("SELECT * FROM workouts WHERE startTime >= :weekStart")
    fun workoutsThisWeek(weekStart: Long): Flow<List<Workout>>

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun count(): Int

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}
