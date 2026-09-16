package com.example.fitnesstracker.workout

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WorkoutTimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observing = false
    private var intentionalStop = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        channels(this)
        val engine = WorkoutSessionEngine.get(this)
        try {
            val initial = notification(engine.liveState.value)
            if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION, initial, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(NOTIFICATION, initial)
        } catch (_: RuntimeException) {
            engine.backgroundUnavailable(); stopSelf(); return START_NOT_STICKY
        }
        if (!observing) {
            observing = true
            scope.launch {
                engine.ready.first { it }
                engine.liveState.collect { state ->
                    if (!state.active) {
                        intentionalStop = true; stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
                    } else runCatching { getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification(state)) }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun notification(state: SessionDisplay): Notification {
        val ctx = LanguagePreferences.localizedContext(this)
        val body = if (state.paused) DateUtils.formatDuration(state.seconds()) else if (state.timingSet) state.exerciseName else state.name(ctx)
        val builder = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_running)
            .setContentTitle(state.label(ctx)).setContentText(body)
            .setContentIntent(openWorkout(this)).setOnlyAlertOnce(true).setSilent(true)
            .setOngoing(true).setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (state.active && !state.paused) builder
            .setWhen(System.currentTimeMillis() + state.clockBase - SystemClock.elapsedRealtime())
            .setUsesChronometer(true).setChronometerCountDown(state.resting)
        else builder.setShowWhen(false)
        if (state.active) {
            builder.addAction(0, ctx.getString(if (state.paused) R.string.timer_resume else R.string.timer_pause),
                timerAction(this, if (state.paused) "resume" else "pause", state.token))
            if (state.resting) builder.addAction(0, ctx.getString(R.string.timer_skip), timerAction(this, "skip", state.token))
            builder.addAction(0, ctx.getString(R.string.timer_finish), openWorkout(this, state.token))
        }
        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        scope.cancel()
        if (!intentionalStop) WorkoutSessionEngine.current()?.pauseWorkout(feedback = false)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "workout_timers"
        private const val REST_CHANNEL = "workout_rest_finished"
        private const val NOTIFICATION = 4301
        private const val REST_NOTIFICATION = 4302
        fun start(context: Context): Boolean = runCatching {
            ContextCompat.startForegroundService(context, Intent(context, WorkoutTimerService::class.java)); true
        }.getOrDefault(false)
        private fun channels(context: Context) {
            val ctx = LanguagePreferences.localizedContext(context)
            context.getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(
                NotificationChannel(CHANNEL, ctx.getString(R.string.timer_channel), NotificationManager.IMPORTANCE_LOW),
                NotificationChannel(REST_CHANNEL, ctx.getString(R.string.timer_rest_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    enableVibration(true); vibrationPattern = longArrayOf(0, 30, 90, 45)
                }
            ))
        }
        fun clearRestAlert(context: Context) { context.getSystemService(NotificationManager::class.java).cancel(REST_NOTIFICATION) }
        fun restFinished(context: Context) {
            channels(context)
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            val ctx = LanguagePreferences.localizedContext(context)
            runCatching {
                context.getSystemService(NotificationManager::class.java).notify(REST_NOTIFICATION,
                    NotificationCompat.Builder(context, REST_CHANNEL).setSmallIcon(R.drawable.ic_running)
                        .setContentTitle(ctx.getString(R.string.timer_rest_done)).setContentText(ctx.getString(R.string.timer_rest_done_hint))
                        .setContentIntent(openWorkout(context)).setAutoCancel(true).setTimeoutAfter(120_000)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
            }
        }
        fun openWorkout(context: Context, finishToken: String? = null): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra("shortcut_action", "workout")
            if (finishToken != null) intent.putExtra("finish_workout_token", finishToken)
            return PendingIntent.getActivity(context, if (finishToken == null) 4300 else 4301, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        fun timerAction(context: Context, action: String, token: String): PendingIntent = PendingIntent.getBroadcast(context,
            when (action) { "pause" -> 4311; "resume" -> 4312; else -> 4313 },
            Intent(context, WorkoutTimerActionReceiver::class.java).setAction(action).putExtra("token", token),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

class WorkoutTimerActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            try {
                val engine = WorkoutSessionEngine.get(context)
                withTimeout(7000) { engine.ready.first { it } }
                if (engine.matchesToken(intent.getStringExtra("token"))) when (intent.action) {
                    "pause" -> engine.pauseWorkout(feedback = false)
                    "resume" -> engine.resumeWorkout()
                    "skip" -> engine.skipRest()
                }
            } catch (_: Exception) { }
            finally { pending.finish() }
        }
    }
}
