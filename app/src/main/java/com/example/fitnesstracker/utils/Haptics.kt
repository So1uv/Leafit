package com.example.fitnesstracker.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

enum class HapticLevel { LIGHT, MEDIUM, SUCCESS, DOUBLE }
enum class HapticTexture { DETENT, PRESS, START, PAUSE, CONFIRM }

object Haptics {
    private var lastTick = 0L
    private var cachedMotor: Vibrator? = null
    private fun preferences(context: Context) = context.applicationContext.getSharedPreferences("leaf_haptics", Context.MODE_PRIVATE)
    fun enabled(context: Context) = preferences(context).getBoolean("enabled", true)
    fun intensity(context: Context) = preferences(context).getFloat("intensity", .65f).coerceIn(.25f, 1f)
    fun setEnabled(context: Context, enabled: Boolean) { preferences(context).edit().putBoolean("enabled", enabled).apply() }
    fun setIntensity(context: Context, value: Float) { preferences(context).edit().putFloat("intensity", value.coerceIn(.25f, 1f)).apply() }

    private fun vibrator(context: Context): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun perform(context: Context, level: HapticLevel) = play(context, when (level) {
        HapticLevel.LIGHT -> HapticTexture.DETENT
        HapticLevel.MEDIUM -> HapticTexture.PRESS
        HapticLevel.SUCCESS -> HapticTexture.CONFIRM
        HapticLevel.DOUBLE -> HapticTexture.START
    })

    fun play(context: Context, texture: HapticTexture) {
        val now = SystemClock.elapsedRealtime()
        if (texture == HapticTexture.DETENT && now - lastTick < 48) return
        if (!enabled(context) || Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) return
        val motor = cachedMotor ?: vibrator(context)?.also { cachedMotor = it } ?: return
        if (!motor.hasVibrator()) return
        lastTick = now
        val strength = intensity(context)
        runCatching {
            val effect = if (Build.VERSION.SDK_INT >= 30 && motor.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                val click = VibrationEffect.Composition.PRIMITIVE_CLICK
                VibrationEffect.startComposition().apply {
                    when (texture) {
                        HapticTexture.DETENT -> addPrimitive(click, .18f * strength)
                        HapticTexture.PRESS -> addPrimitive(click, .48f * strength)
                        HapticTexture.START -> { addPrimitive(click, .25f * strength); addPrimitive(click, .6f * strength, 65) }
                        HapticTexture.PAUSE -> { addPrimitive(click, .5f * strength); addPrimitive(click, .2f * strength, 80) }
                        HapticTexture.CONFIRM -> { addPrimitive(click, .22f * strength); addPrimitive(click, .42f * strength, 50); addPrimitive(click, .65f * strength, 65) }
                    }
                }.compose()
            } else {
                val (timings, levels) = when (texture) {
                    HapticTexture.DETENT -> longArrayOf(0, 8) to intArrayOf(0, 65)
                    HapticTexture.PRESS -> longArrayOf(0, 16) to intArrayOf(0, 130)
                    HapticTexture.START -> longArrayOf(0, 12, 60, 20) to intArrayOf(0, 85, 0, 155)
                    HapticTexture.PAUSE -> longArrayOf(0, 18, 70, 10) to intArrayOf(0, 135, 0, 70)
                    HapticTexture.CONFIRM -> longArrayOf(0, 10, 45, 14, 60, 22) to intArrayOf(0, 70, 0, 115, 0, 155)
                }
                if (motor.hasAmplitudeControl()) VibrationEffect.createWaveform(timings, levels.map { (it * strength).toInt() }.toIntArray(), -1)
                else VibrationEffect.createWaveform(timings, -1)
            }
            if (Build.VERSION.SDK_INT >= 33) motor.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            else {
                @Suppress("DEPRECATION")
                motor.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            }
        }
    }
}

class LeafHapticFeedback(context: Context) : HapticFeedback {
    private val app = context.applicationContext
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        Haptics.play(app, if (hapticFeedbackType == HapticFeedbackType.LongPress) HapticTexture.PRESS else HapticTexture.DETENT)
    }
}
