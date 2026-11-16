package com.example.fitnesstracker.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.WaterRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.NumberFormat

object LeafNotify {
    const val ACCENT = 0xFF3B674D.toInt()

    fun open(context: Context, action: String?, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (action != null) intent.putExtra("shortcut_action", action)
        return PendingIntent.getActivity(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    const val SLEEP = 0xFF635A76.toInt()
    private const val GROUP_TRACKING = "leaf_tracking"
    private const val GROUP_REMINDERS = "leaf_reminders"
    private const val GROUP_ALERTS = "leaf_alerts"
    private val channelGroups = mapOf(
        "steps_tracking_channel" to GROUP_TRACKING, "workout_timers" to GROUP_TRACKING, "live_workout_location" to GROUP_TRACKING,
        "sleep_tracking" to GROUP_TRACKING, "water_reminder_channel" to GROUP_REMINDERS, "bedtime_channel" to GROUP_REMINDERS,
        "workout_rest_finished" to GROUP_ALERTS, "workout_guide_signals" to GROUP_ALERTS, "sleep_alarm" to GROUP_ALERTS)

    fun organize(context: Context) {
        runCatching {
            val manager = context.getSystemService(NotificationManager::class.java)
            val l = LanguagePreferences.localizedContext(context)
            manager.createNotificationChannelGroups(listOf(
                android.app.NotificationChannelGroup(GROUP_TRACKING, l.getString(R.string.b11_notif_group_tracking)),
                android.app.NotificationChannelGroup(GROUP_REMINDERS, l.getString(R.string.b11_notif_group_reminders)),
                android.app.NotificationChannelGroup(GROUP_ALERTS, l.getString(R.string.b11_notif_group_alerts))))
            channelGroups.forEach { (id, group) ->
                val channel = manager.getNotificationChannel(id)
                if (channel != null && channel.group == null) {
                    channel.group = group
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }

    fun live(builder: NotificationCompat.Builder, chip: String? = null): NotificationCompat.Builder {
        builder.setOngoing(true).setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (android.os.Build.VERSION.SDK_INT >= 36) {
            val extras = android.os.Bundle()
            extras.putBoolean("android.requestPromotedOngoing", true)
            if (!chip.isNullOrBlank()) extras.putString("android.shortCriticalText", chip.take(7))
            builder.addExtras(extras)
        }
        return builder
    }

    fun styled(builder: NotificationCompat.Builder, title: String, text: String, details: String? = null) = builder
        .setColor(ACCENT)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(if (details.isNullOrBlank()) text else "$text\n$details"))

    fun card(context: Context, builder: NotificationCompat.Builder, icon: Int, title: String, value: String, caption: String? = null,
        stats: List<String> = emptyList(), tip: String? = null, progress: Int = -1, goal: Int = 0, clockBase: Long = 0L,
        countDown: Boolean = false, sleep: Boolean = false): android.app.Notification {
        val pkg = context.packageName
        val hasProgress = progress >= 0 && goal > 0
        val safeGoal = goal.coerceAtLeast(1)
        val percent = if (hasProgress) (progress.toLong() * 100 / safeGoal).coerceIn(0, 999).toInt() else 0
        val gone = android.view.View.GONE
        val visible = android.view.View.VISIBLE
        fun fill(views: android.widget.RemoteViews, big: Boolean) = views.apply {
            setImageViewResource(R.id.notif_icon, icon)
            if (sleep) {
                setInt(R.id.notif_icon, "setBackgroundResource", R.drawable.notif_plate_sleep)
                setInt(R.id.notif_icon, "setColorFilter", context.getColor(R.color.notif_sleep))
            }
            setTextViewText(R.id.notif_title, title)
            if (clockBase > 0L) {
                setChronometerCountDown(R.id.notif_clock, countDown)
                setChronometer(R.id.notif_clock, clockBase, null, true)
                setViewVisibility(R.id.notif_clock, visible)
                setViewVisibility(R.id.notif_value, gone)
            } else {
                setTextViewText(R.id.notif_value, value)
                setViewVisibility(R.id.notif_clock, gone)
                setViewVisibility(R.id.notif_value, visible)
            }
            setTextViewText(R.id.notif_caption, caption.orEmpty())
            setViewVisibility(R.id.notif_caption, if (caption.isNullOrBlank()) gone else visible)
            setViewVisibility(R.id.notif_bar, if (hasProgress) visible else gone)
            setViewVisibility(R.id.notif_percent, if (hasProgress) visible else gone)
            if (hasProgress) {
                setProgressBar(R.id.notif_bar, safeGoal, progress.coerceIn(0, safeGoal), false)
                setTextViewText(R.id.notif_percent, "$percent%")
            }
            if (big) {
                listOf(R.id.notif_stat1, R.id.notif_stat2, R.id.notif_stat3).forEachIndexed { index, id ->
                    val stat = stats.getOrNull(index)
                    setViewVisibility(id, if (stat != null) visible else gone)
                    if (stat != null) setTextViewText(id, stat)
                }
                setViewVisibility(R.id.notif_stats, if (stats.isEmpty()) gone else visible)
                setViewVisibility(R.id.notif_tip, if (tip.isNullOrBlank()) gone else visible)
                setTextViewText(R.id.notif_tip, tip.orEmpty())
            }
        }
        return builder.setProgress(0, 0, false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(fill(android.widget.RemoteViews(pkg, R.layout.notif_progress), false))
            .setCustomBigContentView(fill(android.widget.RemoteViews(pkg, R.layout.notif_progress_big), true))
            .build()
    }

    fun progressCard(context: Context, builder: NotificationCompat.Builder, headline: String, value: String, caption: String,
        progress: Int, goal: Int, stats: List<String>, tip: String? = null, tracker: Int = 0): android.app.Notification {
        val reached = goal > 0 && progress >= goal
        val title = if (reached) headline + " · " + LanguagePreferences.localizedContext(context).getString(R.string.b11_notif_goal_reached)
            else headline
        return card(context, builder, if (tracker != 0) tracker else R.drawable.ms_steps, title, value, caption, stats, tip, progress, goal)
    }

    fun number(context: Context, value: Int): String =
        NumberFormat.getIntegerInstance(LanguagePreferences.localizedContext(context).resources.configuration.locales[0]).format(value)
}

class WaterNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        when (intent.action) {
            ACTION_ADD -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val volume = intent.getIntExtra(EXTRA_VOLUME, WaterPreferences.getDefaultVolume(app)).coerceIn(50, 2000)
                        val dao = AppDatabase.getInstance(app).waterDao()
                        dao.insert(WaterRecord(timestamp = System.currentTimeMillis(), amountMl = volume))
                        val today = DateUtils.todayStart()
                        val total = withTimeout(5000) { dao.getForDay(today, DateUtils.dayEnd(today)).first() }.sumOf { it.amountMl }
                        WaterReminder.onWaterChanged(app, total, GoalPreferences.getWaterGoal(app))
                        runCatching { withTimeout(8000) { com.example.fitnesstracker.widgets.LeafWidgets.update(app) } }
                        Haptics.play(app, HapticTexture.CONFIRM)
                    } finally { pending.finish() }
                }
            }
            ACTION_SNOOZE -> {
                app.getSystemService(NotificationManager::class.java).cancel(WaterReminder.NOTIFICATION_ID)
                WaterReminder.scheduleNext(app, hoursLater = 1)
            }
        }
    }

    companion object {
        const val ACTION_ADD = "com.example.fitnesstracker.water.ADD"
        const val ACTION_SNOOZE = "com.example.fitnesstracker.water.SNOOZE"
        const val EXTRA_VOLUME = "volume"

        fun intent(context: Context, action: String, volume: Int = 0): PendingIntent =
            PendingIntent.getBroadcast(context, if (action == ACTION_ADD) 1101 else 1102,
                Intent(context, WaterNotificationActionReceiver::class.java).setAction(action).putExtra(EXTRA_VOLUME, volume),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

internal fun formatClock(minutesOfDay: Int): String =
    "%02d:%02d".format((minutesOfDay / 60) % 24, minutesOfDay % 60)
