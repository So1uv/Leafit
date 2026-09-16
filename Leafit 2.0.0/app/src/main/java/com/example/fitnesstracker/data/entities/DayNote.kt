package com.example.fitnesstracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_notes")
data class DayNote(
    @PrimaryKey val dayStart: Long,
    val text: String = "",
    @androidx.room.ColumnInfo(defaultValue = "0") val isPinned: Boolean = false
)
