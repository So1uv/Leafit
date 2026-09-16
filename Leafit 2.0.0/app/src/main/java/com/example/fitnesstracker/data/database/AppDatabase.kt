package com.example.fitnesstracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fitnesstracker.data.dao.AchievementDao
import com.example.fitnesstracker.data.dao.MealDao
import com.example.fitnesstracker.data.dao.SleepDao
import com.example.fitnesstracker.data.dao.StepDao
import com.example.fitnesstracker.data.dao.UserProfileDao
import com.example.fitnesstracker.data.dao.WaterDao
import com.example.fitnesstracker.data.dao.WorkoutDao
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.data.entities.SavedMeal
import com.example.fitnesstracker.data.entities.WeightRecord
import com.example.fitnesstracker.data.entities.DayNote
import com.example.fitnesstracker.data.entities.FoodProduct
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.data.entities.StepRecord
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.entities.WaterRecord
import com.example.fitnesstracker.data.entities.Workout

@Database(
    entities = [
        UserProfile::class,
        Workout::class,
        Meal::class,
        SavedMeal::class,
        SleepRecord::class,
        WaterRecord::class,
        Achievement::class,
        StepRecord::class,
        WeightRecord::class,
        DayNote::class,
        FoodProduct::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun mealDao(): MealDao
    abstract fun sleepDao(): SleepDao
    abstract fun waterDao(): WaterDao
    abstract fun achievementDao(): AchievementDao
    abstract fun stepDao(): StepDao
    abstract fun extrasDao(): com.example.fitnesstracker.data.dao.ExtrasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE day_notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        val DEFAULT_ACHIEVEMENTS = listOf(
            Achievement("first_workout",    "ach_first_workout_title",   "ach_first_workout_desc"),
            Achievement("ten_workouts",     "ach_ten_workouts_title",    "ach_ten_workouts_desc"),
            Achievement("water_week",       "ach_water_week_title",      "ach_water_week_desc"),
            Achievement("sleep_week",       "ach_sleep_week_title",      "ach_sleep_week_desc"),
            Achievement("calorie_goal",     "ach_calorie_goal_title",    "ach_calorie_goal_desc"),
            Achievement("profile_complete", "ach_profile_complete_title","ach_profile_complete_desc"),
            Achievement("first_meal",       "ach_first_meal_title",      "ach_first_meal_desc"),
            Achievement("first_sleep",      "ach_first_sleep_title",     "ach_first_sleep_desc"),
            Achievement("sleep_three", "ach_sleep_three_title", "ach_sleep_three_desc"),
            Achievement("sleep_fourteen", "ach_sleep_fourteen_title", "ach_sleep_fourteen_desc"),
            Achievement("sleep_thirty", "ach_sleep_thirty_title", "ach_sleep_thirty_desc"),
            Achievement("sleep_sixty", "ach_sleep_sixty_title", "ach_sleep_sixty_desc"),
            Achievement("sleep_context", "ach_sleep_context_title", "ach_sleep_context_desc"),
            Achievement("note_first", "ach_note_first_title", "ach_note_first_desc"),
            Achievement("note_seven", "ach_note_seven_title", "ach_note_seven_desc"),
            Achievement("note_fourteen", "ach_note_fourteen_title", "ach_note_fourteen_desc"),
            Achievement("note_thirty", "ach_note_thirty_title", "ach_note_thirty_desc"),
            Achievement("note_pinned", "ach_note_pinned_title", "ach_note_pinned_desc")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker.db"
                ).addMigrations(MIGRATION_5_6).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
