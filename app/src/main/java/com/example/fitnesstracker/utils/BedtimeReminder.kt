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

object BedtimeReminder {
    const val CHANNEL_ID = "bedtime_channel"
    const val NOTIFICATION_ID = 4202
    private const val PREFS = "bedtime_prefs"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)

    fun getHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("hour", 22)

    fun getMinute(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("minute", 30)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", enabled).apply()
        if (enabled) schedule(context) else cancel(context)
    }

    fun setTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("hour", hour).putInt("minute", minute).apply()
        if (isEnabled(context)) schedule(context)
    }

    fun schedule(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, getHour(context))
            set(Calendar.MINUTE, getMinute(context))
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, cal.timeInMillis,
            AlarmManager.INTERVAL_DAY, pending(context)
        )
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(context))
    }

    fun snooze(context: Context, minutes: Int = 15) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes * 60000L,
            PendingIntent.getBroadcast(context, 4206, Intent(context, BedtimeReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    private fun pending(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 4202,
            Intent(context, BedtimeReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}

class BedtimeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ctx = LanguagePreferences.localizedContext(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (intent.action == ACTION_SNOOZE) {
            nm.cancel(BedtimeReminder.NOTIFICATION_ID)
            BedtimeReminder.snooze(context)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    BedtimeReminder.CHANNEL_ID,
                    ctx.getString(R.string.bedtime_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            LeafNotify.organize(context)
        }
        val goal = GoalPreferences.getSleepGoalMinutes(context).coerceIn(60, 16 * 60)
        val now = java.util.Calendar.getInstance()
        val wake = (now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE) + 15 + goal) % (24 * 60)
        val cycles = ((goal - 15).coerceAtLeast(0) / 90f)
        val notification = LeafNotify.styled(NotificationCompat.Builder(ctx, BedtimeReminder.CHANNEL_ID),
                ctx.getString(R.string.bedtime_notif_title),
                ctx.getString(R.string.b7_notif_bed_text, formatClock(wake), goal / 60, goal % 60),
                ctx.getString(R.string.b7_notif_bed_details, cycles))
            .setSmallIcon(R.drawable.ms_bedtime)
            .setColor(LeafNotify.SLEEP)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(LeafNotify.open(context, "sleep", 4203))
            .addAction(R.drawable.ms_bedtime, ctx.getString(R.string.b7_notif_bed_log), LeafNotify.open(context, "sleep", 4204))
            .addAction(0, ctx.getString(R.string.b11_notif_later), PendingIntent.getBroadcast(context, 4205,
                Intent(context, BedtimeReminderReceiver::class.java).setAction(ACTION_SNOOZE),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .setTimeoutAfter(3 * 3600000L)
            .setAutoCancel(true)
            .let { builder ->
                LeafNotify.card(context, builder, R.drawable.ms_bedtime, ctx.getString(R.string.bedtime_notif_title), formatClock(wake),
                    caption = ctx.getString(R.string.b7_notif_bed_text, formatClock(wake), goal / 60, goal % 60),
                    stats = listOf(ctx.getString(R.string.b7_notif_bed_details, cycles)), sleep = true)
            }
        nm.notify(BedtimeReminder.NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_SNOOZE = "com.example.fitnesstracker.BEDTIME_SNOOZE"
    }
}
