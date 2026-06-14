package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.WaterRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp ASC")
    fun getForDay(dayStart: Long, dayEnd: Long): Flow<List<WaterRecord>>

    @Query("SELECT SUM(amountMl) FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun totalMlForDay(dayStart: Long, dayEnd: Long): Flow<Int?>

    @Query("SELECT * FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestToday(dayStart: Long, dayEnd: Long): WaterRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WaterRecord): Long

    @Delete
    suspend fun delete(record: WaterRecord)

    @Query("DELETE FROM water_records")
    suspend fun deleteAll()
}
