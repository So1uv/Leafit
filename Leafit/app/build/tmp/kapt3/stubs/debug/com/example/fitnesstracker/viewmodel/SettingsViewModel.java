package com.example.fitnesstracker.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.fitnesstracker.data.database.AppDatabase;
import com.example.fitnesstracker.data.entities.Achievement;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.data.repository.*;
import kotlinx.coroutines.flow.*;

/**
 * ViewModel налаштувань / профілю.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\u000e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0010\u001a\u00020\u0012J\u000e\u0010 \u001a\u00020\u001f2\u0006\u0010!\u001a\u00020\"J\u0006\u0010#\u001a\u00020\u001fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00160\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0014R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0014R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0014\u00a8\u0006$"}, d2 = {"Lcom/example/fitnesstracker/viewmodel/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "workoutRepo", "Lcom/example/fitnesstracker/data/repository/WorkoutRepository;", "mealRepo", "Lcom/example/fitnesstracker/data/repository/MealRepository;", "sleepRepo", "Lcom/example/fitnesstracker/data/repository/SleepRepository;", "waterRepo", "Lcom/example/fitnesstracker/data/repository/WaterRepository;", "<init>", "(Lcom/example/fitnesstracker/data/repository/UserRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;Lcom/example/fitnesstracker/data/repository/WorkoutRepository;Lcom/example/fitnesstracker/data/repository/MealRepository;Lcom/example/fitnesstracker/data/repository/SleepRepository;Lcom/example/fitnesstracker/data/repository/WaterRepository;)V", "profile", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "getProfile", "()Lkotlinx/coroutines/flow/StateFlow;", "achievements", "", "Lcom/example/fitnesstracker/data/entities/Achievement;", "getAchievements", "workoutCount", "", "getWorkoutCount", "sleepCount", "getSleepCount", "saveProfile", "", "updateAvatar", "uri", "", "resetAllData", "app_debug"})
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.UserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.MealRepository mealRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.SleepRepository sleepRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.WaterRepository waterRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> profile = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> achievements = null;
    
    /**
     * Лічильники для статистики профілю
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> workoutCount = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> sleepCount = null;
    
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.UserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.MealRepository mealRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.SleepRepository sleepRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.WaterRepository waterRepo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> getProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> getAchievements() {
        return null;
    }
    
    /**
     * Лічильники для статистики профілю
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getWorkoutCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSleepCount() {
        return null;
    }
    
    public final void saveProfile(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.UserProfile profile) {
    }
    
    public final void updateAvatar(@org.jetbrains.annotations.NotNull()
    java.lang.String uri) {
    }
    
    /**
     * Повне скидання: всі таблиці + профіль + досягнення скидаються до початкового стану
     */
    public final void resetAllData() {
    }
}