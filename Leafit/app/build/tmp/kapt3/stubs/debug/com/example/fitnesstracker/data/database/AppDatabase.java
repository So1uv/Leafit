package com.example.fitnesstracker.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.fitnesstracker.data.dao.AchievementDao;
import com.example.fitnesstracker.data.dao.MealDao;
import com.example.fitnesstracker.data.dao.SleepDao;
import com.example.fitnesstracker.data.dao.UserProfileDao;
import com.example.fitnesstracker.data.dao.WaterDao;
import com.example.fitnesstracker.data.dao.WorkoutDao;
import com.example.fitnesstracker.data.entities.Achievement;
import com.example.fitnesstracker.data.entities.Meal;
import com.example.fitnesstracker.data.entities.SleepRecord;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.data.entities.WaterRecord;
import com.example.fitnesstracker.data.entities.Workout;

/**
 * Головна база даних Room.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00102\u00020\u0001:\u0001\u0010B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&\u00a8\u0006\u0011"}, d2 = {"Lcom/example/fitnesstracker/data/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "<init>", "()V", "userProfileDao", "Lcom/example/fitnesstracker/data/dao/UserProfileDao;", "workoutDao", "Lcom/example/fitnesstracker/data/dao/WorkoutDao;", "mealDao", "Lcom/example/fitnesstracker/data/dao/MealDao;", "sleepDao", "Lcom/example/fitnesstracker/data/dao/SleepDao;", "waterDao", "Lcom/example/fitnesstracker/data/dao/WaterDao;", "achievementDao", "Lcom/example/fitnesstracker/data/dao/AchievementDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.example.fitnesstracker.data.entities.UserProfile.class, com.example.fitnesstracker.data.entities.Workout.class, com.example.fitnesstracker.data.entities.Meal.class, com.example.fitnesstracker.data.entities.SleepRecord.class, com.example.fitnesstracker.data.entities.WaterRecord.class, com.example.fitnesstracker.data.entities.Achievement.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.example.fitnesstracker.data.database.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.example.fitnesstracker.data.entities.Achievement> DEFAULT_ACHIEVEMENTS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitnesstracker.data.database.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.UserProfileDao userProfileDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.WorkoutDao workoutDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.MealDao mealDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.SleepDao sleepDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.WaterDao waterDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.fitnesstracker.data.dao.AchievementDao achievementDao();
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\rR\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u000e"}, d2 = {"Lcom/example/fitnesstracker/data/database/AppDatabase$Companion;", "", "<init>", "()V", "INSTANCE", "Lcom/example/fitnesstracker/data/database/AppDatabase;", "DEFAULT_ACHIEVEMENTS", "", "Lcom/example/fitnesstracker/data/entities/Achievement;", "getDEFAULT_ACHIEVEMENTS", "()Ljava/util/List;", "getInstance", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.example.fitnesstracker.data.entities.Achievement> getDEFAULT_ACHIEVEMENTS() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.fitnesstracker.data.database.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}