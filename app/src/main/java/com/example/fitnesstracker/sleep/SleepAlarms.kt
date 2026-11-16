package com.example.fitnesstracker.sleep

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class SleepAlarm(
    val id: Int,
    val minutes: Int,
    val days: Int = 0,
    val enabled: Boolean = true,
    val label: String = "",
    val smart: Boolean = true
) {
    fun next(from: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, minutes / 60); set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= from) calendar.add(Calendar.DAY_OF_MONTH, 1)
        if (days == 0) return calendar.timeInMillis
        repeat(7) {
            val index = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            if (days and (1 shl index) != 0) return calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }
}

object SleepAlarms {
    private const val FILE = "sleep_alarms"
    private const val SNOOZE_ID = -1
    const val EXTRA_ID = "alarm_id"
    const val EXTRA_AT = "alarm_at"

    val revision = MutableStateFlow(0)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun list(context: Context): List<SleepAlarm> {
        val p = prefs(context)
        val raw = p.getString("alarms", null)
        if (raw == null) {
            val old = SleepTrackPrefs.load(context)
            val seeded = if (old.alarm) listOf(SleepAlarm(1, old.alarmMinutes)) else emptyList()
            store(context, seeded)
            return seeded
        }
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val o = array.getJSONObject(index)
                SleepAlarm(o.optInt("id"), o.optInt("m").coerceIn(0, 1439), o.optInt("d"), o.optBoolean("on", true),
                    o.optString("l"), o.optBoolean("s", true))
            }.sortedBy { it.minutes }
        }.getOrDefault(emptyList())
    }

    private fun store(context: Context, alarms: List<SleepAlarm>) {
        val array = JSONArray()
        alarms.forEach { a ->
            array.put(JSONObject().put("id", a.id).put("m", a.minutes).put("d", a.days).put("on", a.enabled).put("l", a.label).put("s", a.smart))
        }
        prefs(context).edit().putString("alarms", array.toString()).apply()
    }

    fun save(context: Context, alarms: List<SleepAlarm>) {
        store(context, alarms)
        prefs(context).edit().putLong("handledUntil", 0L).apply()
        schedule(context)
        revision.value++
    }

    fun upsert(context: Context, alarm: SleepAlarm) {
        val current = list(context)
        val id = if (alarm.id > 0) alarm.id else (current.maxOfOrNull { it.id } ?: 0) + 1
        save(context, current.filter { it.id != id } + alarm.copy(id = id))
    }

    fun remove(context: Context, id: Int) = save(context, list(context).filter { it.id != id })

    fun snoozeAt(context: Context): Long = prefs(context).getLong("snoozeAt", 0L)

    fun setSnooze(context: Context, at: Long) {
        prefs(context).edit().putLong("snoozeAt", at).apply()
        schedule(context)
        revision.value++
    }

    fun upcoming(context: Context, from: Long = System.currentTimeMillis()): Pair<SleepAlarm, Long>? {
        val p = prefs(context)
        val base = maxOf(from, p.getLong("handledUntil", 0L))
        val regular = list(context).filter { it.enabled }.map { it to it.next(base) }.minByOrNull { it.second }
        val snooze = p.getLong("snoozeAt", 0L)
        if (snooze > from && (regular == null || snooze < regular.second)) return SleepAlarm(SNOOZE_ID, 0) to snooze
        return regular
    }

    fun consume(context: Context, id: Int, nominal: Long) {
        val p = prefs(context)
        if (id == SNOOZE_ID) p.edit().putLong("snoozeAt", 0L).apply()
        else {
            val alarms = list(context)
            val fired = alarms.firstOrNull { it.id == id }
            if (fired != null && fired.days == 0) store(context, alarms.map { if (it.id == id) it.copy(enabled = false) else it })
            p.edit().putLong("handledUntil", maxOf(nominal, System.currentTimeMillis())).putLong("snoozeAt", 0L).apply()
        }
        schedule(context)
        revision.value++
    }

    fun canScheduleExact(context: Context): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms()
    }

    fun schedule(context: Context) {
        val app = context.applicationContext
        val manager = app.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(app, 4400, Intent(app, SleepAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        runCatching { manager.cancel(pending) }
        val next = upcoming(app) ?: return
        val fire = PendingIntent.getBroadcast(app, 4400, Intent(app, SleepAlarmReceiver::class.java)
            .putExtra(EXTRA_ID, next.first.id).putExtra(EXTRA_AT, next.second),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getActivity(app, 4401, Intent(app, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("shortcut_action", "sleep"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (canScheduleExact(app)) manager.setAlarmClock(AlarmManager.AlarmClockInfo(next.second, show), fire)
            else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.second, fire)
        } catch (_: SecurityException) {
            runCatching { manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.second, fire) }
        }
    }
}

class SleepAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val id = intent.getIntExtra(SleepAlarms.EXTRA_ID, 0)
        val at = intent.getLongExtra(SleepAlarms.EXTRA_AT, System.currentTimeMillis())
        val label = SleepAlarms.list(app).firstOrNull { it.id == id }?.label.orEmpty()
        SleepAlarms.consume(app, id, at)
        runCatching {
            ContextCompat.startForegroundService(app, Intent(app, SleepTrackingService::class.java)
                .setAction(SleepTracker.ACTION_RING).putExtra(SleepAlarms.EXTRA_ID, id).putExtra("alarm_label", label))
        }
    }
}
