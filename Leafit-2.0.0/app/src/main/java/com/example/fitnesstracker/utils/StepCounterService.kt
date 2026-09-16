package com.example.fitnesstracker.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.R

class StepCounterService : Service() {

    private var stepCounter: StepCounter? = null

    override fun onCreate() {
        super.onCreate()

        if (!StepCounter.hasActivityRecognitionPermission(applicationContext)) {
            stopSelf()
            return
        }

        createChannel()
        val foregroundStarted = runCatching {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }.isSuccess

        if (!foregroundStarted) {
            stopSelf()
            return
        }

        stepCounter = StepCounter.getInstance(applicationContext).also {
            if (!it.start("service")) stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!StepCounter.hasActivityRecognitionPermission(applicationContext)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!StepTrackingPreferences.isBackgroundEnabled(applicationContext) || stepCounter?.start("service") != true) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stepCounter?.stop("service")
        stepCounter = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_running)
            .setContentTitle(com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(applicationContext).getString(R.string.notif_steps_title))
            .setContentText(com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(applicationContext).getString(R.string.notif_steps_text))
            .setContentIntent(android.app.PendingIntent.getActivity(
                this, 0, Intent(this, com.example.fitnesstracker.MainActivity::class.java),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            ))
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(applicationContext).getString(R.string.notif_steps_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(applicationContext).getString(R.string.notif_steps_channel)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "steps_tracking_channel"
        private const val NOTIFICATION_ID = 2401

        fun start(context: Context) {
            val appContext = context.applicationContext
            if (!StepTrackingPreferences.isBackgroundEnabled(appContext) || !StepCounter.hasActivityRecognitionPermission(appContext)) return
            val intent = Intent(appContext, StepCounterService::class.java)
            runCatching { ContextCompat.startForegroundService(appContext, intent) }
        }

        fun stop(context: Context) {
            runCatching {
                context.applicationContext.stopService(
                    Intent(context.applicationContext, StepCounterService::class.java)
                )
            }
        }
    }
}
