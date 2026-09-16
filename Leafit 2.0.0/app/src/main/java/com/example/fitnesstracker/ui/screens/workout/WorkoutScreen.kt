package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
import com.example.fitnesstracker.workout.*

@Composable
fun WorkoutScreen(vm: WorkoutViewModel) {
    val history by vm.history.collectAsState()
    val plan by vm.plan.collectAsState()
    val running by vm.isRunning.collectAsState()
    val paused by vm.isPaused.collectAsState()
    val ready by vm.ready.collectAsState()
    val setTimer by vm.setTimer.collectAsState()
    val saving by vm.saving.collectAsState()
    val message by vm.message.collectAsState()
    var selectedDay by rememberSaveable { mutableLongStateOf(DateUtils.todayStart()) }
    var choosingDate by rememberSaveable { mutableStateOf(false) }
    var expandedExercise by rememberSaveable { mutableStateOf<String?>(null) }
    var editingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var deletingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var configuring by rememberSaveable { mutableStateOf(false) }
    var templates by rememberSaveable { mutableStateOf(false) }
    var finishing by rememberSaveable { mutableStateOf(false) }
    val finishRequested by vm.finishRequested.collectAsState()
    var discard by rememberSaveable { mutableStateOf(false) }
    var workoutNote by rememberSaveable { mutableStateOf("") }
    val records = remember(history, selectedDay) { history.filter { it.startTime in selectedDay until DateUtils.dayEnd(selectedDay) }.sortedByDescending { it.startTime } }
    val newest = records.firstOrNull()?.id
    var expandedRecord by rememberSaveable(selectedDay, newest) { mutableStateOf(newest) }
    val header = rememberLeafHeaderState()
    val variant = WorkoutCatalog.variant(plan)
    LaunchedEffect(running, finishRequested) {
        if (finishRequested && running) { finishing = true; vm.consumeFinishRequest() }
        else if (!running && !finishRequested) { finishing = false; workoutNote = "" }
    }
    if (choosingDate) LeafDatePicker(selectedDay, { selectedDay = it; choosingDate = false }, { choosingDate = false })
    if (adding) AddExerciseSheet(variant.mode, plan.restSeconds, onSelect = { adding = false; editingExercise = it }, onDismiss = { adding = false })
    editingExercise?.let { entry -> ExerciseEditorSheet(entry,
        onSave = { vm.saveExercise(it); expandedExercise = it.id }, onDismiss = { editingExercise = null }) }
    deletingExercise?.let { entry -> AlertDialog(onDismissRequest = { deletingExercise = null },
        title = { Text(stringResource(R.string.journal_remove_exercise)) }, text = { Text(entry.name) },
        confirmButton = { TextButton(onClick = { vm.deleteExercise(entry.id); deletingExercise = null }) { Text(stringResource(R.string.common_delete)) } },
        dismissButton = { TextButton(onClick = { deletingExercise = null }) { Text(stringResource(R.string.common_cancel)) } }) }
    if (configuring) PlanSettingsSheet(plan, { vm.updatePlan(it); configuring = false }, { configuring = false })
    if (templates) TemplateSheet(vm, { templates = false })
    if (finishing) LeafEditSheet(onDismissRequest = { if (!saving) finishing = false },
        title = { Text(stringResource(R.string.workout_save_q)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            JournalSummary(plan, running = true)
            AppTextField(workoutNote, { workoutNote = it.take(2000) }, label = stringResource(R.string.workout_note_hint),
                modifier = Modifier.fillMaxWidth(), singleLine = false, maxLines = 5)
            if (message == R.string.journal_save_error) Text(stringResource(R.string.journal_save_error), color = MaterialTheme.colorScheme.error)
        } },
        confirmButton = { LeafButton(enabled = !saving, onClick = { vm.stopAndSave(workoutNote) }) {
            if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text(stringResource(R.string.common_save))
        } }, dismissButton = { TextButton(enabled = !saving, onClick = { finishing = false; discard = true }) { Text(stringResource(R.string.common_delete)) } })
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text(stringResource(R.string.journal_discard_title)) },
        text = { Text(stringResource(R.string.journal_discard_message)) },
        confirmButton = { TextButton(onClick = { vm.stopAndDiscard(); discard = false }) { Text(stringResource(R.string.common_delete)) } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text(stringResource(R.string.common_cancel)) } })

    LeafScreenScaffold(headerState = header, topBar = {
        DayPageTopBar(stringResource(R.string.workout_day_today), stringResource(R.string.workout_day_other), selectedDay,
            { choosingDate = true }, headerState = header)
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 12.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "timer") { WorkoutSessionCard(vm, plan, onFinish = { vm.finishSetTimer(); vm.pauseWorkout(); finishing = true }) }
            if (message != null && message != R.string.journal_save_error) item(key = "message") {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.fillMaxWidth().padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(message!!), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = vm::clearMessage) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
                    }
                }
            }
            item(key = "session_summary") { JournalSummary(plan, running) }
            item(key = "rest") { RestTimerCard(vm) }
            item(key = "planner_header") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.journal_exercises), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    if (!running) IconButton(enabled = ready, onClick = { templates = true }) { Icon(LeafIcons.BookmarkBorder, stringResource(R.string.journal_templates)) }
                    IconButton(enabled = ready && !saving, onClick = { configuring = true }) { Icon(LeafIcons.Tune, stringResource(R.string.journal_configure)) }
                }
            }
            itemsIndexed(plan.exercises, key = { _, entry -> entry.id }) { _, entry ->
                JournalExerciseCard(entry, expandedExercise == entry.id,
                    onToggle = { expandedExercise = if (expandedExercise == entry.id) null else entry.id },
                    editable = !saving && setTimer?.exerciseId != entry.id, canComplete = running && !paused && !saving,
                    timerSetId = setTimer?.setId, onStartTimer = if (setTimer == null) ({ vm.startSetTimer(entry.id, it) }) else null,
                    timerText = { val seconds by vm.setSeconds.collectAsState(); Text(DateUtils.formatDuration(seconds), style = MaterialTheme.typography.titleMedium) },
                    onEdit = { editingExercise = entry }, onSet = { vm.toggleSet(entry.id, it) },
                    onMove = { vm.moveExercise(entry.id, it) }, onDelete = { deletingExercise = entry })
            }
            item(key = "add_exercise") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (plan.exercises.isEmpty()) Text(stringResource(R.string.journal_plan_hint),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LeafTonalButton(enabled = ready && !saving && plan.exercises.size < 100, onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(LeafIcons.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.journal_add_exercise))
                    }
                }
            }
            item(key = "history_title") {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistorySectionLabel(stringResource(R.string.history_records_label), records.size, LeafIcons.History)
                    if (records.isNotEmpty()) Text(stringResource(R.string.journal_day_summary, records.size, records.sumOf { it.durationSeconds } / 60),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (records.isEmpty()) item(key = "empty_history") { Text(stringResource(R.string.workout_history_empty_day_hint),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 20.dp)) }
            itemsIndexed(records, key = { _, workout -> workout.id }) { index, workout ->
                WorkoutHistoryCard(workout, expandedRecord == workout.id, { expandedRecord = if (expandedRecord == workout.id) null else workout.id },
                    index, records.size, vm::deleteWorkout, vm::updateWorkout)
            }
        }
    }
}

