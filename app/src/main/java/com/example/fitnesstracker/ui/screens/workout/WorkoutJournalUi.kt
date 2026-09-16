package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.workout.*

@Composable
internal fun modeLabel(mode: EntryMode): String = stringResource(when (mode) {
    EntryMode.REPS -> R.string.journal_mode_reps
    EntryMode.TIME -> R.string.journal_mode_time
    EntryMode.DISTANCE -> R.string.journal_mode_distance
    EntryMode.SCORE -> R.string.journal_mode_score
})
internal fun modeIcon(mode: EntryMode) = when (mode) {
    EntryMode.REPS -> LeafIcons.FitnessCenter
    EntryMode.TIME -> LeafIcons.Timer
    EntryMode.DISTANCE -> LeafIcons.Straighten
    EntryMode.SCORE -> LeafIcons.SportsTennis
}
internal fun number(value: Float?): String = value?.let { if (it == it.toInt().toFloat()) it.toInt().toString() else it.toString() }.orEmpty()

@Composable
internal fun setLabel(set: JournalSet, mode: EntryMode): String = when (mode) {
    EntryMode.REPS -> listOfNotNull(set.reps?.let { "$it ${stringResource(R.string.journal_reps_unit)}" },
        set.weightKg?.let { "${number(it)} ${stringResource(R.string.unit_kg)}" }).joinToString(" × ").ifEmpty { "—" }
    EntryMode.TIME -> set.seconds?.let { DateUtils.formatDuration(it.toLong()) } ?: "—"
    EntryMode.DISTANCE -> listOfNotNull(set.distanceMeters?.let { "${number(it / 1000)} ${stringResource(R.string.unit_km)}" },
        set.seconds?.let { DateUtils.formatDuration(it.toLong()) }).joinToString(" · ").ifEmpty { "—" }
    EntryMode.SCORE -> set.score?.let { "$it ${stringResource(R.string.unit_points)}" } ?: "—"
}

