package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.R

data class MetricField(
    val key: String,
    val labelRes: Int,
    val unitRes: Int,
    val decimal: Boolean = false
)

object WorkoutMetrics {
    fun fieldsFor(typeKey: String): List<MetricField> = when (typeKey) {
        "run", "walk" -> listOf(
            MetricField("distance", R.string.metric_distance, R.string.unit_km, decimal = true),
            MetricField("avgPace", R.string.metric_pace, R.string.unit_min_km, decimal = true)
        )
        "cycle" -> listOf(
            MetricField("distance", R.string.metric_distance, R.string.unit_km, decimal = true),
            MetricField("avgSpeed", R.string.metric_speed, R.string.unit_kmh, decimal = true)
        )
        "gym" -> listOf(
            MetricField("exercises", R.string.metric_exercises, R.string.unit_count),
            MetricField("sets", R.string.metric_sets, R.string.unit_count),
            MetricField("totalWeight", R.string.metric_weight_lifted, R.string.unit_kg, decimal = true)
        )
        "yoga" -> listOf(
            MetricField("poses", R.string.metric_poses, R.string.unit_count),
            MetricField("difficulty", R.string.metric_difficulty, R.string.unit_level)
        )
        "sport" -> listOf(
            MetricField("score", R.string.metric_score, R.string.unit_points),
            MetricField("throws", R.string.metric_throws, R.string.unit_count)
        )
        else -> listOf(
            MetricField("reps", R.string.metric_reps, R.string.unit_count),
            MetricField("sets", R.string.metric_sets, R.string.unit_count)
        )
    }
}