@Composable
private fun WorkoutSessionCard(vm: WorkoutViewModel, plan: WorkoutPlan, onFinish: () -> Unit) {
    val running by vm.isRunning.collectAsState()
    val paused by vm.isPaused.collectAsState()
    val ready by vm.ready.collectAsState()
    val elapsed by vm.elapsedSeconds.collectAsState()
    val saving by vm.saving.collectAsState()
    val colors = MaterialTheme.colorScheme
    val variant = WorkoutCatalog.variant(plan)
    Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!running) WorkoutTypeSelector(plan.type, vm::setWorkoutType)
            if (running) Text(if (plan.title.isBlank()) stringResource(variant.labelRes) else plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!running) VariantSelector(plan, { vm.updatePlan(plan.copy(variantId = it.id)) })
            WorkoutTimerDial(elapsed, running, paused)
            if (!running) {
                if (ready) com.example.fitnesstracker.tracking.WorkoutStartControls(plan.type,
                    routeAllowed = variant.outdoor, variantId = variant.id, sessionTitle = plan.title) { vm.startWorkout() }
                else LeafLoadingIndicator(Modifier.size(32.dp))
            } else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LeafTonalButton(enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    onClick = { if (paused) vm.resumeWorkout() else vm.pauseWorkout() }) {
                    Icon(if (paused) LeafIcons.PlayArrow else LeafIcons.Pause, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp)); Text(stringResource(if (paused) R.string.workout_resume else R.string.workout_pause))
                }
                LeafButton(enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 54.dp), onClick = onFinish) {
                    Icon(LeafIcons.Stop, null, Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.workout_stop))
                }
            }
        }
    }
}

