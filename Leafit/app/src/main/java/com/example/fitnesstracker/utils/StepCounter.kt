package com.example.fitnesstracker.utils

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

/**
 * Лічильник кроків.
 *
 * TYPE_STEP_COUNTER повертає загальну кількість кроків з моменту останнього
 * перезавантаження пристрою. Ми зберігаємо денну базу і рахуємо різницю.
 * Поточне значення також кладеться в SharedPreferences, щоб UI бачив кроки,
 * які оновив foreground service, навіть якщо екран був закритий.
 */
class StepCounter(context: Context) : SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private var listening = false

    private val _stepsToday = MutableStateFlow(readSteps())
    val stepsToday: StateFlow<Int> = _stepsToday

    private val _available = MutableStateFlow(sensor != null)
    val available: StateFlow<Boolean> = _available

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
        ensureDayIsFresh()
    }

    fun start(): Boolean {
        if (sensor == null) return false
        if (!hasActivityRecognitionPermission(appContext)) return false
        if (listening) return true

        return runCatching {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }.getOrDefault(false).also { registered ->
            listening = registered
        }
    }

    fun stop() {
        if (!listening) return
        runCatching { sensorManager.unregisterListener(this) }
        listening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        updateFromTotalSinceBoot(event.values[0].toInt())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_CURRENT_STEPS || key == KEY_DAY) {
            ensureDayIsFresh()
            _stepsToday.value = readSteps()
        }
    }

    fun release() {
        stop()
        runCatching { prefs.unregisterOnSharedPreferenceChangeListener(this) }
    }

    private fun updateFromTotalSinceBoot(totalSinceBoot: Int) {
        val today = getDayKey()
        val savedDay = prefs.getString(KEY_DAY, null)
        val savedBase = prefs.getInt(KEY_BASE_STEPS, -1)

        val steps = if (savedDay != today || savedBase < 0 || totalSinceBoot < savedBase) {
            prefs.edit()
                .putString(KEY_DAY, today)
                .putInt(KEY_BASE_STEPS, totalSinceBoot)
                .putInt(KEY_CURRENT_STEPS, 0)
                .apply()
            0
        } else {
            (totalSinceBoot - savedBase).coerceAtLeast(0).also { value ->
                prefs.edit()
                    .putString(KEY_DAY, today)
                    .putInt(KEY_CURRENT_STEPS, value)
                    .apply()
            }
        }

        _stepsToday.value = steps
    }

    private fun ensureDayIsFresh() {
        val today = getDayKey()
        val savedDay = prefs.getString(KEY_DAY, null)
        if (savedDay != null && savedDay != today) {
            prefs.edit()
                .putString(KEY_DAY, today)
                .putInt(KEY_BASE_STEPS, -1)
                .putInt(KEY_CURRENT_STEPS, 0)
                .apply()
        }
    }

    private fun readSteps(): Int = prefs.getInt(KEY_CURRENT_STEPS, 0).coerceAtLeast(0)

    private fun getDayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    companion object {
        const val PREFS = "steps_prefs"
        const val KEY_DAY = "base_day"
        const val KEY_BASE_STEPS = "base_steps"
        const val KEY_CURRENT_STEPS = "current_steps"

        fun hasActivityRecognitionPermission(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
