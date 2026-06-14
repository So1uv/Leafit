package com.example.fitnesstracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.fitnesstracker.data.database.AppDatabase;
import com.example.fitnesstracker.data.repository.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J%\u0010\'\u001a\u0002H(\"\b\b\u0000\u0010(*\u00020)2\f\u0010*\u001a\b\u0012\u0004\u0012\u0002H(0+H\u0016\u00a2\u0006\u0002\u0010,R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\b\u001a\u00020\t8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\f\u0010\r\u001a\u0004\b\n\u0010\u000bR\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\r\u001a\u0004\b\u0010\u0010\u0011R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\r\u001a\u0004\b\u0015\u0010\u0016R\u001b\u0010\u0018\u001a\u00020\u00198BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\r\u001a\u0004\b\u001a\u0010\u001bR\u001b\u0010\u001d\u001a\u00020\u001e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b!\u0010\r\u001a\u0004\b\u001f\u0010 R\u001b\u0010\"\u001a\u00020#8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b&\u0010\r\u001a\u0004\b$\u0010%\u00a8\u0006-"}, d2 = {"Lcom/example/fitnesstracker/viewmodel/ViewModelFactory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "application", "Landroid/app/Application;", "db", "Lcom/example/fitnesstracker/data/database/AppDatabase;", "<init>", "(Landroid/app/Application;Lcom/example/fitnesstracker/data/database/AppDatabase;)V", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "getUserRepo", "()Lcom/example/fitnesstracker/data/repository/UserRepository;", "userRepo$delegate", "Lkotlin/Lazy;", "workoutRepo", "Lcom/example/fitnesstracker/data/repository/WorkoutRepository;", "getWorkoutRepo", "()Lcom/example/fitnesstracker/data/repository/WorkoutRepository;", "workoutRepo$delegate", "mealRepo", "Lcom/example/fitnesstracker/data/repository/MealRepository;", "getMealRepo", "()Lcom/example/fitnesstracker/data/repository/MealRepository;", "mealRepo$delegate", "sleepRepo", "Lcom/example/fitnesstracker/data/repository/SleepRepository;", "getSleepRepo", "()Lcom/example/fitnesstracker/data/repository/SleepRepository;", "sleepRepo$delegate", "waterRepo", "Lcom/example/fitnesstracker/data/repository/WaterRepository;", "getWaterRepo", "()Lcom/example/fitnesstracker/data/repository/WaterRepository;", "waterRepo$delegate", "achieveRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "getAchieveRepo", "()Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "achieveRepo$delegate", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"})
public final class ViewModelFactory implements androidx.lifecycle.ViewModelProvider.Factory {
    @org.jetbrains.annotations.NotNull()
    private final android.app.Application application = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.database.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy userRepo$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy workoutRepo$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy mealRepo$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy sleepRepo$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy waterRepo$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy achieveRepo$delegate = null;
    
    public ViewModelFactory(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.database.AppDatabase db) {
        super();
    }
    
    private final com.example.fitnesstracker.data.repository.UserRepository getUserRepo() {
        return null;
    }
    
    private final com.example.fitnesstracker.data.repository.WorkoutRepository getWorkoutRepo() {
        return null;
    }
    
    private final com.example.fitnesstracker.data.repository.MealRepository getMealRepo() {
        return null;
    }
    
    private final com.example.fitnesstracker.data.repository.SleepRepository getSleepRepo() {
        return null;
    }
    
    private final com.example.fitnesstracker.data.repository.WaterRepository getWaterRepo() {
        return null;
    }
    
    private final com.example.fitnesstracker.data.repository.AchievementRepository getAchieveRepo() {
        return null;
    }
    
    @java.lang.Override()
    @kotlin.Suppress(names = {"UNCHECKED_CAST"})
    @org.jetbrains.annotations.NotNull()
    public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
    java.lang.Class<T> modelClass) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
    java.lang.Class<T> modelClass, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.viewmodel.CreationExtras extras) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
    kotlin.reflect.KClass<T> modelClass, @org.jetbrains.annotations.NotNull()
    androidx.lifecycle.viewmodel.CreationExtras extras) {
        return null;
    }
}