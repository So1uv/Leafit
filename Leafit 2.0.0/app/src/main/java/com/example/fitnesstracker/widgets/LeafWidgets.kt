package com.example.fitnesstracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.room.InvalidationTracker
import com.example.fitnesstracker.MainActivity
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.tracking.LivePhase
import com.example.fitnesstracker.tracking.LiveWorkoutActivity
import com.example.fitnesstracker.tracking.LiveWorkoutStore
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import com.example.fitnesstracker.workout.SessionDisplay
import com.example.fitnesstracker.workout.WorkoutSessionEngine
import com.example.fitnesstracker.workout.WorkoutTimerService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

object LeafWidgets {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requests = Channel<Unit>(Channel.CONFLATED)
    private val lock = Mutex()
    private var initialized = false
    private lateinit var observer: InvalidationTracker.Observer
    val providers = listOf(TodayWidget::class.java, SleepWidget::class.java, SessionWidget::class.java)

    @Synchronized fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        observer = object : InvalidationTracker.Observer("step_records", "workouts", "sleep_records") {
            override fun onInvalidated(tables: Set<String>) { requestUpdate(app) }
        }
        scope.launch {
            AppDatabase.getInstance(app).invalidationTracker.addObserver(observer)
            for (ignored in requests) {
                delay(250)
                runCatching { update(app) }
            }
        }
        scope.launch {
            LiveWorkoutStore.state.map { Triple(it.phase, it.token, (it.distanceMeters / 100).toInt()) }
                .distinctUntilChanged().collect { requestUpdate(app) }
        }
        requestUpdate(app)
    }

    fun requestUpdate(context: Context) {
        initialize(context)
        requests.trySend(Unit)
    }

    suspend fun update(context: Context) = lock.withLock {
        val manager = AppWidgetManager.getInstance(context)
        val todayIds = manager.getAppWidgetIds(ComponentName(context, TodayWidget::class.java))
        val sleepIds = manager.getAppWidgetIds(ComponentName(context, SleepWidget::class.java))
        val sessionIds = manager.getAppWidgetIds(ComponentName(context, SessionWidget::class.java))
        if (todayIds.isEmpty() && sleepIds.isEmpty() && sessionIds.isEmpty()) return@withLock
        val ctx = LanguagePreferences.localizedContext(context)
        val db = AppDatabase.getInstance(context)
        val locale = Locale.forLanguageTag(LanguagePreferences.get(context).code)
        val number = NumberFormat.getIntegerInstance(locale)
        if (todayIds.isNotEmpty()) {
            val start = DateUtils.todayStart()
            val steps = db.stepDao().getForDay(start)?.steps ?: 0
            val workouts = db.workoutDao().getForDay(start, DateUtils.dayEnd(start)).first()
            val views = RemoteViews(context.packageName, R.layout.widget_summary).apply {
                setTextViewText(R.id.widget_title, ctx.getString(R.string.widget_today))
                setTextViewText(R.id.widget_value, number.format(steps))
                setTextViewText(R.id.widget_unit, ctx.getString(R.string.widget_steps))
                setTextViewText(R.id.widget_detail, ctx.getString(R.string.widget_day_summary, workouts.size, workouts.sumOf { it.durationSeconds } / 60))
                setOnClickPendingIntent(R.id.widget_root, open(context, "home"))
            }
            todayIds.forEach { manager.updateAppWidget(it, views) }
        }
        if (sleepIds.isNotEmpty()) {
            val sleep = db.sleepDao().getLatest().first()
            val duration = sleep?.let { ((it.wakeTime - it.bedTime) / 1000).coerceAtLeast(0) }
            val views = RemoteViews(context.packageName, R.layout.widget_sleep).apply {
                setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_sleep_background)
                setImageViewResource(R.id.widget_icon, R.drawable.ms_bedtime)
                setInt(R.id.widget_icon, "setColorFilter", ctx.getColor(R.color.widget_on_sleep))
                setTextViewText(R.id.widget_title, ctx.getString(R.string.widget_sleep))
                setTextViewText(R.id.widget_value, duration?.let { ctx.getString(R.string.widget_sleep_time, it / 3600, (it % 3600) / 60) } ?: "—")
                setTextViewText(R.id.widget_unit, ctx.getString(R.string.widget_sleep_latest))
                setTextViewText(R.id.widget_detail, sleep?.let { DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(it.wakeTime)) }
                    ?: ctx.getString(R.string.widget_sleep_empty))
                setOnClickPendingIntent(R.id.widget_root, open(context, "sleep"))
            }
            sleepIds.forEach { manager.updateAppWidget(it, views) }
        }
        if (sessionIds.isNotEmpty()) {
            val engine = withContext(Dispatchers.Main.immediate) { WorkoutSessionEngine.get(context) }
            withTimeout(5000) { engine.ready.first { it } }
            val state = engine.liveState.value
            LiveWorkoutStore.hydrate(context)
            val gps = LiveWorkoutStore.state.value
            val useGps = !state.active && gps.active
            val views = RemoteViews(context.packageName, R.layout.widget_session)
            if (useGps) {
                val openGps = PendingIntent.getActivity(context, 4365, Intent(context, LiveWorkoutActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setTextViewText(R.id.widget_title, ctx.getString(if (gps.phase == LivePhase.PAUSED) R.string.timer_paused else R.string.widget_gps))
                val running = gps.phase == LivePhase.ACTIVE
                setClock(views, running, SystemClock.elapsedRealtime() - gps.elapsedMs, false, DateUtils.formatDuration(gps.elapsedMs / 1000))
                views.setTextViewText(R.id.widget_detail, ctx.getString(R.string.widget_distance, String.format(locale, "%.2f", gps.distanceMeters / 1000)))
                views.setTextViewText(R.id.widget_primary, ctx.getString(R.string.widget_open))
                views.setViewVisibility(R.id.widget_secondary, View.GONE)
                views.setOnClickPendingIntent(R.id.widget_primary, openGps)
                views.setOnClickPendingIntent(R.id.widget_root, openGps)
            } else renderSession(ctx, views, state)
            sessionIds.forEach { manager.updateAppWidget(it, views) }
        }
    }

    private fun renderSession(context: Context, views: RemoteViews, state: SessionDisplay) {
        val open = WorkoutTimerService.openWorkout(context)
        views.setOnClickPendingIntent(R.id.widget_root, open)
        views.setTextViewText(R.id.widget_title, if (state.active) state.label(context) else context.getString(R.string.widget_session))
        setClock(views, state.active && !state.paused, state.clockBase, state.resting,
            if (state.active) DateUtils.formatDuration(state.seconds()) else context.getString(R.string.widget_ready))
        views.setTextViewText(R.id.widget_detail, if (state.active) {
            if (state.timingSet) state.exerciseName else if (state.totalSets > 0) context.getString(R.string.widget_sets, state.completedSets, state.totalSets)
            else state.name(context)
        } else context.getString(R.string.widget_session_hint))
        views.setTextViewText(R.id.widget_primary, context.getString(when {
            !state.active -> R.string.widget_open; state.paused -> R.string.timer_resume; else -> R.string.timer_pause
        }))
        views.setOnClickPendingIntent(R.id.widget_primary, if (state.active) WorkoutTimerService.timerAction(context,
            if (state.paused) "resume" else "pause", state.token) else open)
        views.setViewVisibility(R.id.widget_secondary, if (state.active) View.VISIBLE else View.GONE)
        if (state.active) {
            views.setTextViewText(R.id.widget_secondary, context.getString(if (state.resting) R.string.timer_skip else R.string.timer_finish))
            views.setOnClickPendingIntent(R.id.widget_secondary, if (state.resting) WorkoutTimerService.timerAction(context, "skip", state.token)
                else WorkoutTimerService.openWorkout(context, state.token))
        }
    }

    private fun setClock(views: RemoteViews, running: Boolean, base: Long, countdown: Boolean, frozen: String) {
        views.setChronometerCountDown(R.id.widget_clock, countdown)
        views.setChronometer(R.id.widget_clock, base, null, running)
        views.setViewVisibility(R.id.widget_clock, if (running) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_value, if (running) View.GONE else View.VISIBLE)
        views.setTextViewText(R.id.widget_value, frozen)
    }
    private fun open(context: Context, tab: String): PendingIntent = PendingIntent.getActivity(context, 4360 + when (tab) { "sleep" -> 1; else -> 0 },
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("shortcut_action", tab),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

open class LeafWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context)
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) = refresh(context)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in setOf(Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_MY_PACKAGE_REPLACED)) refresh(context)
    }
    private fun refresh(context: Context) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { withTimeout(8000) { LeafWidgets.update(context.applicationContext) } }
            catch (_: Exception) { }
            finally { pending.finish() }
        }
    }
}
class TodayWidget : LeafWidgetProvider()
class SleepWidget : LeafWidgetProvider()
class SessionWidget : LeafWidgetProvider()
