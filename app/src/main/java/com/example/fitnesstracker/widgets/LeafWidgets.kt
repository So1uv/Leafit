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
    val providers = listOf(TodayWidget::class.java, SleepWidget::class.java, SessionWidget::class.java, WaterWidget::class.java, NutritionWidget::class.java, WeekWidget::class.java)

    @Synchronized fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        observer = object : InvalidationTracker.Observer("step_records", "workouts", "sleep_records", "water_records", "meals", "user_profile") {
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
        val waterIds = manager.getAppWidgetIds(ComponentName(context, WaterWidget::class.java))
        val nutritionIds = manager.getAppWidgetIds(ComponentName(context, NutritionWidget::class.java))
        val weekIds = manager.getAppWidgetIds(ComponentName(context, WeekWidget::class.java))
        if (listOf(todayIds, sleepIds, sessionIds, waterIds, nutritionIds, weekIds).all { it.isEmpty() }) return@withLock
        val ctx = LanguagePreferences.localizedContext(context)
        val db = AppDatabase.getInstance(context)
        val locale = Locale.forLanguageTag(LanguagePreferences.get(context).code)
        val number = NumberFormat.getIntegerInstance(locale)
        val start = DateUtils.todayStart()
        val end = DateUtils.dayEnd(start)
        if (todayIds.isNotEmpty()) {
            val steps = db.stepDao().getForDay(start)?.steps ?: 0
            val workouts = db.workoutDao().getForDay(start, end).first()
            val goal = com.example.fitnesstracker.utils.GoalPreferences.getStepsGoal(context).coerceAtLeast(1)
            val metric = Metric(title = ctx.getString(R.string.widget_today), icon = R.drawable.ms_steps,
                value = number.format(steps), compact = compact(steps, locale),
                unit = ctx.getString(R.string.b5_widget_goal, number.format(goal), ctx.getString(R.string.widget_steps)),
                detail = ctx.getString(R.string.widget_day_summary, workouts.size, workouts.sumOf { it.durationSeconds } / 60),
                footer = DateUtils.formatDate(start), progress = steps, goal = goal, tab = "home")
            todayIds.forEach { publish(context, manager, it, metric) }
        }
        if (sleepIds.isNotEmpty()) {
            val sleep = db.sleepDao().getLatest().first()
            val duration = sleep?.let { ((it.wakeTime - it.bedTime) / 1000).coerceAtLeast(0) }
            val goal = com.example.fitnesstracker.utils.GoalPreferences.getSleepGoalMinutes(context).coerceAtLeast(1)
            val metric = Metric(title = ctx.getString(R.string.widget_sleep), icon = R.drawable.ms_bedtime,
                value = duration?.let { ctx.getString(R.string.widget_sleep_time, it / 3600, (it % 3600) / 60) } ?: "—",
                compact = duration?.let { "%d:%02d".format(it / 3600, (it % 3600) / 60) } ?: "—",
                unit = ctx.getString(R.string.b5_widget_sleep_goal, ctx.getString(R.string.widget_sleep_time, goal.toLong() / 60, goal.toLong() % 60)),
                detail = sleep?.let { DateUtils.formatTime(it.bedTime) + " — " + DateUtils.formatTime(it.wakeTime) } ?: ctx.getString(R.string.widget_sleep_empty),
                footer = sleep?.let { DateUtils.formatDate(it.wakeTime) + " · " + ctx.getString(R.string.sleep_timeline_quality, it.qualityScore) },
                progress = ((duration ?: 0L) / 60).toInt(), goal = if (duration != null) goal else 0, tab = "sleep", defaultTone = 1)
            sleepIds.forEach { publish(context, manager, it, metric) }
        }
        if (waterIds.isNotEmpty()) {
            val rows = db.waterDao().getForDay(start, end).first()
            val total = rows.sumOf { it.amountMl }
            val goal = com.example.fitnesstracker.utils.GoalPreferences.getWaterGoal(context).coerceAtLeast(1)
            val metric = Metric(title = ctx.getString(R.string.b5_widget_water), icon = R.drawable.ms_water_full,
                value = number.format(total),
                compact = if (total >= 1000) String.format(locale, "%.1f", total / 1000f) + " " + ctx.getString(R.string.unit_l) else total.toString(),
                unit = ctx.getString(R.string.b5_widget_goal, number.format(goal), ctx.getString(R.string.unit_ml)),
                detail = ctx.getString(R.string.b5_widget_water_detail, rows.size, (goal - total).coerceAtLeast(0)),
                footer = DateUtils.formatDate(start), progress = total, goal = goal, tab = "water", action = true)
            waterIds.forEach { publish(context, manager, it, metric) }
        }
        if (nutritionIds.isNotEmpty()) {
            val meals = db.mealDao().getMealsForDay(start, end).first()
            val profile = db.userProfileDao().getProfile().first()
            val goal = profile?.let { com.example.fitnesstracker.utils.HealthCalc.dailyCalories(it.weightKg, it.heightCm, it.age, it.gender).toInt() } ?: 0
            val total = meals.sumOf { it.calories.toDouble() }.toInt()
            val metric = Metric(title = ctx.getString(R.string.b5_widget_nutrition), icon = R.drawable.ms_nutrition,
                value = number.format(total), compact = compact(total, locale),
                unit = if (goal > 0) ctx.getString(R.string.b5_widget_goal, number.format(goal), ctx.getString(R.string.unit_kcal)) else ctx.getString(R.string.unit_kcal),
                detail = ctx.getString(R.string.b5_widget_macros, meals.size, meals.sumOf { it.proteins.toDouble() }.toInt(),
                    meals.sumOf { it.fats.toDouble() }.toInt(), meals.sumOf { it.carbs.toDouble() }.toInt()),
                footer = meals.maxByOrNull { it.date }?.name ?: ctx.getString(R.string.b5_widget_nutrition_hint),
                progress = total, goal = goal, tab = "water")
            nutritionIds.forEach { publish(context, manager, it, metric) }
        }
        if (weekIds.isNotEmpty()) {
            val week = DateUtils.weekStart()
            val records = db.stepDao().getBetween(week, end).first()
            val workouts = db.workoutDao().getForDay(week, end).first()
            val total = records.sumOf { it.steps }
            val goal = com.example.fitnesstracker.utils.GoalPreferences.getStepsGoal(context).coerceAtLeast(1) * 7
            val metric = Metric(title = ctx.getString(R.string.b5_widget_week), icon = R.drawable.ms_calendar_view_week,
                value = number.format(total), compact = compact(total, locale), unit = ctx.getString(R.string.widget_steps),
                detail = ctx.getString(R.string.widget_day_summary, workouts.size, workouts.sumOf { it.durationSeconds } / 60),
                footer = DateUtils.formatDate(week) + " — " + DateUtils.formatDate(start),
                progress = total, goal = goal, tab = "home", chart = weekBars(context, records))
            weekIds.forEach { publish(context, manager, it, metric) }
        }
        if (sessionIds.isNotEmpty()) {
            val engine = withContext(Dispatchers.Main.immediate) { WorkoutSessionEngine.get(context) }
            withTimeout(5000) { engine.ready.first { it } }
            val state = engine.liveState.value
            LiveWorkoutStore.hydrate(context)
            val gps = LiveWorkoutStore.state.value
            val useGps = !state.active && gps.active
            val latest = if (!useGps && !state.active) db.workoutDao().getAll().first().firstOrNull() else null
            fun build(layout: Int) = RemoteViews(context.packageName, layout).also { views ->
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
                } else {
                    renderSession(ctx, views, state)
                    if (latest != null) {
                        views.setTextViewText(R.id.widget_title, ctx.getString(R.string.b5_widget_last_session))
                        views.setTextViewText(R.id.widget_value, DateUtils.formatDuration(latest.durationSeconds))
                        views.setTextViewText(R.id.widget_detail, DateUtils.formatDate(latest.startTime) + " · " +
                            ctx.getString(com.example.fitnesstracker.workout.WorkoutCatalog.forType(latest.type).first().labelRes))
                    }
                }
            }
            sessionIds.forEach { id ->
                fun sized(slot: Slot) = build(when (slot) {
                    Slot.XS, Slot.XS_TALL, Slot.COLUMN -> R.layout.widget_session_xs
                    Slot.STRIP, Slot.STRIP_WIDE, Slot.STRIP_XL -> R.layout.widget_session_strip
                    else -> R.layout.widget_session
                }).apply {
                    decorate(context, id, this, session = true)
                    if (slot == Slot.STRIP) setViewVisibility(R.id.widget_secondary, View.GONE)
                    setViewVisibility(R.id.widget_detail, if (slot.tall && WidgetPreferences.details(context, id)) View.VISIBLE else View.GONE)
                }
                deliver(manager, id, SESSION_SLOTS) { sized(it) }
            }
        }
    }

    private class Metric(
        val title: String, val icon: Int, val value: String, val compact: String, val unit: String,
        val detail: String?, val footer: String?, val progress: Int, val goal: Int, val tab: String,
        val chart: android.graphics.Bitmap? = null, val action: Boolean = false, val defaultTone: Int = 0
    )

    private enum class Slot(val w: Float, val h: Float, val tall: Boolean = false) {
        XS(40f, 40f), XS_TALL(40f, 84f), COLUMN(40f, 170f),
        STRIP(96f, 40f), STRIP_WIDE(200f, 40f), STRIP_XL(290f, 40f),
        CARD(120f, 124f), CARD_TALL(120f, 220f, true),
        WIDE(200f, 124f), WIDE_TALL(200f, 196f, true)
    }

    private class SlotSize(val slot: Slot, val w: Float, val h: Float)

    private val METRIC_SLOTS: List<SlotSize> by lazy { Slot.values().map { SlotSize(it, it.w, it.h) } }
    private val SESSION_SLOTS: List<SlotSize> by lazy {
        listOf(SlotSize(Slot.XS, 40f, 40f), SlotSize(Slot.XS_TALL, 40f, 84f), SlotSize(Slot.STRIP, 96f, 40f),
            SlotSize(Slot.STRIP_WIDE, 200f, 40f), SlotSize(Slot.CARD, 180f, 140f), SlotSize(Slot.CARD_TALL, 180f, 220f))
    }

    private fun deliver(manager: AppWidgetManager, id: Int, slots: List<SlotSize>, render: (Slot) -> RemoteViews) {
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            manager.updateAppWidget(id, RemoteViews(slots.associate { android.util.SizeF(it.w, it.h) to render(it.slot) }))
        } else {
            val options = manager.getAppWidgetOptions(id)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 150).toFloat()
            val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 160).toFloat()
            val fit = slots.filter { it.w <= width && it.h <= height }.maxByOrNull { it.w * it.h } ?: slots.first()
            manager.updateAppWidget(id, render(fit.slot))
        }
    }

    private fun publish(context: Context, manager: AppWidgetManager, id: Int, metric: Metric) {
        val ctx = LanguagePreferences.localizedContext(context)
        val details = WidgetPreferences.details(context, id)
        fun sized(slot: Slot): RemoteViews {
            val layout = when (slot) {
                Slot.XS, Slot.XS_TALL -> R.layout.widget_xs
                Slot.COLUMN -> R.layout.widget_column
                Slot.STRIP, Slot.STRIP_WIDE, Slot.STRIP_XL -> R.layout.widget_strip
                Slot.CARD, Slot.CARD_TALL -> R.layout.widget_summary
                Slot.WIDE -> R.layout.widget_wide
                Slot.WIDE_TALL -> R.layout.widget_large
            }
            return RemoteViews(context.packageName, layout).apply {
                setTextViewText(R.id.widget_title, metric.title)
                setTextViewText(R.id.widget_value, metric.value)
                setTextViewText(R.id.widget_compact, metric.compact)
                setTextViewText(R.id.widget_unit, metric.unit)
                setTextViewText(R.id.widget_detail, metric.detail.orEmpty())
                setTextViewText(R.id.widget_footer, metric.footer.orEmpty())
                setImageViewResource(R.id.widget_icon, metric.icon)
                setOnClickPendingIntent(R.id.widget_root, open(context, metric.tab))
                val hasGoal = metric.goal > 0
                val share = if (hasGoal) (metric.progress.toLong() * 100 / metric.goal).coerceIn(0, 999).toInt() else 0
                setProgressBar(R.id.widget_ring, 100, share.coerceAtMost(100), false)
                setProgressBar(R.id.widget_progress, 100, share.coerceAtMost(100), false)
                setViewVisibility(R.id.widget_ring, if (hasGoal) View.VISIBLE else View.INVISIBLE)
                setTextViewText(R.id.widget_percent, "$share%")
                val showPercent = hasGoal && slot != Slot.STRIP && slot != Slot.COLUMN
                val showDetail = details && when (slot) {
                    Slot.STRIP_XL, Slot.CARD_TALL, Slot.WIDE, Slot.WIDE_TALL -> true
                    Slot.STRIP_WIDE -> !metric.action
                    else -> false
                }
                val showAction = metric.action && slot != Slot.XS && slot != Slot.XS_TALL && slot != Slot.STRIP
                setViewVisibility(R.id.widget_percent, if (showPercent) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_detail, if (showDetail) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_footer, if (details && (slot.tall || slot == Slot.WIDE)) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.widget_title, if (slot == Slot.XS) View.GONE else View.VISIBLE)
                setViewVisibility(R.id.widget_progress, if (hasGoal && metric.chart == null) View.VISIBLE else View.GONE)
                val chart = metric.chart
                if (chart != null && (slot == Slot.CARD || slot == Slot.CARD_TALL)) {
                    setImageViewBitmap(R.id.widget_chart, chart); setViewVisibility(R.id.widget_chart, View.VISIBLE)
                } else setViewVisibility(R.id.widget_chart, View.GONE)
                if (chart != null && (slot == Slot.WIDE || slot == Slot.WIDE_TALL)) {
                    setImageViewBitmap(R.id.widget_chart_inline, chart)
                    setViewVisibility(R.id.widget_chart_inline, View.VISIBLE)
                    setViewVisibility(R.id.widget_ring_box, View.GONE)
                } else {
                    setViewVisibility(R.id.widget_chart_inline, View.GONE)
                    setViewVisibility(R.id.widget_ring_box, if (hasGoal) View.VISIBLE else View.GONE)
                }
                setViewVisibility(R.id.widget_action, if (showAction) View.VISIBLE else View.GONE)
                if (showAction) {
                    val volume = WidgetPreferences.water(context, id)
                    setTextViewText(R.id.widget_action, if (slot == Slot.COLUMN || slot == Slot.STRIP_WIDE) "+$volume"
                        else "+$volume " + ctx.getString(R.string.unit_ml))
                    setOnClickPendingIntent(R.id.widget_action, PendingIntent.getBroadcast(context, id,
                        Intent(context, WaterWidget::class.java).setAction("com.example.fitnesstracker.ADD_WIDGET_WATER")
                            .setData(android.net.Uri.parse("leafit://water/$id"))
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                }
                decorate(context, id, this, metric.defaultTone)
            }
        }
        deliver(manager, id, METRIC_SLOTS) { sized(it) }
    }

    private fun compact(n: Int, locale: Locale): String = when {
        n >= 100_000 -> String.format(locale, "%.0fk", n / 1000f)
        n >= 10_000 -> String.format(locale, "%.1fk", n / 1000f).replace(Regex("[.,]0k$"), "k")
        else -> NumberFormat.getIntegerInstance(locale).format(n)
    }

    private fun decorate(context: Context, id: Int, views: RemoteViews, defaultTone: Int = 0, session: Boolean = false) {
        val tone = WidgetPreferences.tone(context, id, defaultTone)
        val ink = context.getColor(when (tone) { 1 -> R.color.widget_on_sleep; 2 -> R.color.widget_text; else -> R.color.widget_on_primary })
        views.setInt(R.id.widget_root, "setBackgroundResource", when (tone) {
            1 -> R.drawable.widget_sleep_background; 2 -> R.drawable.widget_neutral_background; else -> R.drawable.widget_green_background
        })
        val textIds = if (session) listOf(R.id.widget_title, R.id.widget_value, R.id.widget_clock, R.id.widget_detail, R.id.widget_secondary)
            else listOf(R.id.widget_title, R.id.widget_value, R.id.widget_unit, R.id.widget_detail, R.id.widget_footer, R.id.widget_percent)
        textIds.forEach { views.setTextColor(it, ink) }
        views.setTextColor(R.id.widget_compact, ink)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            val bar = android.content.res.ColorStateList.valueOf(ink)
            val track = android.content.res.ColorStateList.valueOf(androidx.core.graphics.ColorUtils.setAlphaComponent(ink, 0x33))
            listOf(R.id.widget_ring, R.id.widget_progress).forEach {
                views.setColorStateList(it, "setProgressTintList", bar)
                views.setColorStateList(it, "setProgressBackgroundTintList", track)
            }
        }
        views.setInt(R.id.widget_icon, "setColorFilter", ink)
        views.setInt(R.id.widget_config, "setColorFilter", ink)
        views.setOnClickPendingIntent(R.id.widget_config, PendingIntent.getActivity(context, id,
            Intent(context, WidgetConfigurationActivity::class.java).setData(android.net.Uri.parse("leafit://widget/$id"))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    private fun percent(views: RemoteViews, value: Int, goal: Int) {
        if (goal <= 0) { views.setViewVisibility(R.id.widget_percent, View.GONE); return }
        views.setTextViewText(R.id.widget_percent, "${(value.toLong() * 100 / goal).coerceIn(0, 999)}%")
        views.setViewVisibility(R.id.widget_percent, View.VISIBLE)
    }

    private fun weekBars(context: Context, records: List<com.example.fitnesstracker.data.entities.StepRecord>): android.graphics.Bitmap {
        val w = 840; val h = 200
        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val zone = java.time.ZoneId.systemDefault()
        val monday = java.time.Instant.ofEpochMilli(DateUtils.weekStart()).atZone(zone).toLocalDate()
        val today = java.time.LocalDate.now(zone)
        val days = records.associate { java.time.Instant.ofEpochMilli(it.dayStart).atZone(zone).toLocalDate() to it.steps }
        val max = days.values.maxOrNull()?.coerceAtLeast(1) ?: 1
        val ink = context.getColor(R.color.widget_on_primary)
        val locale = Locale.forLanguageTag(LanguagePreferences.get(context).code)
        val slot = w / 7f; val bar = slot * .62f; val top = 8f; val bottom = 148f; val radius = bar / 2f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.textSize = 30f
        repeat(7) { index ->
            val day = monday.plusDays(index.toLong())
            val x = index * slot + (slot - bar) / 2f
            paint.color = context.getColor(R.color.widget_track)
            canvas.drawRoundRect(x, top, x + bar, bottom, radius, radius, paint)
            val value = days[day] ?: 0
            if (value > 0) {
                paint.color = if (day == today) ink else androidx.core.graphics.ColorUtils.setAlphaComponent(ink, 140)
                val height = ((bottom - top) * value / max).coerceAtLeast(bar)
                canvas.drawRoundRect(x, bottom - height, x + bar, bottom, radius, radius, paint)
            }
            paint.color = if (day == today) ink else androidx.core.graphics.ColorUtils.setAlphaComponent(ink, 170)
            paint.isFakeBoldText = day == today
            val label = day.format(java.time.format.DateTimeFormatter.ofPattern("EEEEE", locale))
            canvas.drawText(label, x + bar / 2f, 192f, paint)
        }
        paint.isFakeBoldText = false
        return bitmap
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
    private fun open(context: Context, tab: String): PendingIntent = PendingIntent.getActivity(context, 4360 + when (tab) { "sleep" -> 1; "water" -> 2; else -> 0 },
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("shortcut_action", tab),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

open class LeafWidgetProvider : AppWidgetProvider() {
    override fun onDeleted(context: Context, appWidgetIds: IntArray) { WidgetPreferences.remove(context, appWidgetIds) }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context)
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) = refresh(context)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.fitnesstracker.ADD_WIDGET_WATER" && this is WaterWidget) {
            val pending = goAsync()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    withTimeout(8000) {
                        val db = AppDatabase.getInstance(context)
                        db.waterDao().insert(com.example.fitnesstracker.data.entities.WaterRecord(amountMl = WidgetPreferences.water(context,
                            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))))
                        val total = db.waterDao().getForDay(DateUtils.todayStart(), DateUtils.dayEnd(DateUtils.todayStart())).first().sumOf { it.amountMl }
                        com.example.fitnesstracker.utils.WaterReminder.onWaterChanged(context, total, com.example.fitnesstracker.utils.GoalPreferences.getWaterGoal(context))
                        LeafWidgets.update(context.applicationContext)
                    }
                } catch (_: Exception) { LeafWidgets.requestUpdate(context.applicationContext) }
                finally { pending.finish() }
            }
        }
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

class WaterWidget : LeafWidgetProvider()
class NutritionWidget : LeafWidgetProvider()
class WeekWidget : LeafWidgetProvider()