@Composable
internal fun JournalExerciseCard(exercise: JournalExercise, expanded: Boolean, onToggle: () -> Unit,
    editable: Boolean, canComplete: Boolean, onEdit: () -> Unit, onSet: (String) -> Unit,
    onMove: ((Int) -> Unit)? = null, onDelete: (() -> Unit)? = null,
    timerSetId: String? = null, onStartTimer: ((String) -> Unit)? = null, timerText: (@Composable () -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column {
            LeafSurface(onClick = onToggle, color = colors.surfaceContainer, shape = RoundedCornerShape(28.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeafSurface(shape = RoundedCornerShape(16.dp), color = colors.secondaryContainer) {
                        Box(Modifier.size(44.dp), Alignment.Center) { Icon(modeIcon(exercise.mode), null, tint = colors.onSecondaryContainer) }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.journal_sets_progress, exercise.sets.count { it.completed }, exercise.sets.size),
                            style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                    Icon(if (expanded) LeafIcons.KeyboardArrowUp else LeafIcons.KeyboardArrowDown,
                        stringResource(if (expanded) R.string.history_collapse else R.string.history_expand))
                }
            }
            AnimatedVisibility(expanded, enter = expandVertically(spring(1f, 550f)) + fadeIn(),
                exit = shrinkVertically(spring(1f, 550f)) + fadeOut()) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.sets.forEachIndexed { index, set ->
                        LeafSurface(shape = RoundedCornerShape(18.dp), color = if (set.completed) colors.primaryContainer else colors.surfaceContainerHigh) {
                            Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", Modifier.width(28.dp), style = MaterialTheme.typography.labelLarge, color = if (set.completed) colors.onPrimaryContainer.copy(alpha = 0.7f) else colors.onSurfaceVariant)
                                Column(Modifier.weight(1f).padding(vertical = 11.dp)) {
                                    if (timerSetId == set.id && timerText != null) timerText()
                                    else Text(setLabel(set, exercise.mode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium,
                                        color = if (set.completed) colors.onPrimaryContainer else colors.onSurface)
                                    if (exercise.mode == EntryMode.REPS && set.weightKg == null)
                                        Text(stringResource(R.string.journal_no_load), style = MaterialTheme.typography.labelSmall, color = if (set.completed) colors.onPrimaryContainer.copy(alpha = 0.7f) else colors.onSurfaceVariant)
                                }
                                if (editable) IconButton(onClick = onEdit) { Icon(LeafIcons.Edit, stringResource(R.string.note_edit_action), Modifier.size(18.dp)) }
                                if (exercise.mode == EntryMode.TIME && !set.completed && onStartTimer != null) {
                                    IconButton(enabled = canComplete, onClick = { onStartTimer(set.id) }) {
                                        Icon(LeafIcons.PlayArrow, stringResource(R.string.journal_start_set_timer))
                                    }
                                }
                                IconButton(enabled = canComplete && (timerSetId == null || timerSetId == set.id), onClick = { onSet(set.id) }) {
                                    Icon(if (set.completed) LeafIcons.CheckCircle else LeafIcons.RadioButtonUnchecked,
                                        stringResource(if (set.completed) R.string.journal_unmark else R.string.journal_mark),
                                        tint = if (set.completed) colors.primary else colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (exercise.note.isNotBlank()) Text(exercise.note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(stringResource(R.string.journal_rest_value, exercise.restSeconds), style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    if (editable) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Icon(LeafIcons.Tune, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.journal_configure), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        onMove?.let { move ->
                            IconButton(onClick = { move(-1) }) { Icon(LeafIcons.KeyboardArrowUp, stringResource(R.string.journal_move_up)) }
                            IconButton(onClick = { move(1) }) { Icon(LeafIcons.KeyboardArrowDown, stringResource(R.string.journal_move_down)) }
                        }
                        onDelete?.let { remove -> IconButton(onClick = remove) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete)) } }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ExerciseEditorSheet(exercise: JournalExercise, onSave: (JournalExercise) -> Unit, onDismiss: () -> Unit, allowCompletion: Boolean = false) {
    var name by rememberSaveable(exercise.id) { mutableStateOf(exercise.name) }
    var mode by rememberSaveable(exercise.id) { mutableStateOf(exercise.mode) }
    var rest by rememberSaveable(exercise.id) { mutableStateOf(exercise.restSeconds.toString()) }
    var note by rememberSaveable(exercise.id) { mutableStateOf(exercise.note) }
    var sets by remember(exercise.id) { mutableStateOf(exercise.sets) }
    val values = remember(exercise.id) { mutableStateMapOf<String, String>() }
    var invalid by remember { mutableStateOf(false) }
    fun value(s: JournalSet, key: String): String = values[s.id + key] ?: when (key) {
        "reps" -> s.reps?.toString().orEmpty(); "kg" -> number(s.weightKg)
        "seconds" -> s.seconds?.toString().orEmpty(); "km" -> number(s.distanceMeters?.div(1000))
        else -> s.score?.toString().orEmpty()
    }
    fun read(s: JournalSet): JournalSet = s.copy(reps = value(s, "reps").toIntOrNull(),
        weightKg = value(s, "kg").replace(',', '.').toFloatOrNull(), seconds = value(s, "seconds").toIntOrNull(),
        distanceMeters = value(s, "km").replace(',', '.').toFloatOrNull()?.times(1000), score = value(s, "score").toIntOrNull())
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_exercise)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AppTextField(name, { name = it.take(120) }, label = stringResource(R.string.journal_exercise_name), modifier = Modifier.fillMaxWidth())
                EntryMode.entries.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { item -> FilterChip(selected = mode == item, onClick = { mode = item },
                            modifier = Modifier.weight(1f), border = null, label = { Text(modeLabel(item)) },
                            leadingIcon = { Icon(modeIcon(item), null, Modifier.size(18.dp)) }) }
                    }
                }
                AppTextField(rest, { rest = it.filter(Char::isDigit).take(4) }, label = stringResource(R.string.journal_rest_seconds),
                    keyboardType = KeyboardType.Number, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.journal_sets), style = MaterialTheme.typography.titleMedium)
                sets.forEachIndexed { index, s ->
                    key(s.id) {
                        LeafSurface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.journal_set_number, index + 1), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                    if (allowCompletion) Checkbox(s.completed, { checked -> sets = sets.map { if (it.id == s.id) it.copy(completed = checked) else it } })
                                    IconButton(onClick = { sets = sets.filterNot { it.id == s.id } }) { Icon(LeafIcons.Close, stringResource(R.string.common_delete), Modifier.size(18.dp)) }
                                }
                                val fields = when (mode) {
                                    EntryMode.REPS -> listOf("reps" to R.string.journal_repetitions, "kg" to R.string.journal_load)
                                    EntryMode.TIME -> listOf("seconds" to R.string.journal_seconds)
                                    EntryMode.DISTANCE -> listOf("km" to R.string.journal_distance_km, "seconds" to R.string.journal_seconds)
                                    EntryMode.SCORE -> listOf("score" to R.string.journal_score)
                                }
                                fields.forEach { (key, label) ->
                                    AppTextField(value(s, key), { input ->
                                        values[s.id + key] = input.filter { it.isDigit() || ((key == "kg" || key == "km") && (it == '.' || it == ',')) }.take(9)
                                        invalid = false
                                    }, label = stringResource(label), modifier = Modifier.fillMaxWidth(),
                                        keyboardType = if (key == "kg" || key == "km") KeyboardType.Decimal else KeyboardType.Number)
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafTonalButton(enabled = sets.size < 100, onClick = { sets = sets + JournalSet() }) {
                        Icon(LeafIcons.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.journal_add_set))
                    }
                    if (sets.isNotEmpty()) IconButton(enabled = sets.size < 100, onClick = { sets = sets + read(sets.last()).copy(id = journalId(), completed = false) }) {
                        Icon(LeafIcons.ContentCopy, stringResource(R.string.journal_repeat_set))
                    }
                }
                AppTextField(note, { note = it.take(2000) }, label = stringResource(R.string.workout_note_hint), modifier = Modifier.fillMaxWidth(), singleLine = false, maxLines = 4)
                if (invalid) Text(stringResource(R.string.journal_invalid_values), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { LeafButton(enabled = name.isNotBlank() && sets.isNotEmpty(), onClick = {
            val readSets = sets.map(::read)
            val fields = when (mode) {
                EntryMode.REPS -> listOf("reps", "kg"); EntryMode.TIME -> listOf("seconds")
                EntryMode.DISTANCE -> listOf("km", "seconds"); EntryMode.SCORE -> listOf("score")
            }
            val validNumbers = sets.all { s -> fields.all { key ->
                val raw = value(s, key)
                val upper = when (key) { "km" -> 1000.0; "seconds" -> 86400.0; "reps" -> 10000.0; else -> 1_000_000.0 }
                raw.isBlank() || raw.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it in 0.0..upper } == true
            } }
            if (!validNumbers || (rest.toIntOrNull() ?: -1) !in 0..3600) { invalid = true; return@LeafButton }
            onSave(exercise.copy(name = name.trim(), mode = mode, restSeconds = rest.toInt(), note = note.trim(),
                sets = readSets.map { s -> s.copy(completed = s.completed && s.hasResult(mode)) }))
            onDismiss()
        }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}

@Composable
internal fun AddExerciseSheet(mode: EntryMode, restSeconds: Int, onSelect: (JournalExercise) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val entries = WorkoutCatalog.exercises.map { it to stringResource(it.labelRes) }
        .filter { it.second.contains(query.trim(), ignoreCase = true) }.sortedBy { if (it.first.mode == mode) 0 else 1 }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_add_exercise)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppSearchBar(query, { query = it }, stringResource(R.string.journal_search_exercise), Modifier.fillMaxWidth())
            entries.forEach { (preset, label) ->
                LeafSurface(onClick = { onSelect(JournalExercise(name = label, mode = preset.mode, restSeconds = restSeconds)) },
                    shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(modeIcon(preset.mode), null, Modifier.size(22.dp))
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Icon(LeafIcons.Add, null, Modifier.size(20.dp))
                    }
                }
            }
        } }, confirmButton = { LeafButton(onClick = { onSelect(JournalExercise(name = query.trim(), mode = mode, restSeconds = restSeconds)) }) {
            Text(stringResource(R.string.journal_custom))
        } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}
