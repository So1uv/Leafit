@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.dao.*
import com.example.fitnesstracker.data.entities.*
import com.example.fitnesstracker.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import com.example.fitnesstracker.utils.currentDayFlow

class UserRepository(private val dao: UserProfileDao) {
    fun getProfile(): Flow<UserProfile?> = dao.getProfile()
    suspend fun save(profile: UserProfile) = dao.upsert(profile)
    suspend fun clear() = dao.clear()
}

class WorkoutRepository(private val dao: WorkoutDao) {
    fun getAll(): Flow<List<Workout>> = dao.getAll()
    fun getTodayWorkouts(): Flow<List<Workout>> =
        currentDayFlow().flatMapLatest { dao.getForDay(it, DateUtils.dayEnd(it)) }
    suspend fun insert(workout: Workout): Long = dao.insert(workout)
    suspend fun insertSessionOnce(workout: Workout, token: String): Long = dao.insertLiveOnce(workout, token)
    suspend fun hasSession(token: String): Boolean = dao.findLiveSession(token) != null
    suspend fun update(workout: Workout): Int = dao.update(workout)
    suspend fun delete(workout: Workout) = dao.delete(workout)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun count(): Int = dao.count()
    fun totalCaloriesThisWeek(): Flow<Float?> = dao.totalCaloriesThisWeek(DateUtils.weekStart())
    fun workoutDaysThisWeek(): Flow<List<Long>> = dao.workoutDaysThisWeek(DateUtils.weekStart())
    fun workoutsThisWeek(): Flow<List<Workout>> = currentDayFlow().flatMapLatest { dao.workoutsThisWeek(DateUtils.weekStart()) }
}

class MealRepository(private val dao: MealDao) {
    fun getMealsForDay(dayTs: Long): Flow<List<Meal>> =
        dao.getMealsForDay(DateUtils.dayStart(dayTs), DateUtils.dayEnd(dayTs))
    suspend fun insert(meal: Meal): Long = dao.insert(meal)
    suspend fun update(meal: Meal) = dao.update(meal)
    suspend fun delete(meal: Meal) = dao.delete(meal)
    suspend fun deleteAll() = dao.deleteAll()
    fun getAvailableDates(): Flow<List<Long>> = dao.getAvailableDates()

    fun getSavedMeals(): Flow<List<com.example.fitnesstracker.data.entities.SavedMeal>> = dao.getSavedMeals()
    suspend fun insertSaved(meal: com.example.fitnesstracker.data.entities.SavedMeal): Long = dao.insertSaved(meal)
    suspend fun deleteSaved(meal: com.example.fitnesstracker.data.entities.SavedMeal) = dao.deleteSaved(meal)
}

class SleepRepository(private val dao: SleepDao) {
    fun getAll(): Flow<List<SleepRecord>> = dao.getAll()
    fun getForDay(dayTs: Long): Flow<SleepRecord?> =
        dao.getForDay(DateUtils.dayStart(dayTs), DateUtils.dayEnd(dayTs))
    fun getLatest(): Flow<SleepRecord?> = dao.getLatest()
    suspend fun insert(record: SleepRecord): Long = dao.insert(record)
    suspend fun delete(record: SleepRecord) = dao.delete(record)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun count(): Int = dao.count()
}

class WaterRepository(private val dao: WaterDao) {
    suspend fun removeLastForDay(dayTs: Long): Boolean {
        val start = com.example.fitnesstracker.utils.DateUtils.dayStart(dayTs)
        val end = com.example.fitnesstracker.utils.DateUtils.dayEnd(dayTs)
        val last = dao.getLatestToday(start, end) ?: return false
        dao.delete(last)
        return true
    }

    suspend fun removeLastToday(): Boolean = removeLastForDay(com.example.fitnesstracker.utils.DateUtils.todayStart())
    fun getForDay(dayTs: Long): Flow<List<WaterRecord>> =
        dao.getForDay(DateUtils.dayStart(dayTs), DateUtils.dayEnd(dayTs))
    fun totalMlToday(): Flow<Int?> =
        currentDayFlow().flatMapLatest { dao.totalMlForDay(it, DateUtils.dayEnd(it)) }
    suspend fun insert(record: WaterRecord): Long = dao.insert(record)
    suspend fun delete(record: WaterRecord) = dao.delete(record)
    suspend fun deleteAll() = dao.deleteAll()
}

