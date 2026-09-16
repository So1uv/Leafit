package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.SleepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records ORDER BY bedTime DESC")
    fun getAll(): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE bedTime >= :dayStart AND bedTime < :dayEnd LIMIT 1")
    fun getForDay(dayStart: Long, dayEnd: Long): Flow<SleepRecord?>

    @Query("SELECT * FROM sleep_records ORDER BY bedTime DESC LIMIT 1")
    fun getLatest(): Flow<SleepRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SleepRecord): Long

    @Delete
    suspend fun delete(record: SleepRecord)

    @Query("SELECT COUNT(*) FROM sleep_records")
    suspend fun count(): Int

    @Query("DELETE FROM sleep_records")
    suspend fun deleteAll()
}
