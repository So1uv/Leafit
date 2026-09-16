package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.workout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun WorkoutHistoryCard(workout: Workout, expanded: Boolean, onToggle: () -> Unit,
    groupIndex: Int, groupCount: Int, onDelete: (Workout) -> Unit,
    onUpdate: (Workout, (Boolean) -> Unit) -> Unit) {
    val isJournal = remember(workout.metrics) { workout.metrics.contains(WorkoutJournal.KIND) }
    val gps = remember(workout.metrics) { workout.metrics.contains("\"leaf_route_v1\"") }
    val journal by produceState<WorkoutPlan?>(null, workout.metrics) {
        value = if (isJournal) withContext(Dispatchers.Default) { WorkoutJournal.decode(workout.metrics) } else null
    }
    var edit by remember { mutableStateOf(false) }
    var remove by remember { mutableStateOf(false) }
    if (edit && (!isJournal || journal != null)) WorkoutRecordEditor(workout, journal, onUpdate) { edit = false }
    if (remove) AlertDialog(onDismissRequest = { remove = false }, title = { Text(stringResource(R.string.workout_delete_q)) },
        confirmButton = { TextButton(onClick = { onDelete(workout); remove = false }) { Text(stringResource(R.string.common_delete)) } },
        dismissButton = { TextButton(onClick = { remove = false }) { Text(stringResource(R.string.common_cancel)) } })
    val type = WorkoutType.fromKey(workout.type)
    val title = journal?.let { plan -> plan.title.ifBlank { stringResource(WorkoutCatalog.variant(plan).labelRes) } } ?: workoutTypeLabel(type)
    HistoryRecordCard(title = title, date = DateUtils.formatDate(workout.startTime) + " · " + DateUtils.formatTime(workout.startTime),
        icon = type.icon, badgeColor = MaterialTheme.colorScheme.secondaryContainer,
        badgeInk = MaterialTheme.colorScheme.onSecondaryContainer, expanded = expanded, onToggle = onToggle,
        groupIndex = groupIndex, groupCount = groupCount, onEdit = { edit = true }, onDelete = { remove = true }) {
        Text(DateUtils.formatDuration(workout.durationSeconds), style = MaterialTheme.typography.headlineSmall)
        if (gps) {
            Text("${"%.2f".format(workout.distanceMeters / 1000)} ${stringResource(R.string.live_km)}", style = MaterialTheme.typography.titleMedium)
            com.example.fitnesstracker.tracking.SavedWorkoutTelemetry(workout.metrics, workout.durationSeconds, workout.distanceMeters)
            com.example.fitnesstracker.tracking.SavedRouteButton(workout.id, workout.metrics)
        } else journal?.let { plan ->
            JournalSummary(plan, running = false, recorded = true)
            var openExercise by rememberSaveable(workout.id) { mutableStateOf<String?>(plan.exercises.firstOrNull()?.id) }
            plan.exercises.forEach { entry ->
                key(entry.id) { JournalExerciseCard(entry, openExercise == entry.id,
                    onToggle = { openExercise = if (openExercise == entry.id) null else entry.id },
                    editable = false, canComplete = false, onEdit = {}, onSet = {}) }
            }
        } ?: run {
            val values = remember(workout.metrics) { runCatching {
                val json = org.json.JSONObject(workout.metrics)
                WorkoutMetrics.fieldsFor(workout.type).mapNotNull { field -> json.optString(field.key).takeIf { it.isNotBlank() }?.let { field to it } }
            }.getOrDefault(emptyList()) }
            values.forEach { (field, value) -> Text("${stringResource(field.labelRes)}: $value ${stringResource(field.unitRes)}",
                style = MaterialTheme.typography.bodyMedium) }
            if (workout.steps > 0) Text("${workout.steps} ${stringResource(R.string.unit_steps_short)}", style = MaterialTheme.typography.bodyMedium)
        }
        if (workout.note.isNotBlank()) Text(workout.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WorkoutRecordEditor(workout: Workout, originalPlan: WorkoutPlan?,
    onSave: (Workout, (Boolean) -> Unit) -> Unit, onDismiss: () -> Unit) {
    val zone = java.time.ZoneId.systemDefault()
    val initial = remember(workout.id) { java.time.Instant.ofEpochMilli(workout.startTime).atZone(zone) }
    var day by rememberSaveable(workout.id) { mutableLongStateOf(DateUtils.dayStart(workout.startTime)) }
    var time by rememberSaveable(workout.id) { mutableIntStateOf(initial.hour * 60 + initial.minute) }
    var note by rememberSaveable(workout.id) { mutableStateOf(workout.note) }
    var plan by remember(workout.id) { mutableStateOf(originalPlan) }
    var title by rememberSaveable(workout.id) { mutableStateOf(originalPlan?.title.orEmpty()) }
    var choosingDate by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    if (choosingDate) LeafDatePicker(day, { day = it; choosingDate = false }, { choosingDate = false })
    val entry = editingExercise
    if (entry != null) {
        ExerciseEditorSheet(entry, onSave = { updated -> plan = plan?.copy(exercises = plan!!.exercises.map { if (it.id == updated.id) updated else it }) },
            onDismiss = { editingExercise = null }, allowCompletion = true)
    } else LeafEditSheet(onDismissRequest = { if (!saving) onDismiss() }, title = { Text(stringResource(R.string.workout_edit_title)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DaySectionHeader(stringResource(R.string.workout_record_date), stringResource(R.string.workout_record_date), day, { choosingDate = true })
            ExpressiveTimeInput(stringResource(R.string.workout_record_time), LeafIcons.Schedule, time, { time = it })
            if (plan != null) {
                AppTextField(title, { title = it.take(120) }, label = stringResource(R.string.journal_plan_name), modifier = Modifier.fillMaxWidth())
                VariantSelector(plan!!) { selected -> plan = plan!!.copy(variantId = selected.id) }
                Text(stringResource(R.string.journal_exercises), style = MaterialTheme.typography.titleMedium)
                plan!!.exercises.forEach { exercise ->
                    LeafSurface(onClick = { editingExercise = exercise }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                        Text(exercise.name, Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            AppTextField(note, { note = it.take(2000) }, label = stringResource(R.string.workout_note_hint),
                modifier = Modifier.fillMaxWidth(), singleLine = false, maxLines = 5)
            if (error) Text(stringResource(R.string.workout_edit_error), color = MaterialTheme.colorScheme.error)
        } },
        confirmButton = { LeafButton(enabled = !saving, onClick = {
            val date = java.time.Instant.ofEpochMilli(day).atZone(zone).toLocalDate()
            val unchanged = date == initial.toLocalDate() && time == initial.hour * 60 + initial.minute
            val start = if (unchanged) workout.startTime else date.atTime(time / 60, time % 60, initial.second).atZone(zone).toInstant().toEpochMilli()
            val editedPlan = plan?.copy(title = title.trim())
            val metrics = if (editedPlan == null) workout.metrics else {
                val old = org.json.JSONObject(workout.metrics)
                old.put("plan", WorkoutJournal.toJson(editedPlan)).toString()
            }
            saving = true; error = false
            onSave(workout.copy(startTime = start, endTime = workout.endTime + (start - workout.startTime), note = note.trim(), metrics = metrics,
                distanceMeters = editedPlan?.recordedMeters ?: workout.distanceMeters)) { ok ->
                saving = false
                if (ok) onDismiss() else error = true
            }
        }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}