@Composable
private fun RestTimerCard(vm: WorkoutViewModel) {
    val rest by vm.restRemaining.collectAsState()
    if (rest <= 0) return
    val paused by vm.isPaused.collectAsState()
    Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = remember { LeafRosette(6) }, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Box(Modifier.size(44.dp), Alignment.Center) {
                    Icon(LeafIcons.Timer, null, tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(if (paused) R.string.journal_rest_paused else R.string.journal_rest), style = MaterialTheme.typography.labelMedium)
                Text(DateUtils.formatDuration(rest.toLong()), style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"))
            }
            TextButton(onClick = vm::skipRest) { Text(stringResource(R.string.journal_end_rest)) }
        }
    }
}

@Composable
internal fun JournalSummary(plan: WorkoutPlan, running: Boolean, recorded: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val next = plan.exercises.firstOrNull { e -> e.sets.any { !it.completed } }
    Surface(shape = RoundedCornerShape(26.dp), color = colors.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(if (recorded) R.string.journal_results else if (running) R.string.journal_session else R.string.journal_plan), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(if (running || recorded) "${plan.completedCount} / ${plan.setCount}" else plan.setCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.journal_sets), style = MaterialTheme.typography.labelMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text(plan.exercises.size.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.journal_exercises), style = MaterialTheme.typography.labelMedium)
                }
            }
            if (running && !recorded && next != null) Text(stringResource(R.string.journal_next, next.name), style = MaterialTheme.typography.bodyMedium)
            if (plan.totalReps > 0 || plan.volumeKg > 0) Text(stringResource(R.string.journal_totals, plan.totalReps, plan.volumeKg),
                style = MaterialTheme.typography.labelMedium, color = colors.onSecondaryContainer)
        }
    }
}

@Composable
internal fun VariantSelector(plan: WorkoutPlan, onSelect: (WorkoutVariant) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    val selected = WorkoutCatalog.variant(plan)
    Surface(onClick = { open = true }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)) {
                Box(Modifier.size(36.dp), Alignment.Center) {
                    Icon(if (selected.outdoor) LeafIcons.Map else modeIcon(selected.mode), null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.journal_variant), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                Text(stringResource(selected.labelRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Icon(LeafIcons.ExpandMore, stringResource(R.string.journal_variant))
        }
    }
    if (open) LeafEditSheet(onDismissRequest = { open = false }, title = { Text(stringResource(R.string.journal_variant)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            WorkoutCatalog.forType(plan.type).forEach { variant ->
                Surface(onClick = { onSelect(variant); open = false }, shape = RoundedCornerShape(20.dp),
                    color = if (selected.id == variant.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(variant.labelRes), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Icon(if (variant.outdoor) LeafIcons.Map else modeIcon(variant.mode), null, Modifier.size(20.dp))
                    }
                }
            }
        } }, confirmButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.common_close)) } })
}

