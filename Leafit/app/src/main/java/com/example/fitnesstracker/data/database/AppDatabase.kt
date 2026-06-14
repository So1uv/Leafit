package com.example.fitnesstracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnesstracker.data.dao.AchievementDao
import com.example.fitnesstracker.data.dao.MealDao
import com.example.fitnesstracker.data.dao.SleepDao
import com.example.fitnesstracker.data.dao.UserProfileDao
import com.example.fitnesstracker.data.dao.WaterDao
import com.example.fitnesstracker.data.dao.WorkoutDao
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.entities.WaterRecord
import com.example.fitnesstracker.data.entities.Workout

/**
 * Головна база даних Room.
 */
@Database(
    entities = [
        UserProfile::class,
        Workout::class,
        Meal::class,
        SleepRecord::class,
        WaterRecord::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao
    abstract fun sleepDao(): SleepDao
    abstract fun waterDao(): WaterDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_ACHIEVEMENTS = listOf(
            Achievement("first_workout",    "Перше тренування",   "Завершено перше тренування"),
            Achievement("ten_workouts",     "10 тренувань",       "Завершено 10 тренувань"),
            Achievement("water_week",       "Водний тиждень",     "Виконано норму води 7 днів поспіль"),
            Achievement("sleep_week",       "Сонний тиждень",     "Зафіксовано сон 7 днів поспіль"),
            Achievement("calorie_goal",     "Норма калорій",      "Вперше виконано денну норму калорій"),
            Achievement("profile_complete", "Профіль готовий",    "Заповнено всі дані профілю"),
            Achievement("first_meal",       "Перша страва",       "Додано перший запис харчування"),
            Achievement("first_sleep",      "Перший сон",         "Додано перший запис сну")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
