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

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000\u0014\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\u001a\u0014\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0002\u001a\u0014\u0010\u0005\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0002\u001a\f\u0010\u0006\u001a\u00020\u0001*\u00020\u0002H\u0002\u001a\f\u0010\u0007\u001a\u00020\u0001*\u00020\u0002H\u0002\u00a8\u0006\b"}, d2 = {"safeNavigateTopLevel", "", "Landroidx/navigation/NavController;", "route", "", "safeNavigateOnce", "safeNavigateAfterOnboarding", "safeNavigateToOnboarding", "app_debug"})
public final class MainActivityKt {
    
    private static final void safeNavigateTopLevel(androidx.navigation.NavController $this$safeNavigateTopLevel, java.lang.String route) {
    }
    
    private static final void safeNavigateOnce(androidx.navigation.NavController $this$safeNavigateOnce, java.lang.String route) {
    }
    
    private static final void safeNavigateAfterOnboarding(androidx.navigation.NavController $this$safeNavigateAfterOnboarding) {
    }
    
    private static final void safeNavigateToOnboarding(androidx.navigation.NavController $this$safeNavigateToOnboarding) {
    }
}