package com.example.fitnesstracker.ui.screens.workout

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton
import com.example.fitnesstracker.ui.components.LeafCheckbox as Checkbox
import com.example.fitnesstracker.ui.components.LeafConfirmDialog as AlertDialog

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(vm: WorkoutViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifeStore = remember(context) { com.example.fitnesstracker.utils.LifeStore.get(context) }
    val life by lifeStore.state.collectAsLeafState()
    val history by vm.history.collectAsLeafState()
    val lastResult by vm.lastResult.collectAsLeafState()
    val plan by vm.plan.collectAsLeafState()
    val bests by produceState(emptyMap<String, ExerciseBest>(), history) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { exerciseBests(history) }
    }
    val running by vm.isRunning.collectAsLeafState()
    val savedTemplates by vm.templates.collectAsLeafState()
    val paused by vm.isPaused.collectAsLeafState()
    val intervalActive by remember(vm) { vm.interval.map { it != null && it.phase != "done" }.distinctUntilChanged() }.collectAsLeafState(initial = false)
    val ready by vm.ready.collectAsLeafState()
    val setTimer by vm.setTimer.collectAsLeafState()
    val saving by vm.saving.collectAsLeafState()
    val message by vm.message.collectAsLeafState()
    var selectedDay by rememberSaveable { mutableLongStateOf(DateUtils.todayStart()) }
    var choosingDate by rememberSaveable { mutableStateOf(false) }
    var expandedExercise by rememberSaveable { mutableStateOf<String?>(null) }
    var progressExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var editingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var deletingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var replacingExercise by remember { mutableStateOf<JournalExercise?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var configuring by rememberSaveable { mutableStateOf(false) }
    var templates by rememberSaveable { mutableStateOf(false) }
    var finishing by rememberSaveable { mutableStateOf(false) }
    val finishRequested by vm.finishRequested.collectAsLeafState()
    var discard by rememberSaveable { mutableStateOf(false) }
    var workoutNote by rememberSaveable { mutableStateOf("") }
    val records = remember(history, selectedDay) { history.filter { it.startTime in selectedDay until DateUtils.dayEnd(selectedDay) }.sortedByDescending { it.startTime } }
    val newest = records.firstOrNull()?.id
    var expandedRecord by rememberSaveable(selectedDay, newest) { mutableStateOf(newest) }
    lastResult?.let { result ->
        val previous = remember(result.id, history) {
            exerciseBests(history.filter { it.id != result.id && it.startTime <= result.startTime })
        }
        WorkoutResultScreen(result, vm::dismissLastResult, previous)
    }
    progressExercise?.let { entry -> ExerciseProgressSheet(entry.name, entry.mode, history) { progressExercise = null } }
    val header = rememberLeafHeaderState()
    val variant = WorkoutCatalog.variant(plan)
    LaunchedEffect(running, finishRequested) {
        if (finishRequested && running) { finishing = true; vm.consumeFinishRequest() }
        else if (!running && !finishRequested) { finishing = false; workoutNote = "" }
    }
    if (choosingDate) LeafDatePicker(selectedDay, { selectedDay = it; choosingDate = false }, { choosingDate = false })
    replacingExercise?.let { current -> AddExerciseSheet(current.mode, current.restSeconds,
        onSelect = { replacement ->
            vm.saveExercise(replacement.copy(id = current.id)); expandedExercise = current.id; replacingExercise = null
        }, onDismiss = { replacingExercise = null }, replacing = current) }
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
            if (saving) LeafLoadingIndicator(Modifier.size(22.dp), MaterialTheme.colorScheme.onPrimary)
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
            life.appointment?.takeIf { it.time in DateUtils.todayStart() until DateUtils.dayEnd(DateUtils.todayStart()) &&
                !running }?.let { appointment ->
                item(key = "scheduled_workout") {
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(24.dp)) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.catalog_scheduled),
                                    style = MaterialTheme.typography.labelMedium)
                                Text(appointment.title, style = MaterialTheme.typography.titleMedium)
                            }
                            LeafButton(enabled = savedTemplates.any { it.id == appointment.templateId }, onClick = {
                                savedTemplates.firstOrNull { it.id == appointment.templateId }?.let(vm::useTemplate)
                                vm.startWorkout()
                                lifeStore.update { it.copy(appointment = null) }
                            }) { Text(stringResource(R.string.catalog_start)) }
                        }
                    }
                }
            }
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
            if (!running) item(key = "equipment_profiles") { EquipmentProfileCard() }
            item(key = "planner_header") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.journal_exercises), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    if (!running) IconButton(enabled = ready, onClick = { templates = true }) { Icon(LeafIcons.BookmarkBorder, stringResource(R.string.journal_templates)) }
                    IconButton(enabled = ready && !saving && !intervalActive, onClick = { configuring = true }) { Icon(LeafIcons.Tune, stringResource(R.string.journal_configure)) }
                }
            }
            itemsIndexed(plan.exercises, key = { _, entry -> entry.id }) { _, entry ->
                JournalExerciseCard(entry, expandedExercise == entry.id, modifier = Modifier.animateItem(),
                    best = bests[exerciseKey(entry.name, entry.mode)], onToggle = { expandedExercise = if (expandedExercise == entry.id) null else entry.id },
                    onRecordClick = { progressExercise = entry },
                    onReplace = if (!saving && !intervalActive && entry.sets.none { it.completed } && setTimer?.exerciseId != entry.id)
                        ({ replacingExercise = entry }) else null,
                    editable = !saving && setTimer?.exerciseId != entry.id && !intervalActive, canComplete = running && !paused && !saving,
                    timerSetId = setTimer?.setId, onStartTimer = if (setTimer == null) ({ vm.startSetTimer(entry.id, it) }) else null,
                    timerText = { val seconds by vm.setSeconds.collectAsLeafState(); Text(DateUtils.formatDuration(seconds), style = MaterialTheme.typography.titleMedium) },
                    onEdit = { editingExercise = entry }, onSet = { vm.toggleSet(entry.id, it) },
                    onSetValues = { changed -> vm.saveExercise(entry.copy(sets = entry.sets.map {
                        if (it.id == changed.id) changed else it
                    })) },
                    onMove = { vm.moveExercise(entry.id, it) }, onDelete = { deletingExercise = entry })
            }
            item(key = "add_exercise") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (plan.exercises.isEmpty()) Text(stringResource(R.string.journal_plan_hint),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LeafTonalButton(enabled = ready && !saving && !intervalActive && plan.exercises.size < 100, onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(LeafIcons.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.journal_add_exercise))
                    }
                }
            }
            item(key = "rest") { RestTimerCard(vm) }
            item(key = "interval_guide") { IntervalGuideCard(vm, plan, running, paused) }

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
    val running by vm.isRunning.collectAsLeafState()
    val paused by vm.isPaused.collectAsLeafState()
    val intervalActive by remember(vm) { vm.interval.map { it != null && it.phase != "done" }.distinctUntilChanged() }.collectAsLeafState(initial = false)
    val ready by vm.ready.collectAsLeafState()
    val elapsed by vm.elapsedSeconds.collectAsLeafState()
    val saving by vm.saving.collectAsLeafState()
    val colors = MaterialTheme.colorScheme
    val variant = WorkoutCatalog.variant(plan)
    Surface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (!running) WorkoutKindPicker(plan) { type, variantId ->
                if (type != plan.type) vm.setWorkoutType(type)
                vm.updatePlan(vm.plan.value.copy(variantId = variantId))
            }
            else Text(if (plan.title.isBlank()) stringResource(variant.labelRes) else plan.title, style = MaterialTheme.typography.titleMedium)
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
    val rest by vm.restRemaining.collectAsLeafState()
    if (rest <= 0) return
    val paused by vm.isPaused.collectAsLeafState()
    val intervalActive by remember(vm) { vm.interval.map { it != null && it.phase != "done" }.distinctUntilChanged() }.collectAsLeafState(initial = false)
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(LeafIcons.Timer, container = MaterialTheme.colorScheme.surfaceContainerLow,
                content = MaterialTheme.colorScheme.tertiary)
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
    Surface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(if (recorded) R.string.journal_results else if (running) R.string.journal_session else R.string.journal_plan), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    MetricText(if (running || recorded) "${plan.completedCount} / ${plan.setCount}" else plan.setCount.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.journal_sets), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                Column(Modifier.weight(1f)) {
                    MetricText(plan.exercises.size.toString(), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.journal_exercises), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
            }
            if (running && !recorded && next != null) Text(stringResource(R.string.journal_next, next.name), style = MaterialTheme.typography.bodyMedium)
            if (plan.totalReps > 0 || plan.volumeKg > 0) Text(stringResource(R.string.journal_totals, plan.totalReps, plan.volumeKg),
                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
internal fun VariantSelector(plan: WorkoutPlan, onSelect: (WorkoutVariant) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    val selected = WorkoutCatalog.variant(plan)
    Surface(onClick = { open = true }, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(variantIcon(selected), size = LeafIconSize.Small, tone = LeafTone.Secondary)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.journal_variant), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(selected.labelRes), style = MaterialTheme.typography.titleMedium)
            }
            Icon(LeafIcons.ExpandMore, stringResource(R.string.journal_variant), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (open) LeafEditSheet(onDismissRequest = { open = false }, title = { Text(stringResource(R.string.journal_variant)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            WorkoutCatalog.forType(plan.type).forEach { variant ->
                Surface(onClick = { onSelect(variant); open = false }, shape = RoundedCornerShape(20.dp),
                    color = if (selected.id == variant.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(variant.labelRes), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Icon(variantIcon(variant), null, Modifier.size(20.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateSheet(vm: WorkoutViewModel, onDismiss: () -> Unit) {
    val templates by vm.templates.collectAsLeafState()
    val relayContext = androidx.compose.ui.platform.LocalContext.current
    val store = remember(relayContext) { com.example.fitnesstracker.utils.LifeStore.get(relayContext) }
    val life by store.state.collectAsLeafState()
    val orderedTemplates = remember(templates, life.favoriteTemplates, life.recentTemplates) {
        templates.sortedWith(compareByDescending<WorkoutTemplate> { it.id in life.favoriteTemplates }
            .thenByDescending { template -> life.recentTemplates.indexOf(template.id).let { if (it < 0) -100 else -it } })
    }
    var schedule by remember { mutableStateOf<WorkoutTemplate?>(null) }
    val relayScope = rememberCoroutineScope()
    var relayError by remember { mutableStateOf(false) }
    var relayBusy by remember { mutableStateOf(false) }
    var imported by remember { mutableStateOf<WorkoutTemplate?>(null) }
    val relayPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) relayScope.launch {
        relayBusy = true
        imported = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.fitnesstracker.sharing.TemplateRelay.read(relayContext, uri)
        }
        relayError = imported == null
        relayBusy = false
    } }
    imported?.let { candidate ->
        LeafEditSheet(onDismissRequest = { imported = null },
            title = { Text(stringResource(R.string.relay_preview)) },
            content = {
                Text(candidate.name, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.journal_template_detail, candidate.plan.exercises.size, candidate.plan.setCount))
                Text(stringResource(R.string.catalog_duration, candidate.plan.estimatedMinutes()), style = MaterialTheme.typography.bodyMedium)
                candidate.equipment.ifEmpty { candidate.plan.equipmentTags(relayContext) }.forEach { tag ->
                    Text(stringResource(ExerciseLibrary.tag(tag)), style = MaterialTheme.typography.labelMedium)
                }
                candidate.plan.exercises.forEach { Text(it.name, style = MaterialTheme.typography.bodyMedium) }
            },
            confirmButton = { LeafButton(onClick = { vm.importTemplate(candidate); imported = null }) {
                Text(stringResource(R.string.relay_save))
            } }, dismissButton = { TextButton(onClick = { imported = null }) { Text(stringResource(R.string.common_cancel)) } })
    }
    var scheduleDay by rememberSaveable { mutableLongStateOf(DateUtils.todayStart()) }
    var scheduleTime by rememberSaveable { mutableIntStateOf(18 * 60) }
    var choosingScheduleDay by rememberSaveable { mutableStateOf(false) }
    schedule?.let { template ->
        if (choosingScheduleDay) LeafDatePicker(scheduleDay,
            onSelect = { scheduleDay = it; choosingScheduleDay = false },
            onDismiss = { choosingScheduleDay = false })
        else LeafEditSheet(onDismissRequest = { schedule = null },
            title = { Text(stringResource(R.string.catalog_schedule)) }, content = {
                FeatureSection(template.name) {
                    LeafTonalButton(onClick = { choosingScheduleDay = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(LeafIcons.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(DateUtils.formatDate(scheduleDay))
                    }
                    ExpressiveTimeInput(stringResource(R.string.workout_record_time), LeafIcons.Schedule, scheduleTime,
                        onChange = { scheduleTime = it })
                }
            }, confirmButton = { LeafButton(onClick = {
                val time = java.util.Calendar.getInstance().apply {
                    timeInMillis = scheduleDay
                    set(java.util.Calendar.HOUR_OF_DAY, scheduleTime / 60)
                    set(java.util.Calendar.MINUTE, scheduleTime % 60)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.timeInMillis
                store.update { it.copy(appointment = com.example.fitnesstracker.utils.WorkoutAppointment(time, template.id, template.name)) }
                schedule = null
            }) { Text(stringResource(R.string.common_save)) } },
            dismissButton = { TextButton(onClick = { schedule = null }) { Text(stringResource(R.string.common_cancel)) } })
    }
    var name by rememberSaveable { mutableStateOf("") }
    if (imported == null && schedule == null) LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.journal_templates)) },
        content = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(name, { name = it.take(120) }, label = stringResource(R.string.journal_template_name), modifier = Modifier.fillMaxWidth())
            LeafTonalButton(enabled = !relayBusy, onClick = { relayError = false; relayPicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(LeafIcons.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.relay_import))
            }
            if (relayBusy) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { LeafLoadingIndicator() }
            if (relayError) Text(stringResource(R.string.relay_invalid), color = MaterialTheme.colorScheme.error)
            orderedTemplates
                .forEachIndexed { index, template ->
                val section = when {
                    template.id in life.favoriteTemplates -> R.string.catalog_favorites
                    template.id in life.recentTemplates -> R.string.catalog_recent
                    else -> R.string.catalog_other
                }
                val previousTemplate = orderedTemplates.getOrNull(index - 1)
                val previousSection = when {
                    previousTemplate == null -> 0
                    previousTemplate.id in life.favoriteTemplates -> R.string.catalog_favorites
                    previousTemplate.id in life.recentTemplates -> R.string.catalog_recent
                    else -> R.string.catalog_other
                }
                if (section != previousSection) Text(stringResource(section), style = MaterialTheme.typography.titleMedium)
                Surface(shape = RoundedCornerShape(24.dp),
                    color = if (template.id in life.favoriteTemplates) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FeatureBadge(modeIcon(WorkoutCatalog.variant(template.plan).mode))
                            Spacer(Modifier.width(12.dp))
                            Text(template.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { store.update { it.copy(favoriteTemplates =
                                if (template.id in it.favoriteTemplates) it.favoriteTemplates - template.id
                                else it.favoriteTemplates + template.id) } }) {
                                Icon(if (template.id in life.favoriteTemplates) LeafIcons.Star else LeafIcons.StarOutline,
                                    stringResource(R.string.catalog_favorite))
                            }
                        }
                        Text(stringResource(R.string.journal_template_detail, template.plan.exercises.size,
                            template.plan.setCount) + " · " +
                            stringResource(R.string.catalog_duration, template.plan.estimatedMinutes()), style = MaterialTheme.typography.labelMedium)
                        Text(template.equipment.ifEmpty { template.plan.equipmentTags(relayContext) }
                            .joinToString(" · ") { relayContext.getString(ExerciseLibrary.tag(it)) }, style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LeafTonalButton(onClick = {
                                vm.useTemplate(template)
                                vm.startWorkout()
                                store.update { it.copy(recentTemplates =
                                    (listOf(template.id) + it.recentTemplates.filterNot { id -> id == template.id }).take(20)) }
                                onDismiss()
                            }) { Text(stringResource(R.string.catalog_start)) }
                            IconButton(onClick = { schedule = template }) {
                                Icon(LeafIcons.Schedule, stringResource(R.string.catalog_schedule))
                            }
                            IconButton(enabled = !relayBusy, onClick = { relayScope.launch {
                                relayBusy = true
                                relayError = runCatching { com.example.fitnesstracker.sharing.TemplateRelay.share(relayContext, template) }.isFailure
                                relayBusy = false
                            } }) {
                                Icon(LeafIcons.Upload, stringResource(R.string.relay_share))
                            }
                            IconButton(onClick = {
                                vm.deleteTemplate(template.id)
                                store.update { state -> state.copy(
                                    favoriteTemplates = state.favoriteTemplates - template.id,
                                    recentTemplates = state.recentTemplates.filterNot { it == template.id },
                                    appointment = state.appointment?.takeUnless { it.templateId == template.id }
                                ) }
                            }) {
                                Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete))
                            }
                        }
                    }
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

internal fun variantIcon(variant: WorkoutVariant) = when (variant.id) {
    "swimming" -> LeafIcons.Pool
    "hiking", "trail", "nordic" -> LeafIcons.Hiking
    "rowing" -> LeafIcons.Rowing
    "stretching", "mobility", "balance" -> LeafIcons.Accessibility
    else -> if (variant.outdoor) LeafIcons.Route else modeIcon(variant.mode)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WorkoutKindPicker(plan: WorkoutPlan, onPick: (type: String, variantId: String) -> Unit) {
    val types = com.example.fitnesstracker.ui.components.WorkoutType.entries
    val current = types.firstOrNull { it.key == plan.type } ?: types.first()
    val variant = WorkoutCatalog.variant(plan)
    var choosing by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Surface(onClick = { choosing = true }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerHigh) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(current.icon, tone = LeafTone.Primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(workoutTypeLabel(current), style = MaterialTheme.typography.titleMedium, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(variantIcon(variant), null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                    Text(stringResource(variant.labelRes), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            Icon(LeafIcons.ExpandMore, stringResource(R.string.workout_choose_type), tint = colors.onSurfaceVariant)
        }
    }
    if (!choosing) return
    var draftType by rememberSaveable { mutableStateOf(plan.type) }
    ModalBottomSheet(onDismissRequest = { choosing = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.b7_workout_kind), style = MaterialTheme.typography.titleLarge)
            Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                types.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pair.forEach { type ->
                            val isSelected = type.key == draftType
                            val bg by androidx.compose.animation.animateColorAsState(
                                if (isSelected) colors.primary else colors.surfaceContainerHigh, label = "kindTypeBg")
                            val fg = if (isSelected) colors.onPrimary else colors.onSurface
                            val corner by androidx.compose.animation.core.animateDpAsState(if (isSelected) 28.dp else 16.dp,
                                androidx.compose.animation.core.spring(.85f, 500f), label = "kindTypeShape")
                            Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(corner)).background(bg)
                                    .selectable(selected = isSelected, role = androidx.compose.ui.semantics.Role.RadioButton,
                                        onClick = { draftType = type.key })
                                    .heightIn(min = 56.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(type.icon, null, tint = fg, modifier = Modifier.size(24.dp))
                                LeafFitText(workoutTypeLabel(type), Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelLarge, color = fg)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Text(stringResource(R.string.journal_variant), style = MaterialTheme.typography.titleSmall, color = colors.primary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            val variants = WorkoutCatalog.forType(draftType)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                variants.forEachIndexed { index, option ->
                    val isCurrent = draftType == plan.type && option.id == variant.id
                    Surface(onClick = { onPick(draftType, option.id); choosing = false },
                        shape = groupedLeafShape(index, variants.size),
                        color = if (isCurrent) colors.secondaryContainer else colors.surfaceContainer) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Icon(variantIcon(option), null, Modifier.size(22.dp),
                                tint = if (isCurrent) colors.onSecondaryContainer else colors.onSurfaceVariant)
                            Text(stringResource(option.labelRes), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            if (option.outdoor) Icon(LeafIcons.LocationOn, null, Modifier.size(18.dp), tint = colors.primary)
                            if (isCurrent) Icon(LeafIcons.Check, null, Modifier.size(20.dp), tint = colors.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }
}