class AchievementRepository(private val dao: AchievementDao) {
    fun getAll(): Flow<List<Achievement>> = dao.getAll()
    fun getUnlocked(): Flow<List<Achievement>> = dao.getUnlocked()
    suspend fun unlock(id: String) = dao.unlock(id)
    suspend fun evaluateSleepJournal(records: List<SleepRecord>) {
        listOf("sleep_three" to 3, "sleep_fourteen" to 14, "sleep_thirty" to 30, "sleep_sixty" to 60)
            .filter { records.size >= it.second }.forEach { unlock(it.first) }
        if (records.any { it.tags.isNotBlank() }) unlock("sleep_context")
    }
    suspend fun evaluateNotes(notes: List<com.example.fitnesstracker.data.entities.DayNote>) {
        val written = notes.filter { it.text.isNotBlank() }
        listOf("note_first" to 1, "note_seven" to 7, "note_fourteen" to 14, "note_thirty" to 30)
            .filter { written.size >= it.second }.forEach { unlock(it.first) }
        if (written.any { it.isPinned }) unlock("note_pinned")
    }
    suspend fun resetAll() = dao.resetAll()
    suspend fun ensureDefaults(defaults: List<Achievement>) {
        dao.insertAll(defaults) // IGNORE preserves existing unlocks while adding new badges.
    }
}

class StepRepository(private val dao: com.example.fitnesstracker.data.dao.StepDao) {
    fun getAll() = dao.getAll()
    fun getSinceWeekStart() = currentDayFlow().flatMapLatest { dao.getSince(DateUtils.weekStart()) }
    fun observeForDay(dayTs: Long) = dao.observeForDay(DateUtils.dayStart(dayTs))
    suspend fun getForDay(dayTs: Long): Int =
        dao.getForDay(DateUtils.dayStart(dayTs))?.steps ?: 0
    suspend fun upsert(dayTs: Long, steps: Int) {
        dao.upsert(com.example.fitnesstracker.data.entities.StepRecord(
            dayStart = DateUtils.dayStart(dayTs), steps = steps
        ))
    }
    suspend fun clear() = dao.clear()
}

class ExtrasRepository(private val dao: com.example.fitnesstracker.data.dao.ExtrasDao) {
    suspend fun saveNoteText(dayStart: Long, text: String) = dao.saveNoteText(dayStart, text)
    suspend fun toggleNotePin(dayStart: Long) = dao.toggleNotePin(dayStart)
    fun getWeightHistory() = dao.getWeightHistory()
    suspend fun insertWeight(r: com.example.fitnesstracker.data.entities.WeightRecord) = dao.insertWeight(r)
    suspend fun deleteWeight(r: com.example.fitnesstracker.data.entities.WeightRecord) = dao.deleteWeightAndSync(r)

    suspend fun updateWeight(r: com.example.fitnesstracker.data.entities.WeightRecord) = dao.editWeightAndSync(r)

    fun getNote(dayStart: Long) = dao.getNote(dayStart)
    fun getAllNotes() = dao.getAllNotes()
    suspend fun upsertNote(n: com.example.fitnesstracker.data.entities.DayNote) = dao.upsertNote(n)
    suspend fun deleteNote(dayStart: Long) = dao.deleteNote(dayStart)

    fun getProducts() = dao.getProducts()
    suspend fun insertProduct(p: com.example.fitnesstracker.data.entities.FoodProduct) = dao.insertProduct(p)
    suspend fun deleteProduct(p: com.example.fitnesstracker.data.entities.FoodProduct) = dao.deleteProduct(p)

    suspend fun clearAll() {
        dao.clearWeights()
        dao.clearNotes()
        dao.clearProducts()
    }
}
