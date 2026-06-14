package com.example.fitnesstracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.fitnesstracker.data.entities.*;
import com.example.fitnesstracker.data.repository.*;
import com.example.fitnesstracker.utils.DateUtils;
import com.example.fitnesstracker.utils.StepCounter;
import com.example.fitnesstracker.utils.StepCounterService;
import kotlinx.coroutines.flow.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\u008e\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\"\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\u0006\u00106\u001a\u000207J\u0006\u00108\u001a\u000207J\u000e\u00109\u001a\u0002072\u0006\u0010:\u001a\u00020\u001bJ\u0006\u0010;\u001a\u000207J\b\u0010<\u001a\u000207H\u0014R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n \u0012*\u0004\u0018\u00010\u00110\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0019R\u001d\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001f0\u001e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0019R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\"0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0019R\u0019\u0010$\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010%0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0019R\u001d\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0\u001e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0019R\u001d\u0010*\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0\u001e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u0019R\u001d\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0-0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u0019R\u001d\u0010/\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\"0\u001e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u0019R\u0017\u00101\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u0019R\u0017\u00103\u001a\b\u0012\u0004\u0012\u0002040\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010\u0019\u00a8\u0006="}, d2 = {"Lcom/example/fitnesstracker/viewmodel/HomeViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "waterRepo", "Lcom/example/fitnesstracker/data/repository/WaterRepository;", "workoutRepo", "Lcom/example/fitnesstracker/data/repository/WorkoutRepository;", "sleepRepo", "Lcom/example/fitnesstracker/data/repository/SleepRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "<init>", "(Landroid/app/Application;Lcom/example/fitnesstracker/data/repository/UserRepository;Lcom/example/fitnesstracker/data/repository/WaterRepository;Lcom/example/fitnesstracker/data/repository/WorkoutRepository;Lcom/example/fitnesstracker/data/repository/SleepRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;)V", "appContext", "Landroid/content/Context;", "kotlin.jvm.PlatformType", "stepCounter", "Lcom/example/fitnesstracker/utils/StepCounter;", "profile", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "getProfile", "()Lkotlinx/coroutines/flow/StateFlow;", "totalWaterMl", "", "getTotalWaterMl", "todayWorkouts", "", "Lcom/example/fitnesstracker/data/entities/Workout;", "getTodayWorkouts", "todayCaloriesBurned", "", "getTodayCaloriesBurned", "lastSleep", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "getLastSleep", "unlockedAchievements", "Lcom/example/fitnesstracker/data/entities/Achievement;", "getUnlockedAchievements", "allAchievements", "getAllAchievements", "weekActivityDays", "", "getWeekActivityDays", "weekActivityMinutes", "getWeekActivityMinutes", "steps", "getSteps", "stepsSensorAvailable", "", "getStepsSensorAvailable", "startStepTracking", "", "stopStepTracking", "addWater", "ml", "removeLastWater", "onCleared", "app_debug"})
public final class HomeViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.UserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.WaterRepository waterRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.SleepRepository sleepRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.utils.StepCounter stepCounter = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> profile = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> totalWaterMl = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Workout>> todayWorkouts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> todayCaloriesBurned = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> lastSleep = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> unlockedAchievements = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> allAchievements = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.Integer>> weekActivityDays = null;
    
    /**
     * Хвилини активності по днях тижня (Пн..Нд) для графіка
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Float>> weekActivityMinutes = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> steps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> stepsSensorAvailable = null;
    
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application app, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.UserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.WaterRepository waterRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.SleepRepository sleepRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> getProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTotalWaterMl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Workout>> getTodayWorkouts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getTodayCaloriesBurned() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> getLastSleep() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> getUnlockedAchievements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> getAllAchievements() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.Set<java.lang.Integer>> getWeekActivityDays() {
        return null;
    }
    
    /**
     * Хвилини активності по днях тижня (Пн..Нд) для графіка
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Float>> getWeekActivityMinutes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSteps() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getStepsSensorAvailable() {
        return null;
    }
    
    public final void startStepTracking() {
    }
    
    /**
     * Зупиняємо тільки локальний listener екрана. Foreground service лишається
     * активним, щоб кроки рахувалися після згортання або закриття застосунку.
     */
    public final void stopStepTracking() {
    }
    
    public final void addWater(int ml) {
    }
    
    public final void removeLastWater() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}