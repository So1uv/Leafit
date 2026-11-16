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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
            if (!it.start("service")) stopSelf() else observeSteps(it)
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
        scope.cancel()
        stepCounter?.stop("service")
        stepCounter = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(steps: Int = StepCounter.getInstance(applicationContext).stepsToday.value): Notification {
        val l = com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(applicationContext)
        val goal = GoalPreferences.getStepsGoal(applicationContext).coerceAtLeast(1)
        val left = (goal - steps).coerceAtLeast(0)
        val km = steps * 0.000762f
        val builder = LeafNotify.styled(NotificationCompat.Builder(this, CHANNEL_ID),
                l.getString(R.string.b7_notif_steps_title, LeafNotify.number(this, steps)),
                if (left > 0) l.getString(R.string.b7_notif_steps_left, LeafNotify.number(this, left), LeafNotify.number(this, goal))
                else l.getString(R.string.b7_notif_steps_done, LeafNotify.number(this, goal)),
                l.getString(R.string.b7_notif_steps_details, km))
            .setSmallIcon(R.drawable.ic_running)
            .setContentIntent(LeafNotify.open(this, "home", 2402))
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return LeafNotify.progressCard(this, builder,
            headline = l.getString(R.string.b8_notif_steps_headline),
            value = LeafNotify.number(this, steps),
            caption = l.getString(R.string.b8_notif_of_goal, LeafNotify.number(this, goal)),
            progress = steps, goal = goal,
            stats = listOf(l.getString(R.string.b8_notif_km, km), l.getString(R.string.b8_notif_kcal, (steps * 0.04f).toInt()),
                if (left > 0) l.getString(R.string.b8_notif_left, LeafNotify.number(this, left)) else l.getString(R.string.b8_notif_goal_done)),
            tracker = R.drawable.ms_steps)
    }

    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    private var lastShown = -1
    private var lastShownAt = 0L
    private fun observeSteps(counter: StepCounter) {
        scope.launch {
            counter.stepsToday.collect { steps ->
                val now = android.os.SystemClock.elapsedRealtime()
                if (lastShown < 0 || kotlin.math.abs(steps - lastShown) >= 50 || now - lastShownAt >= 30_000L) {
                    lastShown = steps; lastShownAt = now
                    runCatching { getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(steps)) }
                }
            }
        }
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
            LeafNotify.organize(applicationContext)
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
