package com.example.fitnesstracker.sleep

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.SleepRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

data class SleepTrackSettings(
    val sound: Boolean = true,
    val sensitivity: Int = 1,
    val recordings: Boolean = true,
    val maxClips: Int = 20,
    val alarm: Boolean = false,
    val alarmMinutes: Int = 7 * 60,
    val smartWindow: Int = 30,
    val gradual: Boolean = true,
    val vibrate: Boolean = true,
    val snooze: Int = 10,
    val antiSnore: Boolean = false,
    val maxHours: Int = 12,
    val startDelay: Int = 0,
    val clipSeconds: Int = 20,
    val recordFilter: Int = 0,
    val alarmUri: String = "",
    val alarmVolume: Int = 100,
    val rampSeconds: Int = 60,
    val snoozeLimit: Int = 0,
    val mathDismiss: Boolean = false,
    val strongVibration: Boolean = false,
    val keepNights: Int = 30,
    val lullaby: Int = 0,
    val lullabyMinutes: Int = 30,
    val lullabyVolume: Int = 40
)

object SleepTrackPrefs {
    private const val FILE = "sleep_tracking"
    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(context: Context): SleepTrackSettings = prefs(context).let { p ->
        SleepTrackSettings(
            sound = p.getBoolean("sound", true),
            sensitivity = p.getInt("sensitivity", 1).coerceIn(0, 2),
            recordings = p.getBoolean("recordings", true),
            maxClips = p.getInt("maxClips", 20).coerceIn(1, 40),
            alarm = p.getBoolean("alarm", false),
            alarmMinutes = p.getInt("alarmMinutes", 7 * 60).coerceIn(0, 1439),
            smartWindow = p.getInt("smartWindow", 30).coerceIn(0, 90),
            gradual = p.getBoolean("gradual", true),
            vibrate = p.getBoolean("vibrate", true),
            snooze = p.getInt("snooze", 10).coerceIn(1, 30),
            antiSnore = p.getBoolean("antiSnore", false),
            maxHours = p.getInt("maxHours", 12).coerceIn(4, 16),
            startDelay = p.getInt("startDelay", 0).coerceIn(0, 60),
            clipSeconds = p.getInt("clipSeconds", 20).coerceIn(5, 60),
            recordFilter = p.getInt("recordFilter", 0).coerceIn(0, 1),
            alarmUri = p.getString("alarmUri", "") ?: "",
            alarmVolume = p.getInt("alarmVolume", 100).coerceIn(10, 100),
            rampSeconds = p.getInt("rampSeconds", 60).coerceIn(10, 600),
            snoozeLimit = p.getInt("snoozeLimit", 0).coerceIn(0, 10),
            mathDismiss = p.getBoolean("mathDismiss", false),
            strongVibration = p.getBoolean("strongVibration", false),
            keepNights = p.getInt("keepNights", 30).coerceIn(7, 365),
            lullaby = p.getInt("lullaby", 0).coerceIn(0, 3),
            lullabyMinutes = p.getInt("lullabyMinutes", 30).coerceIn(5, 120),
            lullabyVolume = p.getInt("lullabyVolume", 40).coerceIn(5, 100))
    }

    fun save(context: Context, s: SleepTrackSettings) {
        prefs(context).edit()
            .putBoolean("sound", s.sound).putInt("sensitivity", s.sensitivity).putBoolean("recordings", s.recordings)
            .putInt("maxClips", s.maxClips).putBoolean("alarm", s.alarm)
            .putInt("alarmMinutes", s.alarmMinutes).putInt("smartWindow", s.smartWindow).putBoolean("gradual", s.gradual)
            .putBoolean("vibrate", s.vibrate).putInt("snooze", s.snooze).putBoolean("antiSnore", s.antiSnore)
            .putInt("maxHours", s.maxHours).putInt("startDelay", s.startDelay).putInt("clipSeconds", s.clipSeconds)
            .putInt("recordFilter", s.recordFilter).putString("alarmUri", s.alarmUri).putInt("alarmVolume", s.alarmVolume)
            .putInt("rampSeconds", s.rampSeconds).putInt("snoozeLimit", s.snoozeLimit).putBoolean("mathDismiss", s.mathDismiss)
            .putBoolean("strongVibration", s.strongVibration).putInt("keepNights", s.keepNights)
            .putInt("lullaby", s.lullaby).putInt("lullabyMinutes", s.lullabyMinutes).putInt("lullabyVolume", s.lullabyVolume).apply()
    }

