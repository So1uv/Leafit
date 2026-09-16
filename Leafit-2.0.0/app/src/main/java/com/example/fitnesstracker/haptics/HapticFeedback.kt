package com.example.fitnesstracker.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

object HapticFeedback {
    
    fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }
    }
    
    fun lightTap(context: Context) {
        vibrate(context, HapticPattern.LIGHT_TAP)
    }
    
    fun mediumTap(context: Context) {
        vibrate(context, HapticPattern.MEDIUM_TAP)
    }
    
    fun heavyTap(context: Context) {
        vibrate(context, HapticPattern.HEAVY_TAP)
    }
    
    fun successPattern(context: Context) {
        vibrate(context, HapticPattern.SUCCESS)
    }
    
    fun warningPattern(context: Context) {
        vibrate(context, HapticPattern.WARNING)
    }
    
    fun errorPattern(context: Context) {
        vibrate(context, HapticPattern.ERROR)
    }
    
    fun customPattern(context: Context, pattern: LongArray, amplitudes: IntArray? = null) {
        vibrate(context, pattern, amplitudes)
    }
    
    private fun vibrate(context: Context, pattern: HapticPattern) {
        vibrate(context, pattern.timings, pattern.amplitudes)
    }
    
    private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray?) {
        val vibrator = getVibrator(context) ?: return
        
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = if (amplitudes != null) {
                VibrationEffect.createWaveform(timings, amplitudes)
            } else {
                VibrationEffect.createWaveform(timings)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings)
        }
    }
}

object HapticPattern {
    val LIGHT_TAP = HapticPattern(
        timings = longArrayOf(0, 30),
        amplitudes = intArrayOf(0, 50)
    )
    
    val MEDIUM_TAP = HapticPattern(
        timings = longArrayOf(0, 50),
        amplitudes = intArrayOf(0, 100)
    )
    
    val HEAVY_TAP = HapticPattern(
        timings = longArrayOf(0, 70),
        amplitudes = intArrayOf(0, 255)
    )
    
    val SUCCESS = HapticPattern(
        timings = longArrayOf(0, 50, 30, 50),
        amplitudes = intArrayOf(0, 100, 0, 150)
    )
    
    val WARNING = HapticPattern(
        timings = longArrayOf(0, 40, 30, 40, 30, 40),
        amplitudes = intArrayOf(0, 150, 0, 150, 0, 150)
    )
    
    val ERROR = HapticPattern(
        timings = longArrayOf(0, 60, 40, 60),
        amplitudes = intArrayOf(0, 255, 0, 255)
    )
}

data class HapticPattern(
    val timings: LongArray,
    val amplitudes: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HapticPattern) return false
        if (!timings.contentEquals(other.timings)) return false
        if (!amplitudes.contentEquals(other.amplitudes)) return false
        return true
    }
    
    override fun hashCode(): Int {
        var result = timings.contentHashCode()
        result = 31 * result + amplitudes.contentHashCode()
        return result
    }
}
