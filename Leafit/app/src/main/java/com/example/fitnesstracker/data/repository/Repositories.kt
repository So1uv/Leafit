package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.dao.*
import com.example.fitnesstracker.data.entities.*
import com.example.fitnesstracker.utils.DateUtils
import kotlinx.coroutines.flow.Flow

/** Репозиторій профілю */
class UserRepository(private val dao: UserProfileDao) {
    fun getProfile(): Flow<UserProfile?> = dao.getProfile()
    suspend fun save(profile: UserProfile) = dao.upsert(profile)
    suspend fun clear() = dao.clear()
}

/** Репозиторій тренувань */
class WorkoutRepository(private val dao: WorkoutDao) {
    fun getAll(): Flow<List<Workout>> = dao.getAll()
    fun getTodayWorkouts(): Flow<List<Workout>> =
        dao.getForDay(DateUtils.todayStart(), DateUtils.todayEnd())
    suspend fun insert(workout: Workout): Long = dao.insert(workout)
    suspend fun delete(workout: Workout) = dao.delete(workout)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun count(): Int = dao.count()
    fun totalCaloriesThisWeek(): Flow<Float?> = dao.totalCaloriesThisWeek(DateUtils.weekStart())
    fun workoutDaysThisWeek(): Flow<List<Long>> = dao.workoutDaysThisWeek(DateUtils.weekStart())
    fun workoutsThisWeek(): Flow<List<Workout>> = dao.workoutsThisWeek(DateUtils.weekStart())
}

/** Репозиторій харчування */
class MealRepository(private val dao: MealDao) {
    fun getMealsForDay(dayTs: Long): Flow<List<Meal>> =
        dao.getMealsForDay(DateUtils.dayStart(dayTs), DateUtils.dayEnd(dayTs))
    suspend fun insert(meal: Meal): Long = dao.insert(meal)
    suspend fun delete(meal: Meal) = dao.delete(meal)
    suspend fun deleteAll() = dao.deleteAll()
    fun getAvailableDates(): Flow<List<Long>> = dao.getAvailableDates()
}

/** Репозиторій сну */
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

/** Репозиторій води */
class WaterRepository(private val dao: WaterDao) {
    suspend fun removeLastToday(): Boolean {
        val today = com.example.fitnesstracker.utils.DateUtils.todayStart()
        val tom   = com.example.fitnesstracker.utils.DateUtils.todayEnd()
        val last  = dao.getLatestToday(today, tom) ?: return false
        dao.delete(last); return true
    }
    fun getForDay(dayTs: Long): Flow<List<WaterRecord>> =
        dao.getForDay(DateUtils.dayStart(dayTs), DateUtils.dayEnd(dayTs))
    fun totalMlToday(): Flow<Int?> =
        dao.totalMlForDay(DateUtils.todayStart(), DateUtils.todayEnd())
    suspend fun insert(record: WaterRecord): Long = dao.insert(record)
    suspend fun delete(record: WaterRecord) = dao.delete(record)
    suspend fun deleteAll() = dao.deleteAll()
}

/** Репозиторій досягнень */
class AchievementRepository(private val dao: AchievementDao) {
    fun getAll(): Flow<List<Achievement>> = dao.getAll()
    fun getUnlocked(): Flow<List<Achievement>> = dao.getUnlocked()
    suspend fun unlock(id: String) = dao.unlock(id)
    suspend fun resetAll() = dao.resetAll()
    suspend fun ensureDefaults(defaults: List<Achievement>) {
        if (dao.count() == 0) dao.insertAll(defaults)
    }
}
