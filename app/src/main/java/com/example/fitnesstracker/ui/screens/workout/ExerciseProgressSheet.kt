package com.example.fitnesstracker.ui.screens.workout

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.workout.*
import java.util.Locale

@Composable
internal fun ExerciseProgressSheet(name: String, mode: EntryMode, history: List<Workout>, onClose: () -> Unit) {
    var weight by rememberSaveable(name) { mutableStateOf(true) }
    val sessions = remember(name, mode, history) {
        history.mapNotNull { workout ->
            val sets = WorkoutJournal.decode(workout.metrics)?.exercises
                ?.filter { exerciseKey(it.name, it.mode) == exerciseKey(name, mode) }
                ?.flatMap { it.sets }?.filter { it.completed && it.hasResult(mode) }.orEmpty()
            if (sets.isEmpty()) null else workout.startTime to sets
        }.sortedBy { it.first }
    }
    val hasWeight = sessions.any { row -> row.second.any { (it.weightKg ?: 0f) > 0f } }
    val values = remember(sessions, mode, weight) { sessions.map { (_, sets) ->
        when (mode) {
            EntryMode.REPS -> if (weight && hasWeight) sets.maxOf { it.weightKg ?: 0f } else sets.maxOf { it.reps ?: 0 }.toFloat()
            EntryMode.TIME -> sets.maxOf { it.seconds ?: 0 }.toFloat()
            EntryMode.DISTANCE -> sets.maxOf { it.distanceMeters ?: 0f } / 1000f
            EntryMode.SCORE -> sets.maxOf { it.score ?: 0 }.toFloat()
        }
    } }
    val unit = stringResource(when (mode) {
        EntryMode.REPS -> if (weight && hasWeight) R.string.unit_kg else R.string.journal_reps_unit
        EntryMode.TIME -> R.string.journal_seconds
        EntryMode.DISTANCE -> R.string.live_km
        EntryMode.SCORE -> R.string.journal_score
    })
    val color = MaterialTheme.colorScheme.primary
    LeafEditSheet(onDismissRequest = onClose, title = { Text(name) }, content = {
        Text(stringResource(R.string.exercise_progress) + " · " + unit, style = MaterialTheme.typography.titleMedium)
        if (mode == EntryMode.REPS && hasWeight) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(weight, { weight = true }, label = { Text(stringResource(R.string.unit_kg)) })
            FilterChip(!weight, { weight = false }, label = { Text(stringResource(R.string.journal_reps_unit)) })
        }
        if (values.isEmpty()) FeatureEmpty(stringResource(R.string.exercise_progress), stringResource(R.string.exercise_no_results), LeafIcons.ShowChart)
        if (values.size == 1) Text(stringResource(R.string.design_progress_one), style = MaterialTheme.typography.bodyMedium)
        if (values.isNotEmpty()) Text(String.format(Locale.getDefault(), "%.1f %s", values.maxOrNull(), unit),
            style = MaterialTheme.typography.headlineMedium)
        if (sessions.size >= 2) Canvas(Modifier.fillMaxWidth().height(140.dp).padding(6.dp)) {
            val top = values.maxOrNull()!!.coerceAtLeast(1f)
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = size.width * index / (values.size - 1)
                val y = size.height - size.height * value / top
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(color, 3.dp.toPx(), Offset(x, y))
            }
            drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
        var visible by rememberSaveable { mutableIntStateOf(30) }
        sessions.indices.reversed().take(visible).forEach { i ->
            FeatureSection(DateUtils.formatDate(sessions[i].first)) {
                Text(String.format(Locale.getDefault(), "%.1f %s", values[i], unit), style = MaterialTheme.typography.headlineSmall)
            }
        }
        if (visible < sessions.size) TextButton(onClick = { visible += 30 }) { Text(stringResource(R.string.common_show_more)) }
    }, confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.common_close)) } })
}
