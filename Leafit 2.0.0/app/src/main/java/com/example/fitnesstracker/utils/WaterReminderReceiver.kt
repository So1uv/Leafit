package com.example.fitnesstracker.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesstracker.R
import java.util.Calendar

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        showNotification(context)

        WaterReminder.scheduleNext(context, hoursLater = 2)
    }

    private fun showNotification(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WaterReminder.CHANNEL_ID,
                LanguagePreferences.localizedContext(ctx).getString(R.string.notif_steps_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = LanguagePreferences.localizedContext(ctx).getString(R.string.notif_water_title) }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(ctx, WaterReminder.CHANNEL_ID)
            .setSmallIcon(com.example.fitnesstracker.R.drawable.ms_water_drop)
            .setContentTitle(LanguagePreferences.localizedContext(ctx).getString(R.string.notif_water_title))
            .setContentText(LanguagePreferences.localizedContext(ctx).getString(R.string.notif_water_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(WaterReminder.NOTIFICATION_ID, notification)
    }
}

object WaterReminder {
    const val CHANNEL_ID     = "water_reminder_channel"
    const val NOTIFICATION_ID = 1001
    private const val REQUEST_CODE = 100

    fun scheduleNext(context: Context, hoursLater: Int = 2) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, hoursLater)
        }.timeInMillis

        try {

            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (e: SecurityException) {

            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("water_reminders", false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putBoolean("water_reminders", enabled).apply()
        if (enabled) scheduleNext(context) else cancel(context)
    }
}
