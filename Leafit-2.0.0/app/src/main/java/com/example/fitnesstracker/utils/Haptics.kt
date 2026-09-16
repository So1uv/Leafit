package com.example.fitnesstracker.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class HapticLevel { LIGHT, MEDIUM, SUCCESS, DOUBLE }

object Haptics {
    private fun vibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun perform(context: Context, level: HapticLevel) {
        val vib = vibrator(context) ?: return
        if (!vib.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (level) {
                HapticLevel.LIGHT   -> VibrationEffect.createOneShot(12, 60)
                HapticLevel.MEDIUM  -> VibrationEffect.createOneShot(26, 130)
                HapticLevel.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 20, 60, 40), intArrayOf(0, 110, 0, 200), -1)
                HapticLevel.DOUBLE  -> VibrationEffect.createWaveform(longArrayOf(0, 32, 80, 32), intArrayOf(0, 180, 0, 180), -1)
            }
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(when (level) {
                HapticLevel.LIGHT -> 12L
                HapticLevel.MEDIUM -> 26L
                HapticLevel.SUCCESS -> 60L
                HapticLevel.DOUBLE -> 80L
            })
        }
    }
}