@Composable
private fun PlanSettingsSheet(plan: WorkoutPlan, onSave: (WorkoutPlan) -> Unit, onDismiss: () -> Unit) {
    var title by rememberSaveable { mutableStateOf(plan.title) }
    var rest by rememberSaveable { mutableStateOf(plan.restSeconds.toString()) }
    var applyToAll by rememberSaveable { mutableStateOf(false) }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_configure)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppTextField(title, { title = it.take(120) }, label = stringResource(R.string.journal_plan_name), modifier = Modifier.fillMaxWidth())
            AppTextField(rest, { rest = it.filter(Char::isDigit).take(4) }, label = stringResource(R.string.journal_rest_seconds),
                keyboardType = KeyboardType.Number, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(applyToAll, { applyToAll = it }); Text(stringResource(R.string.journal_rest_apply_all), style = MaterialTheme.typography.bodyMedium)
            }
        } }, confirmButton = { LeafButton(enabled = (rest.toIntOrNull() ?: -1) in 0..3600, onClick = {
            val seconds = rest.toInt(); onSave(plan.copy(title = title.trim(), restSeconds = seconds,
                exercises = if (applyToAll) plan.exercises.map { it.copy(restSeconds = seconds) } else plan.exercises))
        }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}

@Composable
private fun TemplateSheet(vm: WorkoutViewModel, onDismiss: () -> Unit) {
    val templates by vm.templates.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_templates)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(name, { name = it.take(120) }, label = stringResource(R.string.journal_template_name), modifier = Modifier.fillMaxWidth())
            templates.forEach { template ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = { vm.useTemplate(template); onDismiss() }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(template.name, style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.journal_template_detail, template.plan.exercises.size, template.plan.setCount),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    IconButton(onClick = { vm.deleteTemplate(template.id) }) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete)) }
                }
            }
        } }, confirmButton = { LeafButton(enabled = name.isNotBlank(), onClick = { vm.saveTemplate(name); onDismiss() }) {
            Text(stringResource(R.string.journal_save_template))
        } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } })
}
@Composable
internal fun workoutTypeLabel(type: com.example.fitnesstracker.ui.components.WorkoutType): String = when (type) {
    com.example.fitnesstracker.ui.components.WorkoutType.GENERAL -> stringResource(R.string.workout_type_general)
    com.example.fitnesstracker.ui.components.WorkoutType.RUN -> stringResource(R.string.workout_type_run)
    com.example.fitnesstracker.ui.components.WorkoutType.WALK -> stringResource(R.string.workout_type_walk)
    com.example.fitnesstracker.ui.components.WorkoutType.CYCLE -> stringResource(R.string.workout_type_cycle)
    com.example.fitnesstracker.ui.components.WorkoutType.GYM -> stringResource(R.string.workout_type_gym)
    com.example.fitnesstracker.ui.components.WorkoutType.YOGA -> stringResource(R.string.workout_type_yoga)
    com.example.fitnesstracker.ui.components.WorkoutType.SPORT -> stringResource(R.string.workout_type_sport)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutTypeSelector(selected: String, onSelect: (String) -> Unit) {
    val types = com.example.fitnesstracker.ui.components.WorkoutType.entries
    val current = types.firstOrNull { it.key == selected } ?: types.first()
    var choosing by remember { mutableStateOf(false) }
    Surface(
        onClick = { choosing = true }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(40.dp), Alignment.Center) {
                    Icon(current.icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.workout_type_title), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(workoutTypeLabel(current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Icon(com.example.fitnesstracker.ui.components.LeafIcons.ExpandMore, stringResource(R.string.workout_choose_type),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (choosing) {
        ModalBottomSheet(onDismissRequest = { choosing = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.workout_type_title), style = MaterialTheme.typography.titleLarge)
                Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { type ->
                                val isSelected = type.key == selected
                                val bg by androidx.compose.animation.animateColorAsState(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    label = "workoutTypeBackground"
                                )
                                val fg = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                Row(
                                    Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(bg)
                                        .selectable(selected = isSelected, role = androidx.compose.ui.semantics.Role.RadioButton,
                                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(type.key); choosing = false })
                                        .heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(type.icon, null, tint = fg, modifier = Modifier.size(24.dp))
                                    Text(workoutTypeLabel(type), style = MaterialTheme.typography.labelLarge,
                                        color = fg, modifier = Modifier.weight(1f))
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
