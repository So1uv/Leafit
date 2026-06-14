package com.example.fitnesstracker.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import kotlinx.coroutines.flow.StateFlow;
import java.util.Calendar;

/**
 * Лічильник кроків.
 *
 * TYPE_STEP_COUNTER повертає загальну кількість кроків з моменту останнього
 * перезавантаження пристрою. Ми зберігаємо денну базу і рахуємо різницю.
 * Поточне значення також кладеться в SharedPreferences, щоб UI бачив кроки,
 * які оновив foreground service, навіть якщо екран був закритий.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\b\u0018\u0000 -2\u00020\u00012\u00020\u0002:\u0001-B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0004\b\u0005\u0010\u0006J\u0006\u0010\u001b\u001a\u00020\u0010J\u0006\u0010\u001c\u001a\u00020\u001dJ\u0012\u0010\u001e\u001a\u00020\u001d2\b\u0010\u001f\u001a\u0004\u0018\u00010 H\u0016J\u001a\u0010!\u001a\u00020\u001d2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\"\u001a\u00020\u0013H\u0016J\u001c\u0010#\u001a\u00020\u001d2\b\u0010$\u001a\u0004\u0018\u00010\u000e2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016J\u0006\u0010\'\u001a\u00020\u001dJ\u0010\u0010(\u001a\u00020\u001d2\u0006\u0010)\u001a\u00020\u0013H\u0002J\b\u0010*\u001a\u00020\u001dH\u0002J\b\u0010+\u001a\u00020\u0013H\u0002J\b\u0010,\u001a\u00020&H\u0002R\u0016\u0010\u0007\u001a\n \b*\u0004\u0018\u00010\u00040\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00100\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00100\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0017\u00a8\u0006."}, d2 = {"Lcom/example/fitnesstracker/utils/StepCounter;", "Landroid/hardware/SensorEventListener;", "Landroid/content/SharedPreferences$OnSharedPreferenceChangeListener;", "context", "Landroid/content/Context;", "<init>", "(Landroid/content/Context;)V", "appContext", "kotlin.jvm.PlatformType", "sensorManager", "Landroid/hardware/SensorManager;", "sensor", "Landroid/hardware/Sensor;", "prefs", "Landroid/content/SharedPreferences;", "listening", "", "_stepsToday", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "stepsToday", "Lkotlinx/coroutines/flow/StateFlow;", "getStepsToday", "()Lkotlinx/coroutines/flow/StateFlow;", "_available", "available", "getAvailable", "start", "stop", "", "onSensorChanged", "event", "Landroid/hardware/SensorEvent;", "onAccuracyChanged", "accuracy", "onSharedPreferenceChanged", "sharedPreferences", "key", "", "release", "updateFromTotalSinceBoot", "totalSinceBoot", "ensureDayIsFresh", "readSteps", "getDayKey", "Companion", "app_debug"})
public final class StepCounter implements android.hardware.SensorEventListener, android.content.SharedPreferences.OnSharedPreferenceChangeListener {
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull()
    private final android.hardware.SensorManager sensorManager = null;
    @org.jetbrains.annotations.Nullable()
    private final android.hardware.Sensor sensor = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    private boolean listening = false;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _stepsToday = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> stepsToday = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _available = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> available = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREFS = "steps_prefs";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_DAY = "base_day";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_BASE_STEPS = "base_steps";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_CURRENT_STEPS = "current_steps";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitnesstracker.utils.StepCounter.Companion Companion = null;
    
    public StepCounter(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getStepsToday() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getAvailable() {
        return null;
    }
    
    public final boolean start() {
        return false;
    }
    
    public final void stop() {
    }
    
    @java.lang.Override()
    public void onSensorChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.SensorEvent event) {
    }
    
    @java.lang.Override()
    public void onAccuracyChanged(@org.jetbrains.annotations.Nullable()
    android.hardware.Sensor sensor, int accuracy) {
    }
    
    @java.lang.Override()
    public void onSharedPreferenceChanged(@org.jetbrains.annotations.Nullable()
    android.content.SharedPreferences sharedPreferences, @org.jetbrains.annotations.Nullable()
    java.lang.String key) {
    }
    
    public final void release() {
    }
    
    private final void updateFromTotalSinceBoot(int totalSinceBoot) {
    }
    
    private final void ensureDayIsFresh() {
    }
    
    private final int readSteps() {
        return 0;
    }
    
    private final java.lang.String getDayKey() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/fitnesstracker/utils/StepCounter$Companion;", "", "<init>", "()V", "PREFS", "", "KEY_DAY", "KEY_BASE_STEPS", "KEY_CURRENT_STEPS", "hasActivityRecognitionPermission", "", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean hasActivityRecognitionPermission(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return false;
        }
    }
}