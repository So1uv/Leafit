package com.example.fitnesstracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StepBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val restart = intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        val clockChanged = intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED
        if (restart && StepTrackingPreferences.isBackgroundEnabled(context) && StepCounter.hasActivityRecognitionPermission(context)) {
            StepCounterService.start(context)
        }
        if (restart || clockChanged) {
            runCatching { if (WaterReminder.isEnabled(context)) WaterReminder.scheduleNext(context) }
            runCatching { if (BedtimeReminder.isEnabled(context)) BedtimeReminder.schedule(context) }
            runCatching { com.example.fitnesstracker.sleep.SleepAlarms.schedule(context) }
        }
    }
}
