package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.ui.screens.workout.WorkoutResultScreen
import com.example.fitnesstracker.utils.*
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun WeekRecordsSheet(monday: LocalDate, onClose: () -> Unit) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context) }
    val start = monday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val end = monday.plusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val workouts by remember(start) { db.workoutDao().getForDay(start, end) }.collectAsLeafState(emptyList())
    val sleeps by remember(start) { db.sleepDao().getBetween(start, end) }.collectAsLeafState(emptyList())
    val meals by remember(start) { db.mealDao().getMealsForDay(start, end) }.collectAsLeafState(emptyList())
    val weights by remember(db) { db.extrasDao().getWeightHistory() }.collectAsLeafState(emptyList())
    val notes by remember(db) { db.extrasDao().getAllNotes() }.collectAsLeafState(emptyList())
    val life by remember(context) { LifeStore.get(context) }.state.collectAsLeafState()
    var selected by remember { mutableStateOf<Workout?>(null) }
    val record = selected
    if (record != null) { WorkoutResultScreen(record, onClose = { selected = null }, showRecords = false); return }
    val weekWeights = weights.filter { it.date in start until end }
    val weekNotes = notes.filter { it.dayStart in start until end }
    val checks = life.progress.filter { it.date in start until end }
    LeafEditSheet(onDismissRequest = onClose, title = { Text(stringResource(R.string.b5_week_records)) }, content = {
        Text(DateUtils.formatDate(start) + " — " + DateUtils.formatDate(end - 1), style = MaterialTheme.typography.labelMedium)
        if (workouts.isEmpty() && sleeps.isEmpty() && meals.isEmpty() && weekWeights.isEmpty() && weekNotes.isEmpty() && checks.isEmpty())
            FeatureEmpty(stringResource(R.string.b5_week_records), stringResource(R.string.b5_week_empty), LeafIcons.CalendarViewWeek)
        if (workouts.isNotEmpty()) FeatureSection(stringResource(R.string.album_workouts), icon = LeafIcons.Exercise, tone = LeafTone.Secondary) {
            workouts.sortedBy { it.startTime }.forEach { workout ->
                LeafTonalButton(onClick = { selected = workout }, modifier = Modifier.fillMaxWidth()) {
                    Text(DateUtils.formatDate(workout.startTime) + " · " + DateUtils.formatDuration(workout.durationSeconds))
                }
            }
        }
        if (sleeps.isNotEmpty()) FeatureSection(stringResource(R.string.album_sleep), icon = LeafIcons.Bedtime, tone = LeafTone.Tertiary) {
            sleeps.forEach { Text(DateUtils.formatDate(it.wakeTime) + " · " + DateUtils.formatTime(it.bedTime) + "–" + DateUtils.formatTime(it.wakeTime), style = MaterialTheme.typography.bodyMedium) }
        }
        if (meals.isNotEmpty()) FeatureSection(stringResource(R.string.album_meals), icon = LeafIcons.Nutrition, tone = LeafTone.Primary) {
            meals.groupBy { DateUtils.dayStart(it.date) }.forEach { (day, rows) ->
                Text(DateUtils.formatDate(day), style = MaterialTheme.typography.labelLarge)
                rows.forEach { Text(it.name + " · " + it.calories.toInt() + " " + stringResource(R.string.unit_kcal), style = MaterialTheme.typography.bodyMedium) }
            }
        }
        if (weekWeights.isNotEmpty() || checks.isNotEmpty()) FeatureSection(stringResource(R.string.progress_story), icon = LeafIcons.MonitorWeight, tone = LeafTone.Primary) {
            weekWeights.forEach { Text(DateUtils.formatDate(it.date) + " · " + it.weightKg + " " + stringResource(R.string.unit_kg)) }
            checks.forEach { check ->
                Text(DateUtils.formatDate(check.date), style = MaterialTheme.typography.labelLarge)
                check.photo?.let { coil.compose.AsyncImage(java.io.File(it), stringResource(R.string.progress_photo), Modifier.fillMaxWidth().height(160.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit) }
                val values = listOfNotNull(check.chest?.let { stringResource(R.string.progress_chest) + " $it" },
                    check.waist?.let { stringResource(R.string.progress_waist) + " $it" }, check.hips?.let { stringResource(R.string.progress_hips) + " $it" },
                    check.bicep?.let { stringResource(R.string.profile_bicep) + " $it" }) + check.measurements.map { "${it.key} · ${it.value}" }
                if (values.isNotEmpty()) Text(values.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (weekNotes.isNotEmpty()) FeatureSection(stringResource(R.string.b5_week_notes), icon = LeafIcons.EditNote, tone = LeafTone.Tertiary) {
            weekNotes.forEach { Text(DateUtils.formatDate(it.dayStart), style = MaterialTheme.typography.labelLarge); Text(it.text, style = MaterialTheme.typography.bodyMedium) }
        }
    }, confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.common_close)) } })
}