    fun activeStart(context: Context): Long = prefs(context).getLong("activeStart", 0L)
    fun setActiveStart(context: Context, start: Long) { prefs(context).edit().putLong("activeStart", start).apply() }
    fun alarmAt(context: Context): Long = prefs(context).getLong("alarmAt", 0L)
    fun setAlarmAt(context: Context, at: Long) { prefs(context).edit().putLong("alarmAt", at).apply() }
}

data class SleepEvent(val at: Long, val seconds: Int, val peak: Int, val type: Int, val clip: String?) {
    companion object {
        const val NOISE = 0
        const val SNORE = 1
        const val TALK = 2
    }
}

data class SleepNight(val start: Long, val end: Long, val noise: List<Int>, val events: List<SleepEvent>) {
    val minutes: Int get() = ((end - start) / 60000L).coerceAtLeast(0L).toInt()
    val averageNoise: Int get() = noise.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.sum() / it.size }
    val snoreSeconds: Int get() = events.filter { it.type == SleepEvent.SNORE }.sumOf { it.seconds }
    val talkSeconds: Int get() = events.filter { it.type == SleepEvent.TALK }.sumOf { it.seconds }
    val loudSeconds: Int get() = events.sumOf { it.seconds }
    val loudest: Int get() = events.maxOfOrNull { it.peak } ?: (noise.maxOrNull() ?: 0)
    val clips: Int get() = events.count { it.clip != null }
    val quietShare: Int get() = if (minutes <= 0) 100 else (100 - loudSeconds * 100 / (minutes * 60).coerceAtLeast(1)).coerceIn(0, 100)

    fun suggestedQuality(): Int {
        val hours = (minutes / 60f).coerceAtLeast(.1f)
        var score = 5f
        if (hours < 6f) score -= 1f
        if (hours < 4.5f) score -= 1f
        score -= (events.size / hours.coerceAtLeast(1f) / 5f).coerceAtMost(2f)
        return score.toInt().coerceIn(1, 5)
    }
}

object SleepNightStore {
    private const val PREFIX = "leafnight:"

    private fun dir(context: Context) = File(context.applicationContext.filesDir, "sleep_nights").apply { mkdirs() }
    fun clipDir(context: Context) = File(dir(context), "clips").apply { mkdirs() }
    private fun file(context: Context, start: Long) = File(dir(context), "night_$start.json")

