package com.example.fitnesstracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.navigation.NavController;
import com.example.fitnesstracker.data.database.AppDatabase;
import com.example.fitnesstracker.ui.navigation.Screen;
import com.example.fitnesstracker.ui.theme.AppThemeMode;
import com.example.fitnesstracker.ui.theme.ThemePreferences;
import com.example.fitnesstracker.utils.StepCounter;
import com.example.fitnesstracker.viewmodel.HomeViewModel;
import com.example.fitnesstracker.viewmodel.MainViewModel;
import com.example.fitnesstracker.viewmodel.NutritionViewModel;
import com.example.fitnesstracker.viewmodel.SettingsViewModel;
import com.example.fitnesstracker.viewmodel.SleepViewModel;
import com.example.fitnesstracker.viewmodel.ViewModelFactory;
import com.example.fitnesstracker.viewmodel.WorkoutViewModel;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u00101\u001a\u0002022\b\u00103\u001a\u0004\u0018\u000104H\u0014J\b\u00105\u001a\u000202H\u0002J\b\u00106\u001a\u000202H\u0002R\u001b\u0010\u0004\u001a\u00020\u00058BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\t\u001a\u0004\b\f\u0010\rR\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\t\u001a\u0004\b\u0011\u0010\u0012R\u001b\u0010\u0014\u001a\u00020\u00158BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0018\u0010\t\u001a\u0004\b\u0016\u0010\u0017R\u001b\u0010\u0019\u001a\u00020\u001a8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001d\u0010\t\u001a\u0004\b\u001b\u0010\u001cR\u001b\u0010\u001e\u001a\u00020\u001f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\"\u0010\t\u001a\u0004\b \u0010!R\u001b\u0010#\u001a\u00020$8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\'\u0010\t\u001a\u0004\b%\u0010&R\u001b\u0010(\u001a\u00020)8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b,\u0010\t\u001a\u0004\b*\u0010+R\u0014\u0010-\u001a\b\u0012\u0004\u0012\u00020/0.X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u00100\u001a\b\u0012\u0004\u0012\u00020/0.X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/example/fitnesstracker/MainActivity;", "Landroidx/activity/ComponentActivity;", "<init>", "()V", "db", "Lcom/example/fitnesstracker/data/database/AppDatabase;", "getDb", "()Lcom/example/fitnesstracker/data/database/AppDatabase;", "db$delegate", "Lkotlin/Lazy;", "factory", "Lcom/example/fitnesstracker/viewmodel/ViewModelFactory;", "getFactory", "()Lcom/example/fitnesstracker/viewmodel/ViewModelFactory;", "factory$delegate", "mainVm", "Lcom/example/fitnesstracker/viewmodel/MainViewModel;", "getMainVm", "()Lcom/example/fitnesstracker/viewmodel/MainViewModel;", "mainVm$delegate", "homeVm", "Lcom/example/fitnesstracker/viewmodel/HomeViewModel;", "getHomeVm", "()Lcom/example/fitnesstracker/viewmodel/HomeViewModel;", "homeVm$delegate", "workoutVm", "Lcom/example/fitnesstracker/viewmodel/WorkoutViewModel;", "getWorkoutVm", "()Lcom/example/fitnesstracker/viewmodel/WorkoutViewModel;", "workoutVm$delegate", "nutritionVm", "Lcom/example/fitnesstracker/viewmodel/NutritionViewModel;", "getNutritionVm", "()Lcom/example/fitnesstracker/viewmodel/NutritionViewModel;", "nutritionVm$delegate", "sleepVm", "Lcom/example/fitnesstracker/viewmodel/SleepViewModel;", "getSleepVm", "()Lcom/example/fitnesstracker/viewmodel/SleepViewModel;", "sleepVm$delegate", "settingsVm", "Lcom/example/fitnesstracker/viewmodel/SettingsViewModel;", "getSettingsVm", "()Lcom/example/fitnesstracker/viewmodel/SettingsViewModel;", "settingsVm$delegate", "activityRecognitionLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "notificationsLauncher", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "requestStepPermissionIfNeeded", "requestNotificationPermissionIfNeeded", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy factory$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy mainVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy homeVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy workoutVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy nutritionVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy sleepVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy settingsVm$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> activityRecognitionLauncher = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> notificationsLauncher = null;
    
    public MainActivity() {
        super(0);
    }
    
    private final com.example.fitnesstracker.data.database.AppDatabase getDb() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.ViewModelFactory getFactory() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.MainViewModel getMainVm() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.HomeViewModel getHomeVm() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.WorkoutViewModel getWorkoutVm() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.NutritionViewModel getNutritionVm() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.SleepViewModel getSleepVm() {
        return null;
    }
    
    private final com.example.fitnesstracker.viewmodel.SettingsViewModel getSettingsVm() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void requestStepPermissionIfNeeded() {
    }
    
    private final void requestNotificationPermissionIfNeeded() {
    }
}