package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY CASE WHEN unlockedAt IS NULL THEN 1 ELSE 0 END, unlockedAt DESC")
    fun getAll(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlocked(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<Achievement>)

    @Query("UPDATE achievements SET unlockedAt = :time WHERE id = :id AND unlockedAt IS NULL")
    suspend fun unlock(id: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE achievements SET unlockedAt = NULL")
    suspend fun resetAll()

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun count(): Int
}
