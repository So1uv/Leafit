package com.example.fitnesstracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StepBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            if (StepTrackingPreferences.isBackgroundEnabled(context)) StepCounterService.start(context)
            if (BedtimeReminder.isEnabled(context)) BedtimeReminder.schedule(context)
            if (WaterReminder.isEnabled(context)) WaterReminder.scheduleNext(context)
        }
    }
}
