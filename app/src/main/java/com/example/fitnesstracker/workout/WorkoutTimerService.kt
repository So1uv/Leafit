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
    private var intervalWakeLock: android.os.PowerManager.WakeLock? = null

    private fun keepIntervalsAwake(state: SessionDisplay) {
        if (state.active && !state.paused && state.guiding) {
            val lock = intervalWakeLock ?: getSystemService(android.os.PowerManager::class.java)
                .newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "Leafit:Intervals")
                .also { it.setReferenceCounted(false); intervalWakeLock = it }
            if (!lock.isHeld) lock.acquire(6L * 60L * 60L * 1000L)
        } else {
            intervalWakeLock?.takeIf { it.isHeld }?.release()
        }
    }

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
                    keepIntervalsAwake(state)
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
        val body = if (state.paused) DateUtils.formatDuration(state.seconds())
            else if (state.guiding) state.guideName
            else if (state.timingSet) state.exerciseName else state.name(ctx)
        val builder = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_running)
            .setContentTitle(state.label(ctx)).setContentText(body)
            .setContentIntent(openWorkout(this)).setOnlyAlertOnce(true).setSilent(true)
            .setOngoing(true).setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setColor(com.example.fitnesstracker.utils.LeafNotify.ACCENT)
        if (state.totalSets > 0) {
            builder.setSubText(ctx.getString(R.string.b7_notif_sets, state.completedSets, state.totalSets))
            builder.setProgress(state.totalSets, state.completedSets.coerceAtMost(state.totalSets), false)
        }
        val detail = listOfNotNull(
            state.exerciseName.takeIf { it.isNotBlank() && it != body }?.let { ctx.getString(R.string.b7_notif_exercise, it) },
            state.name(ctx).takeIf { it.isNotBlank() && it != body })
        if (detail.isNotEmpty()) builder.setStyle(NotificationCompat.BigTextStyle().bigText((listOf(body) + detail).joinToString("\n")))
        if (state.active && !state.paused) builder
            .setWhen(System.currentTimeMillis() + state.clockBase - SystemClock.elapsedRealtime())
            .setUsesChronometer(true).setChronometerCountDown(state.resting || state.guiding)
        else builder.setShowWhen(false)
        if (state.active) {
            builder.addAction(0, ctx.getString(if (state.paused) R.string.timer_resume else R.string.timer_pause),
                timerAction(this, if (state.paused) "resume" else "pause", state.token))
            if (state.resting) builder.addAction(0, ctx.getString(R.string.timer_skip), timerAction(this, "skip", state.token))
            if (state.guiding) builder.addAction(0, ctx.getString(R.string.timer_skip), timerAction(this, "skip_guide", state.token))
            builder.addAction(0, ctx.getString(R.string.timer_finish), openWorkout(this, state.token))
        }
        val ticking = state.active && !state.paused
        val sets = if (state.totalSets > 0) ctx.getString(R.string.b7_notif_sets, state.completedSets, state.totalSets) else null
        return com.example.fitnesstracker.utils.LeafNotify.card(this, builder, R.drawable.ic_running, state.label(ctx),
            if (state.paused) DateUtils.formatDuration(state.seconds()) else body,
            caption = body.takeIf { ticking || state.paused },
            stats = (listOfNotNull(sets) + detail).take(3),
            progress = if (state.totalSets > 0) state.completedSets.coerceAtMost(state.totalSets) else -1, goal = state.totalSets,
            clockBase = if (ticking) state.clockBase else 0L, countDown = state.resting || state.guiding)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        intervalWakeLock?.takeIf { it.isHeld }?.release()
        scope.cancel()
        if (!intentionalStop) WorkoutSessionEngine.current()?.pauseWorkout(feedback = false)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "workout_timers"
        private const val REST_CHANNEL = "workout_rest_finished"
        private const val GUIDE_CHANNEL = "workout_guide_signals"
        private const val NOTIFICATION = 4301
        private const val REST_NOTIFICATION = 4302
        private const val GUIDE_NOTIFICATION = 4303
        fun start(context: Context): Boolean = runCatching {
            ContextCompat.startForegroundService(context, Intent(context, WorkoutTimerService::class.java)); true
        }.getOrDefault(false)
        private fun channels(context: Context) {
            val ctx = LanguagePreferences.localizedContext(context)
            context.getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(
                NotificationChannel(CHANNEL, ctx.getString(R.string.timer_channel), NotificationManager.IMPORTANCE_LOW),
                NotificationChannel(REST_CHANNEL, ctx.getString(R.string.timer_rest_channel), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    enableVibration(true); vibrationPattern = longArrayOf(0, 30, 90, 45)
                },
                NotificationChannel(GUIDE_CHANNEL, ctx.getString(R.string.interval_title), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    enableVibration(true); vibrationPattern = longArrayOf(0, 50, 70, 50)
                }
            ))
            com.example.fitnesstracker.utils.LeafNotify.organize(context)
        }
        fun clearRestAlert(context: Context) { context.getSystemService(NotificationManager::class.java).cancel(REST_NOTIFICATION) }
        fun restFinished(context: Context) {
            channels(context)
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            val ctx = LanguagePreferences.localizedContext(context)
            runCatching {
                context.getSystemService(NotificationManager::class.java).notify(REST_NOTIFICATION,
                    NotificationCompat.Builder(context, REST_CHANNEL).setSmallIcon(R.drawable.ic_running)
                        .setColor(com.example.fitnesstracker.utils.LeafNotify.ACCENT).setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentTitle(ctx.getString(R.string.timer_rest_done)).setContentText(ctx.getString(R.string.timer_rest_done_hint))
                        .setContentIntent(openWorkout(context)).setAutoCancel(true).setTimeoutAfter(120_000)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).let { builder ->
                            com.example.fitnesstracker.utils.LeafNotify.card(context, builder, R.drawable.ic_running,
                                ctx.getString(R.string.timer_rest_channel), ctx.getString(R.string.timer_rest_done),
                                caption = ctx.getString(R.string.timer_rest_done_hint))
                        })
            }
        }
        fun guideChanged(context: Context, phase: String, exerciseName: String) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            channels(context)
            val ctx = LanguagePreferences.localizedContext(context)
            runCatching {
                context.getSystemService(NotificationManager::class.java).notify(GUIDE_NOTIFICATION,
                    NotificationCompat.Builder(context, GUIDE_CHANNEL).setSmallIcon(R.drawable.ic_running)
                        .setColor(com.example.fitnesstracker.utils.LeafNotify.ACCENT)
                        .setContentTitle(ctx.getString(when (phase) {
                            "work" -> R.string.interval_work
                            "rest" -> R.string.interval_rest
                            else -> R.string.interval_complete
                        }))
                        .setContentText(exerciseName).setContentIntent(openWorkout(context))
                        .setAutoCancel(true).setTimeoutAfter(30_000L).let { builder ->
                            com.example.fitnesstracker.utils.LeafNotify.card(context, builder, R.drawable.ic_running,
                                ctx.getString(R.string.interval_title), ctx.getString(when (phase) {
                                    "work" -> R.string.interval_work
                                    "rest" -> R.string.interval_rest
                                    else -> R.string.interval_complete
                                }), caption = exerciseName)
                        })
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
            when (action) { "pause" -> 4311; "resume" -> 4312; "skip_guide" -> 4314; else -> 4313 },
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
                    "skip_guide" -> engine.skipIntervalPhase()
                }
            } catch (_: Exception) { }
            finally { pending.finish() }
        }
    }
}
