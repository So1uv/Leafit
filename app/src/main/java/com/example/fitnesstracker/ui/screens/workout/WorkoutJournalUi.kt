package com.example.fitnesstracker.ui.screens.workout

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton
import com.example.fitnesstracker.ui.components.LeafCheckbox as Checkbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
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
    EntryMode.DISTANCE -> LeafIcons.Distance
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
    modifier: Modifier = Modifier,
    best: ExerciseBest? = null,
    onRecordClick: (() -> Unit)? = null,
    onReplace: (() -> Unit)? = null,
    editable: Boolean, canComplete: Boolean, onEdit: () -> Unit, onSet: (String) -> Unit,
    onSetValues: ((JournalSet) -> Unit)? = null,
    onMove: ((Int) -> Unit)? = null, onDelete: (() -> Unit)? = null,
    timerSetId: String? = null, onStartTimer: ((String) -> Unit)? = null, timerText: (@Composable () -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    var inlineSet by rememberSaveable(exercise.id) { mutableStateOf<String?>(null) }
    LeafSurface(modifier = modifier, shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column {
            LeafSurface(onClick = onToggle, color = colors.surfaceContainer, shape = RoundedCornerShape(28.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeafIconBadge(modeIcon(exercise.mode), tone = LeafTone.Secondary)
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(stringResource(R.string.journal_sets_progress, exercise.sets.count { it.completed }, exercise.sets.size),
                            style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        if (best != null && best.sessions > 0) {
                            Text(when (exercise.mode) {
                                EntryMode.REPS -> if (best.maxWeightKg > 0f) stringResource(R.string.journal_personal_best,
                                    best.maxReps, number(best.maxWeightKg)) else stringResource(R.string.journal_personal_best_reps, best.maxReps)
                                EntryMode.TIME -> DateUtils.formatDuration(best.maxSeconds.toLong())
                                EntryMode.DISTANCE -> number(best.maxMeters / 1000f) + " " + stringResource(R.string.live_km)
                                EntryMode.SCORE -> best.maxScore.toString()
                            },
                                style = MaterialTheme.typography.labelSmall, color = colors.primary,
                                modifier = if (onRecordClick != null) Modifier.clickable(onClick = onRecordClick) else Modifier)
                        }
                    }
                    if (editable && onMove != null) {
                        val move by rememberUpdatedState(onMove)
                        Icon(LeafIcons.MoreVert, stringResource(R.string.journal_move_up),
                            Modifier.size(48.dp).pointerInput(exercise.id) {
                                var dragDistance = 0f
                                detectDragGesturesAfterLongPress(
                                    onDragEnd = { dragDistance = 0f },
                                    onDragCancel = { dragDistance = 0f }
                                ) { change, amount ->
                                    change.consume()
                                    dragDistance += amount.y
                                    val threshold = 62.dp.toPx()
                                    if (kotlin.math.abs(dragDistance) >= threshold) {
                                        move(if (dragDistance > 0) 1 else -1)
                                        dragDistance = 0f
                                    }
                                }
                            })
                    } else Icon(if (expanded) LeafIcons.KeyboardArrowUp else LeafIcons.KeyboardArrowDown,
                        stringResource(if (expanded) R.string.history_collapse else R.string.history_expand))
                }
            }
            AnimatedVisibility(expanded, enter = expandVertically(spring(1f, 550f)) + fadeIn(),
                exit = shrinkVertically(spring(1f, 550f)) + fadeOut()) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.sets.forEachIndexed { index, set ->
                        LeafSurface(shape = RoundedCornerShape(18.dp), color = if (set.completed) colors.primaryContainer else colors.surfaceContainerHigh) {
                            Column {
                            Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${index + 1}", Modifier.width(28.dp), style = MaterialTheme.typography.labelLarge, color = if (set.completed) colors.onPrimaryContainer.copy(alpha = 0.7f) else colors.onSurfaceVariant)
                                Column(Modifier.weight(1f).padding(vertical = 11.dp)) {
                                    if (timerSetId == set.id && timerText != null) timerText()
                                    else Text(setLabel(set, exercise.mode), style = MaterialTheme.typography.titleSmall,
                                        color = if (set.completed) colors.onPrimaryContainer else colors.onSurface)
                                    if (exercise.mode == EntryMode.REPS && set.weightKg == null)
                                        Text(stringResource(R.string.journal_no_load), style = MaterialTheme.typography.labelSmall, color = if (set.completed) colors.onPrimaryContainer.copy(alpha = 0.7f) else colors.onSurfaceVariant)
                                }
                                if (editable) IconButton(onClick = {
                                    if (onSetValues != null) {
                                        inlineSet = if (inlineSet == set.id) null else set.id
                                    } else onEdit()
                                }) {
                                    Icon(LeafIcons.Edit, stringResource(R.string.note_edit_action), Modifier.size(18.dp))
                                }
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
                            if (inlineSet == set.id && editable && onSetValues != null) {
                                InlineSetEditor(set, exercise.mode, onSave = {
                                    onSetValues(it); inlineSet = null
                                }, onCancel = { inlineSet = null })
                            }
                            }
                        }
                    }
                    if (onReplace != null) TextButton(onClick = onReplace) { Text(stringResource(R.string.library_replace)) }
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
private fun InlineSetEditor(set: JournalSet, mode: EntryMode, onSave: (JournalSet) -> Unit, onCancel: () -> Unit) {
    var first by rememberSaveable(set.id) { mutableStateOf(when (mode) {
        EntryMode.REPS -> set.reps?.toString().orEmpty()
        EntryMode.TIME -> set.seconds?.toString().orEmpty()
        EntryMode.DISTANCE -> number(set.distanceMeters?.div(1000))
        EntryMode.SCORE -> set.score?.toString().orEmpty()
    }) }
    var second by rememberSaveable(set.id) { mutableStateOf(when (mode) {
        EntryMode.REPS -> number(set.weightKg)
        EntryMode.DISTANCE -> set.seconds?.toString().orEmpty()
        else -> ""
    }) }
    val numeric = first.replace(',', '.').toFloatOrNull()
    val other = second.replace(',', '.').toFloatOrNull()
    val valid = numeric != null && numeric.isFinite() && numeric >= 0f &&
        numeric <= (if (mode == EntryMode.DISTANCE) 1000f else 86400f) &&
        (second.isBlank() || other != null && other.isFinite() && other in 0f..1_000_000f)
    Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTextField(first, { first = it }, stringResource(when (mode) {
            EntryMode.REPS -> R.string.journal_repetitions
            EntryMode.TIME -> R.string.journal_seconds
            EntryMode.DISTANCE -> R.string.journal_distance_km
            EntryMode.SCORE -> R.string.journal_score
        }), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
        if (mode == EntryMode.REPS || mode == EntryMode.DISTANCE)
            AppTextField(second, { second = it }, stringResource(if (mode == EntryMode.REPS)
                R.string.journal_load else R.string.journal_seconds),
                Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) }
            LeafButton(enabled = valid, onClick = {
                val result = when (mode) {
                    EntryMode.REPS -> set.copy(reps = numeric!!.toInt(), weightKg = other)
                    EntryMode.TIME -> set.copy(seconds = numeric!!.toInt())
                    EntryMode.DISTANCE -> set.copy(distanceMeters = numeric!! * 1000f, seconds = other?.toInt())
                    EntryMode.SCORE -> set.copy(score = numeric!!.toInt())
                }
                onSave(result.copy(completed = result.completed && result.hasResult(mode)))
            }) { Text(stringResource(R.string.common_save)) }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddExerciseSheet(mode: EntryMode, restSeconds: Int, onSelect: (JournalExercise) -> Unit, onDismiss: () -> Unit,
    replacing: JournalExercise? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var movement by rememberSaveable { mutableStateOf("") }
    var muscle by rememberSaveable { mutableStateOf("") }
    var equipment by rememberSaveable { mutableStateOf("") }
    var selected by remember(replacing?.id) { mutableStateOf<ExercisePreset?>(
        replacing?.let { entry -> WorkoutCatalog.exercises.firstOrNull { context.getString(it.labelRes) == entry.name } }) }
    var custom by remember { mutableStateOf(false) }
    var technique by rememberSaveable { mutableStateOf("") }
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    val store = remember(context) { com.example.fitnesstracker.utils.LifeStore.get(context) }
    val life by store.state.collectAsLeafState()
    val profile = life.equipmentProfiles.firstOrNull { it.id == life.activeEquipmentId }
    val available = profile?.equipment.orEmpty()
    val entries = WorkoutCatalog.exercises.map { it to stringResource(it.labelRes) }
        .filter { (preset, name) ->
            val info = ExerciseLibrary.info(preset)
            name.contains(query.trim(), ignoreCase = true) &&
                (muscle.isBlank() || info.muscle == muscle) &&
                (movement.isBlank() || ExerciseLibrary.movement(preset) == movement) &&
                (equipment.isBlank() || info.equipment == equipment) &&
                (profile == null || info.equipment == "bodyweight" || info.equipment in available)
        }.sortedBy { if (it.first.mode == mode) 0 else 1 }
    if (selected != null) {
        val preset = selected!!
        val info = ExerciseLibrary.info(preset)
        LeafEditSheet(onDismissRequest = { selected = null }, title = { Text(stringResource(preset.labelRes)) },
            content = {
                Text(stringResource(ExerciseLibrary.tag(info.muscle)) + " · " +
                    stringResource(ExerciseLibrary.tag(info.equipment)), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(info.techniqueRes), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.library_alternatives), style = MaterialTheme.typography.titleMedium)
                ExerciseLibrary.alternatives(preset, profile?.equipment).forEach { alternative ->
                    TextButton(onClick = { selected = alternative }) { Text(stringResource(alternative.labelRes)) }
                }
            }, confirmButton = { LeafButton(onClick = {
                onSelect(JournalExercise(name = context.getString(preset.labelRes), mode = preset.mode, restSeconds = restSeconds))
            }) { Text(stringResource(if (replacing == null) R.string.journal_add_exercise else R.string.library_replace)) } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text(stringResource(R.string.common_close)) } })
    } else if (custom) {
        LeafEditSheet(onDismissRequest = { custom = false }, title = { Text(stringResource(R.string.library_custom)) },
            content = {
                AppTextField(query, { query = it.take(120) }, stringResource(R.string.journal_exercise_name),
                    Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseLibrary.muscles.forEach { tag -> FilterChip(muscle == tag, { muscle = tag },
                        label = { Text(stringResource(ExerciseLibrary.tag(tag))) }) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseLibrary.equipment.forEach { tag -> FilterChip(equipment == tag, { equipment = tag },
                        label = { Text(stringResource(ExerciseLibrary.tag(tag))) }) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseLibrary.movements.forEach { tag -> FilterChip(movement == tag, { movement = tag },
                        label = { Text(stringResource(ExerciseLibrary.movementLabel(tag))) }) }
                }
                AppTextField(technique, { technique = it.take(500) }, stringResource(R.string.library_technique),
                    Modifier.fillMaxWidth(), singleLine = false, maxLines = 4)
            }, confirmButton = { LeafButton(enabled = query.isNotBlank(), onClick = {
                store.update { it.copy(customExercises = it.customExercises + com.example.fitnesstracker.utils.UserExercise(
                    name = query.trim(), mode = mode.name, muscle = muscle.ifBlank { "mobility" },
                    equipment = equipment.ifBlank { "bodyweight" }, technique = technique.trim(), movement = movement.ifBlank { "mobility" })) }
                onSelect(JournalExercise(name = query.trim(), mode = mode, restSeconds = restSeconds))
            }) { Text(stringResource(R.string.common_save)) } },
            dismissButton = { TextButton(onClick = { custom = false }) { Text(stringResource(R.string.common_cancel)) } })
    } else LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_add_exercise)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppSearchBar(query, { query = it }, stringResource(R.string.journal_search_exercise), Modifier.fillMaxWidth())
            Text(profile?.name ?: stringResource(R.string.library_all_equipment),
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            LeafTonalButton(onClick = { filtersOpen = !filtersOpen }, modifier = Modifier.fillMaxWidth()) {
                Icon(LeafIcons.Tune, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.design_library_filters))
                if (listOf(muscle, equipment, movement).any { it.isNotBlank() }) {
                    Spacer(Modifier.width(8.dp)); Text(listOf(muscle, equipment, movement).count { it.isNotBlank() }.toString())
                }
            }
            if (filtersOpen) FeatureSection(stringResource(R.string.design_library_filters)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = muscle.isBlank(), onClick = { muscle = "" },
                    label = { Text(stringResource(R.string.library_all_muscles)) })
                ExerciseLibrary.muscles.forEach { key ->
                    FilterChip(selected = muscle == key, onClick = { muscle = if (muscle == key) "" else key },
                        label = { Text(stringResource(ExerciseLibrary.tag(key))) })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = equipment.isBlank(), onClick = { equipment = "" },
                    label = { Text(stringResource(R.string.library_all_equipment)) })
                ExerciseLibrary.equipment.forEach { key ->
                    FilterChip(selected = equipment == key, onClick = { equipment = if (equipment == key) "" else key },
                        label = { Text(stringResource(ExerciseLibrary.tag(key))) })
                }
            }
            Text(stringResource(R.string.library_movement), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ExerciseLibrary.movements.forEach { key ->
                    FilterChip(selected = movement == key, onClick = { movement = if (movement == key) "" else key },
                        label = { Text(stringResource(ExerciseLibrary.movementLabel(key))) })
                }
            }
            }
            entries.forEach { (preset, label) ->
                LeafSurface(onClick = { selected = preset },
                    shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureBadge(modeIcon(preset.mode))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(label, style = MaterialTheme.typography.titleMedium)
                            val info = ExerciseLibrary.info(preset)
                            Text(stringResource(ExerciseLibrary.tag(info.muscle)) + " · " + stringResource(ExerciseLibrary.tag(info.equipment)),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp))
                    }
                }
            }
            life.customExercises.filter { it.name.contains(query, ignoreCase = true) &&
                (muscle.isBlank() || it.muscle == muscle) && (equipment.isBlank() || it.equipment == equipment) &&
                (profile == null || it.equipment == "bodyweight" || it.equipment in available) &&
                (movement.isBlank() || it.movement == movement) }.forEach { exercise ->
                LeafSurface(onClick = { onSelect(JournalExercise(name = exercise.name,
                    mode = runCatching { EntryMode.valueOf(exercise.mode) }.getOrDefault(mode),
                    restSeconds = restSeconds)) }, shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        Text(exercise.technique, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { store.update { it.copy(customExercises = it.customExercises.filterNot { row -> row.id == exercise.id }) } }) {
                            Text(stringResource(R.string.common_delete))
                        }
                    }
                }
            }
        } }, confirmButton = { LeafButton(onClick = { custom = true }) {
            Text(stringResource(R.string.library_custom))
        } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}
