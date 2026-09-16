package com.example.fitnesstracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.StepRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StepCounter private constructor(context: Context) : SensorEventListener {
    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val worker = HandlerThread("LeafitSteps").apply { start() }
    private val handler = Handler(worker.looper)
    private val owners = mutableSetOf<String>()
    @Volatile private var listening = false
    private val boot = Settings.Global.getInt(appContext.contentResolver, Settings.Global.BOOT_COUNT, -1)
    private val writes = Channel<StepRecord>(Channel.UNLIMITED)
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _stepsToday = MutableStateFlow(0)
    val stepsToday: StateFlow<Int> = _stepsToday
    val available: StateFlow<Boolean> = MutableStateFlow(sensor != null)
    private var snapshot = StepSnapshot("", 0, -1, boot)

    private val refresh = object : Runnable {
        override fun run() {
            refreshDay()
            if (listening) handler.postDelayed(this, 30_000L)
        }
    }

    init {
        ioScope.launch {
            val dao = AppDatabase.getInstance(appContext).stepDao()
            for (record in writes) {
                try { dao.upsert(record) }
                catch (e: Exception) { Log.e("LeafitSteps", "Could not save step history", e) }
            }
        }
        handler.post {
            val savedDay = prefs.getString(KEY_DAY, null)
            val current = prefs.getInt(KEY_CURRENT_STEPS, 0).coerceAtLeast(0)
            val base = prefs.getInt(KEY_BASE_STEPS, -1)
            snapshot = StepSnapshot(
                savedDay?.let { value ->
                    val parts = value.split("-")
                    if (parts.size == 2) runCatching {
                        LocalDate.ofYearDay(parts[0].toInt(), parts[1].toInt()).toString()
                    }.getOrDefault("") else value
                }.orEmpty(), current,
                prefs.getInt("last_total", if (base >= 0) base + current else -1),
                prefs.getInt("boot_count", boot)
            )
            refreshDay()
        }
    }

    @Synchronized
    fun start(owner: String = "ui"): Boolean {
        if (sensor == null || !hasActivityRecognitionPermission(appContext)) return false
        owners.add(owner)
        if (listening) return true
        listening = runCatching {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, handler)
        }.getOrDefault(false)
        if (listening) handler.post(refresh) else owners.remove(owner)
        return listening
    }

    @Synchronized
    fun stop(owner: String = "ui") {
        owners.remove(owner)
        if (owners.isNotEmpty() || !listening) return
        sensorManager.unregisterListener(this)
        listening = false
        handler.removeCallbacks(refresh)
    }

    fun release() = stop()

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER || event.values.isEmpty()) return
        val total = event.values[0].toInt().coerceAtLeast(0)
        val eventTime = System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() - event.timestamp) / 1_000_000L
        val eventDay = Instant.ofEpochMilli(eventTime).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        if (eventDay < snapshot.day) {
            snapshot = snapshot.copy(total = total, boot = boot)
            persist()
            return
        }
        snapshot = StepAccumulator.update(snapshot, eventDay, total, boot)
        persist()
        refreshDay()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun refreshDay() {
        val today = LocalDate.now().toString()
        _stepsToday.value = if (snapshot.day == today) snapshot.steps else 0
    }

    private fun persist() {
        prefs.edit().putString(KEY_DAY, snapshot.day)
            .putInt(KEY_CURRENT_STEPS, snapshot.steps)
            .putInt("last_total", snapshot.total).putInt("boot_count", snapshot.boot).commit()
        val dayStart = LocalDate.parse(snapshot.day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        writes.trySend(StepRecord(dayStart = dayStart, steps = snapshot.steps))
        _stepsToday.value = if (snapshot.day == LocalDate.now().toString()) snapshot.steps else 0
    }

    companion object {
        const val PREFS = "steps_prefs"
        const val KEY_DAY = "base_day"
        const val KEY_BASE_STEPS = "base_steps"
        const val KEY_CURRENT_STEPS = "current_steps"
        @Volatile private var instance: StepCounter? = null
        fun getInstance(context: Context): StepCounter = instance ?: synchronized(this) {
            instance ?: StepCounter(context).also { instance = it }
        }
        fun hasActivityRecognitionPermission(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }
}
