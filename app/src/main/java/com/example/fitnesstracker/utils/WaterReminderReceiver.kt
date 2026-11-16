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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.example.fitnesstracker.data.database.AppDatabase

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (WaterReminder.isEnabled(context)) {
                    val today = DateUtils.todayStart()
                    val amount = AppDatabase.getInstance(context).waterDao()
                        .getForDay(today, DateUtils.dayEnd(today)).let { flow -> kotlinx.coroutines.withTimeout(7000L) { flow.first() } }.sumOf { it.amountMl }
                    val reached = amount >= GoalPreferences.getWaterGoal(context)
                    if (!reached && !WaterReminder.isQuietNow(context) && WaterReminder.isDue(context)) runCatching { showNotification(context, amount, GoalPreferences.getWaterGoal(context)) }
                    WaterReminder.scheduleNext(context, goalReached = reached)
                }
            } catch (_: Exception) {
                if (WaterReminder.isEnabled(context)) WaterReminder.scheduleNext(context)
            } finally { pending.finish() }
        }
    }

    private fun showNotification(ctx: Context, amount: Int, goal: Int) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val l = LanguagePreferences.localizedContext(ctx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(WaterReminder.CHANNEL_ID, l.getString(R.string.notif_water_title),
                NotificationManager.IMPORTANCE_DEFAULT).apply { description = l.getString(R.string.b7_notif_water_channel_desc) }
            nm.createNotificationChannel(channel)
            LeafNotify.organize(ctx)
        }
        val safeGoal = goal.coerceAtLeast(1)
        val left = (safeGoal - amount).coerceAtLeast(0)
        val volume = WaterPreferences.getDefaultVolume(ctx)
        val sinceLast = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE).getLong("last_water_action", 0L)
            .takeIf { it > 0 }?.let { ((System.currentTimeMillis() - it) / 60000L).toInt() }
        val details = buildList {
            add(l.getString(R.string.b7_notif_water_left, LeafNotify.number(ctx, left), (amount * 100 / safeGoal).coerceIn(0, 100)))
            if (sinceLast != null && sinceLast in 1 until 24 * 60) add(l.getString(R.string.b7_notif_water_since, sinceLast / 60, sinceLast % 60))
        }.joinToString("\n")
        val notification = LeafNotify.styled(NotificationCompat.Builder(ctx, WaterReminder.CHANNEL_ID),
                l.getString(R.string.notif_water_title),
                l.getString(R.string.b7_notif_water_progress, LeafNotify.number(ctx, amount), LeafNotify.number(ctx, safeGoal)),
                details)
            .setSmallIcon(R.drawable.ms_water_drop)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(LeafNotify.open(ctx, "water", 1001))
            .addAction(R.drawable.ms_water_drop, l.getString(R.string.b7_notif_water_add, volume),
                WaterNotificationActionReceiver.intent(ctx, WaterNotificationActionReceiver.ACTION_ADD, volume))
            .addAction(0, l.getString(R.string.b7_notif_snooze_hour),
                WaterNotificationActionReceiver.intent(ctx, WaterNotificationActionReceiver.ACTION_SNOOZE))
            .setAutoCancel(true)
        val glasses = (left + volume - 1) / volume.coerceAtLeast(1)
        val card = LeafNotify.progressCard(ctx, notification,
            headline = l.getString(R.string.b8_notif_water_headline),
            value = LeafNotify.number(ctx, amount) + " " + l.getString(R.string.unit_ml),
            caption = l.getString(R.string.b8_notif_of_goal, LeafNotify.number(ctx, safeGoal)),
            progress = amount, goal = safeGoal,
            stats = listOfNotNull(l.getString(R.string.b8_notif_left, LeafNotify.number(ctx, left) + " " + l.getString(R.string.unit_ml)),
                l.getString(R.string.b8_notif_glasses, glasses, volume),
                sinceLast?.takeIf { it in 1 until 24 * 60 }?.let { l.getString(R.string.b8_notif_ago, it / 60, it % 60) }),
            tip = l.getString(R.string.b8_notif_water_tip), tracker = R.drawable.ms_water_drop)
        nm.notify(WaterReminder.NOTIFICATION_ID, card)
    }
}

object WaterReminder {
    const val CHANNEL_ID     = "water_reminder_channel"
    const val NOTIFICATION_ID = 1001
    private const val REQUEST_CODE = 100

    fun scheduleNext(context: Context, hoursLater: Int = 2, goalReached: Boolean = false) {
        if (!isEnabled(context)) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val next = Calendar.getInstance().apply {
            if (goalReached) {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            } else {
                val now = System.currentTimeMillis()
                val afterDrink = lastDrink(context) + hoursLater * 3600000L
                timeInMillis = if (afterDrink > now) afterDrink else now + hoursLater * 3600000L
                if (quietHours(context) && (get(Calendar.HOUR_OF_DAY) >= 22 || get(Calendar.HOUR_OF_DAY) < 8)) {
                    if (get(Calendar.HOUR_OF_DAY) >= 22) add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                }
            }
        }
        val triggerAt = next.timeInMillis

        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
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

    fun quietHours(context: Context): Boolean =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("water_quiet_hours", true)

    fun setQuietHours(context: Context, enabled: Boolean) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putBoolean("water_quiet_hours", enabled).apply()
        if (isEnabled(context)) scheduleNext(context)
    }

    fun isQuietNow(context: Context): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return quietHours(context) && (hour >= 22 || hour < 8)
    }

    private fun lastDrink(context: Context): Long = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getLong("last_water_action", 0L).coerceAtMost(System.currentTimeMillis())

    fun isDue(context: Context): Boolean = System.currentTimeMillis() - lastDrink(context) >= 2L * 3600000L

    fun onWaterChanged(context: Context, amount: Int, goal: Int) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putLong("last_water_action", System.currentTimeMillis()).apply()
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        if (isEnabled(context)) scheduleNext(context, goalReached = amount >= goal.coerceAtLeast(1))
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