    fun note(start: Long) = PREFIX + start
    fun startOf(note: String): Long? = note.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)?.toLongOrNull()

    fun save(context: Context, night: SleepNight) {
        val json = JSONObject().put("start", night.start).put("end", night.end)
            .put("noise", JSONArray(night.noise))
            .put("events", JSONArray().apply {
                night.events.forEach { e ->
                    put(JSONObject().put("at", e.at).put("s", e.seconds).put("p", e.peak).put("t", e.type).put("c", e.clip ?: ""))
                }
            })
        val target = file(context, night.start)
        val temp = File(target.parentFile, target.name + ".tmp")
        runCatching {
            temp.writeText(json.toString())
            if (!temp.renameTo(target)) { target.writeText(json.toString()); temp.delete() }
        }
    }

    fun load(context: Context, start: Long): SleepNight? = runCatching {
        val json = JSONObject(file(context, start).readText())
        fun ints(key: String) = json.optJSONArray(key)?.let { a -> List(a.length()) { a.optInt(it) } } ?: emptyList()
        val events = json.optJSONArray("events")?.let { a ->
            List(a.length()) { i ->
                val o = a.getJSONObject(i)
                SleepEvent(o.optLong("at"), o.optInt("s"), o.optInt("p"), o.optInt("t"), o.optString("c").takeIf { it.isNotEmpty() })
            }
        } ?: emptyList()
        SleepNight(json.optLong("start", start), json.optLong("end", start), ints("noise"), events)
    }.getOrNull()

    fun list(context: Context): List<Long> = runCatching {
        dir(context).listFiles { f -> f.name.startsWith("night_") && f.name.endsWith(".json") }
            ?.mapNotNull { it.name.removePrefix("night_").removeSuffix(".json").toLongOrNull() }?.sortedDescending() ?: emptyList()
    }.getOrDefault(emptyList())

    fun delete(context: Context, start: Long) {
        runCatching {
            file(context, start).delete()
            clipDir(context).listFiles()?.forEach { clip -> if (clip.name.startsWith("${start}_")) clip.delete() }
        }
    }

    fun prune(context: Context, keep: Int = 30) {
        runCatching {
            val nights = dir(context).listFiles { f -> f.name.startsWith("night_") && f.name.endsWith(".json") }
                ?.sortedByDescending { it.name } ?: return
            val stale = nights.drop(keep).map { it.name.removePrefix("night_").removeSuffix(".json") }
            nights.drop(keep).forEach { it.delete() }
            clipDir(context).listFiles()?.forEach { clip -> if (stale.any { clip.name.startsWith(it + "_") }) clip.delete() }
        }
    }
}

data class SleepLive(
    val active: Boolean = false,
    val start: Long = 0L,
    val level: Float = 0f,
    val db: Int = 0,
    val events: Int = 0,
    val snores: Int = 0,
    val ringing: Boolean = false,
    val alarmAt: Long = 0L,
    val sound: Boolean = false,
    val canSnooze: Boolean = true,
    val math: Boolean = false,
    val saved: Int = 0,
    val label: String = "",
    val lullaby: Boolean = false
)

object SleepTracker {
    const val ACTION_START = "com.example.fitnesstracker.sleep.START"
    const val ACTION_STOP = "com.example.fitnesstracker.sleep.STOP"
    const val ACTION_SNOOZE = "com.example.fitnesstracker.sleep.SNOOZE"
    const val ACTION_DISCARD = "com.example.fitnesstracker.sleep.DISCARD"
    const val ACTION_RING = "com.example.fitnesstracker.sleep.RING"
    const val ACTION_DISMISS = "com.example.fitnesstracker.sleep.DISMISS"
    const val ACTION_TEST = "com.example.fitnesstracker.sleep.TEST"

    val state = MutableStateFlow(SleepLive())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start(context: Context) {
        val app = context.applicationContext
        ContextCompat.startForegroundService(app, Intent(app, SleepTrackingService::class.java).setAction(ACTION_START))
    }

    fun send(context: Context, action: String) {
        val app = context.applicationContext
        runCatching { app.startService(Intent(app, SleepTrackingService::class.java).setAction(action)) }
    }

    fun finalize(context: Context, night: SleepNight, onDone: () -> Unit = {}) {
        val app = context.applicationContext
        scope.launch {
            runCatching {
                SleepNightStore.save(app, night)
                if (night.end - night.start >= 5000L) {
                    AppDatabase.getInstance(app).sleepDao().insert(SleepRecord(bedTime = night.start, wakeTime = night.end,
                        qualityScore = night.suggestedQuality(), note = SleepNightStore.note(night.start)))
                }
                SleepNightStore.prune(app, SleepTrackPrefs.load(app).keepNights)
            }
            state.value = state.value.copy(saved = state.value.saved + 1)
            SleepTrackPrefs.setActiveStart(app, 0L)
            onDone()
        }
    }

    fun recover(context: Context) {
        val app = context.applicationContext
        val start = SleepTrackPrefs.activeStart(app)
        if (start <= 0L || state.value.active) return
        val partial = SleepNightStore.load(app, start)
        if (partial != null && partial.end > start) finalize(app, partial) else SleepTrackPrefs.setActiveStart(app, 0L)
    }
}
