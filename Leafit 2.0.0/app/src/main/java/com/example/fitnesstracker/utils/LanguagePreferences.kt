package com.example.fitnesstracker.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val code: String) {
    UKRAINIAN("uk"),
    RUSSIAN("ru"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: UKRAINIAN
    }
}

object LanguagePreferences {
    private const val PREFS = "ui_language_prefs"
    private const val KEY_LANG = "lang"

    fun get(context: Context): AppLanguage {
        val code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, null)
        return AppLanguage.fromCode(code)
    }

    fun set(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, language.code).apply()
    }

    fun applyTo(context: Context): Context {
        val language = get(context)
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun localizedContext(context: Context): Context {
        val language = get(context)
        val locale = Locale(language.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

object StepTrackingPreferences {
    private const val PREFS = "step_tracking_prefs"
    private const val KEY_BG = "background_enabled"

    fun isBackgroundEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_BG, true)

    fun setBackgroundEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_BG, enabled).apply()
    }
}

object WaterPreferences {
    private const val PREFS = "water_prefs"
    private const val KEY_VOLUME = "default_volume"

    fun getDefaultVolume(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_VOLUME, 250)

    fun setDefaultVolume(context: Context, volume: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_VOLUME, volume).apply()
    }
}

object GoalPreferences {
    private const val PREFS = "goal_prefs"
    private const val KEY_STEPS = "goal_steps"
    private const val KEY_WATER = "goal_water"
    private const val KEY_SLEEP = "goal_sleep_minutes"

    fun getStepsGoal(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_STEPS, 10000)
    fun setStepsGoal(context: Context, value: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_STEPS, value).apply()

    fun getWaterGoal(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_WATER, 2000)
    fun setWaterGoal(context: Context, value: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_WATER, value).apply()

    fun getSleepGoalMinutes(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_SLEEP, 480)
    fun setSleepGoalMinutes(context: Context, value: Int) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_SLEEP, value).apply()
}

object MeasurementHistory {
    private const val PREFS = "measure_prefs"

    fun snapshot(context: Context, chest: Float?, waist: Float?, hips: Float?, bicep: Float?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat("chest", chest ?: -1f).putFloat("waist", waist ?: -1f)
            .putFloat("hips", hips ?: -1f).putFloat("bicep", bicep ?: -1f)
            .apply()
    }

    fun previous(context: Context, key: String): Float? {
        val v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(key, -1f)
        return if (v < 0f) null else v
    }
}
