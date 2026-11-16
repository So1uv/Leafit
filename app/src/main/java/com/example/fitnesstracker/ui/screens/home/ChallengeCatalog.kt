package com.example.fitnesstracker.ui.screens.home

import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.*
import com.example.fitnesstracker.utils.*
import java.time.Instant
import java.time.ZoneId

internal data class ChallengeSpec(val id: String, val title: Int, val metric: String,
    val goal: Int, val threshold: Int = 0, val days: Int = 7, val category: Int = 0)

internal object ChallengeCatalog {
    val entries = listOf(
        ChallengeSpec("first", R.string.challenge_first, "workouts", 1, category = 1),
        ChallengeSpec("gentle", R.string.challenge_gentle, "step_days", 3, 3000),
        ChallengeSpec("three", R.string.challenge_three, "workout_days", 3, category = 1),
        ChallengeSpec("walks", R.string.challenge_walks, "step_days", 7, 5000),
        ChallengeSpec("ten", R.string.challenge_ten, "step_days", 1, 10000),
        ChallengeSpec("distance", R.string.challenge_distance, "steps", 35000),
        ChallengeSpec("minutes", R.string.challenge_minutes, "minutes", 90, category = 1),
        ChallengeSpec("variety", R.string.challenge_variety, "types", 3, category = 1),
        ChallengeSpec("walk_15", R.string.b5_ch_walk15, "steps", 15000),
        ChallengeSpec("walk_50", R.string.b5_ch_walk50, "steps", 50000),
        ChallengeSpec("walk_100", R.string.b5_ch_walk100, "steps", 100000, days = 30),
        ChallengeSpec("walk_14", R.string.b5_ch_walk14, "step_days", 10, 4000, 14),
        ChallengeSpec("walk_8", R.string.b5_ch_walk8, "step_days", 4, 8000),
        ChallengeSpec("sessions_6", R.string.b5_ch_sessions6, "workouts", 6, days = 14, category = 1),
        ChallengeSpec("sessions_12", R.string.b5_ch_sessions12, "workouts", 12, days = 30, category = 1),
        ChallengeSpec("minutes_30", R.string.b5_ch_min30, "minutes", 30, category = 1),
        ChallengeSpec("minutes_150", R.string.b5_ch_min150, "minutes", 150, days = 14, category = 1),
        ChallengeSpec("minutes_300", R.string.b5_ch_min300, "minutes", 300, days = 30, category = 1),
        ChallengeSpec("km_5", R.string.b5_ch_km5, "kilometers", 5, category = 1),
        ChallengeSpec("km_20", R.string.b5_ch_km20, "kilometers", 20, days = 14, category = 1),
        ChallengeSpec("km_50", R.string.b5_ch_km50, "kilometers", 50, days = 30, category = 1),
        ChallengeSpec("sleep_3", R.string.b5_ch_sleep3, "sleep_days", 3, category = 2),
        ChallengeSpec("sleep_7", R.string.b5_ch_sleep7, "sleep_days", 7, days = 14, category = 2),
        ChallengeSpec("rest_3", R.string.b5_ch_rest3, "sleep_goal", 3, 420, category = 2),
        ChallengeSpec("rest_7", R.string.b5_ch_rest7, "sleep_goal", 7, 420, 14, 2),
        ChallengeSpec("water_3", R.string.b5_ch_water3, "water_days", 3, 1500, category = 3),
        ChallengeSpec("water_7", R.string.b5_ch_water7, "water_days", 7, 1500, 14, 3),
        ChallengeSpec("meals_3", R.string.b5_ch_meals3, "meal_days", 3, category = 3),
        ChallengeSpec("meals_7", R.string.b5_ch_meals7, "meal_days", 7, days = 14, category = 3),
        ChallengeSpec("notes_3", R.string.b5_ch_notes3, "note_days", 3, category = 4),
        ChallengeSpec("notes_7", R.string.b5_ch_notes7, "note_days", 7, days = 14, category = 4),
        ChallengeSpec("own", R.string.b5_ch_own, "manual", 7, category = 4)
    )
    val metrics = listOf("steps", "step_days", "workouts", "workout_days", "minutes", "kilometers", "types",
        "sleep_days", "sleep_goal", "water_days", "meal_days", "note_days", "manual")
    fun find(id: String?) = entries.firstOrNull { it.id == id }
    fun label(metric: String) = when(metric) {
        "steps" -> R.string.b5_metric_steps; "step_days" -> R.string.b5_metric_step_days
        "workouts" -> R.string.b5_metric_workouts; "workout_days" -> R.string.b5_metric_workout_days
        "minutes" -> R.string.b5_metric_minutes; "kilometers" -> R.string.b5_metric_kilometers
        "types" -> R.string.b5_metric_types; "sleep_days" -> R.string.b5_metric_sleep_days
        "sleep_goal" -> R.string.b5_metric_sleep_goal; "water_days" -> R.string.b5_metric_water_days
        "meal_days" -> R.string.b5_metric_meal_days; "note_days" -> R.string.b5_metric_note_days
        else -> R.string.b5_metric_manual
    }
    fun end(run: ChallengeRun): Long = Instant.ofEpochMilli(run.start).atZone(ZoneId.systemDefault())
        .plusDays(run.days.toLong()).toInstant().toEpochMilli()
    fun progress(run: ChallengeRun, steps: List<StepRecord>, workouts: List<Workout>, sleeps: List<SleepRecord>,
        water: List<WaterRecord>, meals: List<Meal>, notes: List<DayNote>, now: Long): Int {
        val until = minOf(end(run), now + 1)
        fun included(time: Long) = time >= run.start && time < until
        val stepDays = steps.filter { included(it.dayStart) }.groupBy { it.dayStart }
            .mapValues { (_, rows) -> rows.maxOf { it.steps }.coerceAtLeast(0) }
        val sessions = workouts.filter { included(it.startTime) && it.durationSeconds > 0 }
        val sleepDays = sleeps.filter { included(it.wakeTime) }.groupBy { DateUtils.dayStart(it.wakeTime) }
        val value: Long = when (run.metric) {
            "steps" -> stepDays.values.sumOf { it.toLong() }
            "step_days" -> stepDays.count { it.value >= run.threshold }.toLong()
            "workouts" -> sessions.size.toLong()
            "workout_days" -> sessions.map { DateUtils.dayStart(it.startTime) }.distinct().size.toLong()
            "minutes" -> sessions.sumOf { it.durationSeconds } / 60
            "kilometers" -> (sessions.sumOf { it.distanceMeters.coerceAtLeast(0f).toDouble() } / 1000).toLong()
            "types" -> sessions.map { it.type }.distinct().size.toLong()
            "sleep_days" -> sleepDays.size.toLong()
            "sleep_goal" -> sleepDays.count { (_, rows) -> rows.sumOf { (it.wakeTime - it.bedTime).coerceAtLeast(0) } / 60000 >= run.threshold }.toLong()
            "water_days" -> water.filter { included(it.timestamp) }.groupBy { DateUtils.dayStart(it.timestamp) }
                .count { (_, rows) -> rows.sumOf { it.amountMl.toLong() } >= run.threshold }.toLong()
            "meal_days" -> meals.filter { included(it.date) }.map { DateUtils.dayStart(it.date) }.distinct().size.toLong()
            "note_days" -> notes.filter { included(it.dayStart) && it.text.isNotBlank() }.map { it.dayStart }.distinct().size.toLong()
            else -> run.manual.toLong()
        }
        return value.coerceIn(0, run.goal.toLong()).toInt()
    }
}
