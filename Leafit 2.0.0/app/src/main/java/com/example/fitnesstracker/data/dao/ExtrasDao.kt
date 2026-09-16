package com.example.fitnesstracker.data.dao

import androidx.room.*
import com.example.fitnesstracker.data.entities.DayNote
import com.example.fitnesstracker.data.entities.FoodProduct
import com.example.fitnesstracker.data.entities.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtrasDao {
    @Query("SELECT * FROM weight_records ORDER BY date ASC")
    fun getWeightHistory(): Flow<List<WeightRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(record: WeightRecord): Long

    @Delete
    suspend fun deleteWeight(record: WeightRecord)

    @Update
    suspend fun updateWeight(record: WeightRecord): Int

    @Query("SELECT * FROM weight_records ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun latestWeight(): WeightRecord?

    @Query("UPDATE user_profile SET weightKg = :kg WHERE id = 1")
    suspend fun syncProfileWeight(kg: Float)

    @Transaction
    suspend fun editWeightAndSync(record: WeightRecord): Boolean {
        val wasLatest = latestWeight()?.id == record.id
        if (updateWeight(record) != 1) return false
        if (wasLatest) latestWeight()?.let { syncProfileWeight(it.weightKg) }
        return true
    }

    @Transaction
    suspend fun deleteWeightAndSync(record: WeightRecord) {
        val wasLatest = latestWeight()?.id == record.id
        deleteWeight(record)
        if (wasLatest) latestWeight()?.let { syncProfileWeight(it.weightKg) }
        // An empty journal does not invent a replacement for the profile value.
    }

    @Query("DELETE FROM weight_records")
    suspend fun clearWeights()

    @Query("SELECT * FROM day_notes WHERE dayStart = :dayStart")
    fun getNote(dayStart: Long): Flow<DayNote?>

    @Query("SELECT * FROM day_notes ORDER BY dayStart DESC")
    fun getAllNotes(): Flow<List<DayNote>>

    @Query("SELECT * FROM day_notes ORDER BY dayStart ASC")
    suspend fun getAllNotesOnce(): List<DayNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: DayNote)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteIfMissing(note: DayNote)

    @Query("UPDATE day_notes SET text = :text WHERE dayStart = :dayStart")
    suspend fun updateNoteText(dayStart: Long, text: String)

    @Transaction
    suspend fun saveNoteText(dayStart: Long, text: String) {
        insertNoteIfMissing(DayNote(dayStart = dayStart, text = text))
        updateNoteText(dayStart, text)
    }

    @Query("UPDATE day_notes SET isPinned = NOT isPinned WHERE dayStart = :dayStart")
    suspend fun toggleNotePin(dayStart: Long)

    @Query("DELETE FROM day_notes WHERE dayStart = :dayStart")
    suspend fun deleteNote(dayStart: Long)

    @Query("DELETE FROM day_notes")
    suspend fun clearNotes()

    @Query("SELECT * FROM food_products ORDER BY name ASC")
    fun getProducts(): Flow<List<FoodProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: FoodProduct): Long

    @Delete
    suspend fun deleteProduct(product: FoodProduct)

    @Query("DELETE FROM food_products")
    suspend fun clearProducts()
}
