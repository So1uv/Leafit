package com.example.fitnesstracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnesstracker.data.entities.StepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records ORDER BY dayStart DESC")
    fun getAll(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE dayStart >= :since ORDER BY dayStart ASC")
    fun getSince(since: Long): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE dayStart = :dayStart LIMIT 1")
    suspend fun getForDay(dayStart: Long): StepRecord?

    @Query("SELECT * FROM step_records WHERE dayStart = :dayStart LIMIT 1")
    fun observeForDay(dayStart: Long): Flow<StepRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: StepRecord)

    @Query("DELETE FROM step_records")
    suspend fun clear()
}
