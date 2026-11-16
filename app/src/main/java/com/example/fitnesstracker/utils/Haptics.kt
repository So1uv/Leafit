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
    private var lastTexture: HapticTexture? = null
    private var cachedMotor: Vibrator? = null

    @Volatile private var snapshot: Snapshot? = null
    private var systemCheckedAt = 0L
    @Volatile private var systemOn = true
    private val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, _ ->
        snapshot = Snapshot.read(prefs)
    }

    private data class Snapshot(val enabled: Boolean, val intensity: Float, val textures: Map<HapticTexture, Boolean>) {
        companion object {
            fun read(p: android.content.SharedPreferences) = Snapshot(
                p.getBoolean("enabled", true), p.getFloat("intensity", .65f).coerceIn(.25f, 1f),
                HapticTexture.entries.associateWith { p.getBoolean("texture_${it.name}", true) })
        }
    }

    private fun preferences(context: Context) = context.applicationContext.getSharedPreferences("leaf_haptics", Context.MODE_PRIVATE)
    private fun state(context: Context): Snapshot = snapshot ?: synchronized(this) {
        snapshot ?: preferences(context).let { prefs ->
            prefs.registerOnSharedPreferenceChangeListener(listener)
            Snapshot.read(prefs).also { snapshot = it }
        }
    }

    fun enabled(context: Context) = state(context).enabled
    fun intensity(context: Context) = state(context).intensity
    fun setEnabled(context: Context, enabled: Boolean) { preferences(context).edit().putBoolean("enabled", enabled).apply(); snapshot = null }
    fun setIntensity(context: Context, value: Float) { preferences(context).edit().putFloat("intensity", value.coerceIn(.25f, 1f)).apply(); snapshot = null }

    fun textureEnabled(context: Context, texture: HapticTexture) = state(context).textures[texture] != false
    fun setTextureEnabled(context: Context, texture: HapticTexture, value: Boolean) {
        preferences(context).edit().putBoolean("texture_${texture.name}", value).apply(); snapshot = null
    }
    fun available(context: Context) = motor(context)?.hasVibrator() == true
    fun systemEnabled(context: Context): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - systemCheckedAt > 3_000L) {
            systemCheckedAt = now
            systemOn = runCatching {
                Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0
            }.getOrDefault(true)
        }
        return systemOn
    }

    private fun vibrator(context: Context): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    private fun motor(context: Context): Vibrator? = cachedMotor ?: vibrator(context.applicationContext)?.also { cachedMotor = it }

    fun perform(context: Context, level: HapticLevel) = play(context, when (level) {
        HapticLevel.LIGHT -> HapticTexture.DETENT
        HapticLevel.MEDIUM -> HapticTexture.PRESS
        HapticLevel.SUCCESS -> HapticTexture.CONFIRM
        HapticLevel.DOUBLE -> HapticTexture.START
    })

    fun play(context: Context, texture: HapticTexture) {
        val now = SystemClock.elapsedRealtime()
        val gap = when (texture) { HapticTexture.DETENT -> 28L; HapticTexture.PRESS -> 60L; else -> 0L }
        if (gap > 0 && lastTexture == texture && now - lastTick < gap) return
        val prefs = state(context)
        if (!prefs.enabled || prefs.textures[texture] == false || !systemEnabled(context)) return
        val motor = motor(context) ?: return
        if (!motor.hasVibrator()) return
        lastTick = now; lastTexture = texture
        val strength = prefs.intensity
        runCatching {
            val effect = composed(motor, texture, strength) ?: predefined(texture) ?: waveform(motor, texture, strength)
            if (Build.VERSION.SDK_INT >= 33) motor.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            else {
                @Suppress("DEPRECATION")
                motor.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            }
        }
    }

    private fun composed(motor: Vibrator, texture: HapticTexture, k: Float): VibrationEffect? {
        if (Build.VERSION.SDK_INT < 30) return null
        val tick = VibrationEffect.Composition.PRIMITIVE_TICK
        val lowTick = if (Build.VERSION.SDK_INT >= 31) VibrationEffect.Composition.PRIMITIVE_LOW_TICK else VibrationEffect.Composition.PRIMITIVE_TICK
        val needed = when (texture) {
            HapticTexture.DETENT -> intArrayOf(lowTick)
            HapticTexture.PRESS -> intArrayOf(VibrationEffect.Composition.PRIMITIVE_CLICK)
            HapticTexture.START -> intArrayOf(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, VibrationEffect.Composition.PRIMITIVE_CLICK)
            HapticTexture.PAUSE -> intArrayOf(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, tick)
            HapticTexture.CONFIRM -> intArrayOf(tick, VibrationEffect.Composition.PRIMITIVE_CLICK)
        }
        if (!motor.areAllPrimitivesSupported(*needed)) {
            if (!motor.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) return null
            val click = VibrationEffect.Composition.PRIMITIVE_CLICK
            return VibrationEffect.startComposition().apply {
                when (texture) {
                    HapticTexture.DETENT -> addPrimitive(click, .32f * k)
                    HapticTexture.PRESS -> addPrimitive(click, .62f * k)
                    HapticTexture.START -> { addPrimitive(click, .25f * k); addPrimitive(click, .7f * k, 60) }
                    HapticTexture.PAUSE -> { addPrimitive(click, .6f * k); addPrimitive(click, .22f * k, 80) }
                    HapticTexture.CONFIRM -> { addPrimitive(click, .25f * k); addPrimitive(click, .45f * k, 50); addPrimitive(click, .75f * k, 60) }
                }
            }.compose()
        }
        return VibrationEffect.startComposition().apply {
            when (texture) {
                HapticTexture.DETENT -> addPrimitive(lowTick, (.55f * k).coerceAtMost(1f))
                HapticTexture.PRESS -> addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, .7f * k)
                HapticTexture.START -> { addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, .6f * k); addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, .85f * k, 20) }
                HapticTexture.PAUSE -> { addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, .7f * k); addPrimitive(tick, .35f * k, 40) }
                HapticTexture.CONFIRM -> { addPrimitive(tick, .45f * k); addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, .9f * k, 70) }
            }
        }.compose()
    }

    private fun predefined(texture: HapticTexture): VibrationEffect? {
        if (Build.VERSION.SDK_INT < 29) return null
        return VibrationEffect.createPredefined(when (texture) {
            HapticTexture.DETENT -> VibrationEffect.EFFECT_TICK
            HapticTexture.PRESS -> VibrationEffect.EFFECT_CLICK
            HapticTexture.START -> VibrationEffect.EFFECT_HEAVY_CLICK
            HapticTexture.PAUSE -> VibrationEffect.EFFECT_DOUBLE_CLICK
            HapticTexture.CONFIRM -> VibrationEffect.EFFECT_DOUBLE_CLICK
        })
    }

    private fun waveform(motor: Vibrator, texture: HapticTexture, k: Float): VibrationEffect {
        val (timings, levels) = when (texture) {
            HapticTexture.DETENT -> longArrayOf(0, 8) to intArrayOf(0, 105)
            HapticTexture.PRESS -> longArrayOf(0, 16) to intArrayOf(0, 130)
            HapticTexture.START -> longArrayOf(0, 12, 60, 20) to intArrayOf(0, 85, 0, 155)
            HapticTexture.PAUSE -> longArrayOf(0, 18, 70, 10) to intArrayOf(0, 135, 0, 70)
            HapticTexture.CONFIRM -> longArrayOf(0, 10, 45, 14, 60, 22) to intArrayOf(0, 70, 0, 115, 0, 155)
        }
        return if (motor.hasAmplitudeControl()) VibrationEffect.createWaveform(timings,
            levels.map { (it * k).toInt().coerceIn(1, 255) }.toIntArray(), -1)
        else VibrationEffect.createWaveform(timings, -1)
    }
}

class LeafHapticFeedback(context: Context) : HapticFeedback {
    private val app = context.applicationContext
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        Haptics.play(app, if (hapticFeedbackType == HapticFeedbackType.LongPress) HapticTexture.PRESS else HapticTexture.DETENT)
    }
}
