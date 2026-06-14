package com.example.fitnesstracker.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.fitnesstracker.R;
import java.util.Calendar;

/**
 * Менеджер нагадувань про воду через AlarmManager.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u0007J\u000e\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u0011\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/example/fitnesstracker/utils/WaterReminder;", "", "<init>", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "REQUEST_CODE", "scheduleNext", "", "context", "Landroid/content/Context;", "hoursLater", "cancel", "isEnabled", "", "setEnabled", "enabled", "app_debug"})
public final class WaterReminder {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID = "water_reminder_channel";
    public static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_CODE = 100;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fitnesstracker.utils.WaterReminder INSTANCE = null;
    
    private WaterReminder() {
        super();
    }
    
    /**
     * Запланувати наступне нагадування через N годин
     */
    public final void scheduleNext(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int hoursLater) {
    }
    
    /**
     * Скасувати всі нагадування
     */
    public final void cancel(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final boolean isEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enabled) {
    }
}