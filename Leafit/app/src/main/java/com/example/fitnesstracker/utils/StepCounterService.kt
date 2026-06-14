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

/**
 * Foreground service для підрахунку кроків, коли застосунок закритий або у фоні.
 * Сервіс спеціально зроблений fail-safe: якщо Android не дозволяє старт health-FGS
 * або немає дозволу ACTIVITY_RECOGNITION, застосунок не падає, а сервіс просто зупиняється.
 */
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

        stepCounter = StepCounter(applicationContext).also { it.start() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!StepCounter.hasActivityRecognitionPermission(applicationContext)) {
            stopSelf()
            return START_NOT_STICKY
        }
        stepCounter?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        stepCounter?.release()
        stepCounter = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_running)
            .setContentTitle("Leafit")
            .setContentText("Підрахунок кроків активний")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Підрахунок кроків",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Фоновий підрахунок кроків"
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
            if (!StepCounter.hasActivityRecognitionPermission(appContext)) return
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
