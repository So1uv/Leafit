package com.example.fitnesstracker.ui.screens.sleep

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import com.example.fitnesstracker.ui.components.collectAsLeafState
import com.example.fitnesstracker.ui.components.LeafScreenScaffold
import com.example.fitnesstracker.ui.components.rememberLeafHeaderState
import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.animation.SizeTransform
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafTitle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.viewmodel.SleepViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(vm: SleepViewModel, onBack: () -> Unit = {}) {
    val history       by vm.history.collectAsLeafState()
    val selectedDay   by vm.selectedDay.collectAsLeafState()
    val weekHours     by vm.weekSleepHours.collectAsLeafState()

    val newestId = remember(history) { history.maxByOrNull { it.wakeTime }?.id }
    var expandedRecordId by androidx.compose.runtime.saveable.rememberSaveable(newestId) { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var manualOpen by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var historyFilter by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }
    val shownHistory = remember(history, historyFilter) {
        history.filter { record ->
            val tracked = com.example.fitnesstracker.sleep.SleepNightStore.startOf(record.note) != null
            historyFilter == 0 || (historyFilter == 1) == tracked
        }
    }

    if (showDatePicker) {
        SleepDateSelectDialog(
            selectedDay = selectedDay,
            onSelect = { vm.selectDay(it); showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }

    val headerState = rememberLeafHeaderState()
    LeafScreenScaffold(
        headerState = headerState,
        topBar = {
            com.example.fitnesstracker.ui.components.DayPageTopBar(
                todayTitle = stringResource(R.string.sleep_day_today),
                dayTitle = stringResource(R.string.sleep_day_other),
                selectedDay = selectedDay, onChooseDate = { showDatePicker = true }, headerState = headerState)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 12.dp, bottom = 120.dp)
        ) {



            item(key = "sleep_summary", contentType = "sleep_summary") { SleepSummaryCard(history, selectedDay) }

            item(key = "sleep_tracker", contentType = "sleep_tracker") { SleepTrackerCard() }

            item(key = "sleep_alarms", contentType = "sleep_alarms") { SleepAlarmsCard() }

            item(key = "sleep_input", contentType = "sleep_input") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chevron by animateFloatAsState(if (manualOpen) 180f else 0f, spring(.8f, 400f), label = "manualChevron")
                    Surface(onClick = { manualOpen = !manualOpen }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            com.example.fitnesstracker.ui.components.LeafIconBadge(LeafIcons.Edit,
                                tone = com.example.fitnesstracker.ui.components.LeafTone.Secondary)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.b12_sleep_manual), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.b12_sleep_manual_hint), style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(LeafIcons.ExpandMore, null, Modifier.graphicsLayer { rotationZ = chevron })
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(manualOpen) {
                        AddSleepInline(selectedDay = selectedDay, onSave = { vm.saveRecord(it) })
                    }
                }
            }

            item(key = "sleep_week", contentType = "sleep_week") { SleepWeekChart(weekHours) }

            item(key = "sleep_history_header", contentType = "sleep_history_header") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.fitnesstracker.ui.components.HistorySectionLabel(
                        stringResource(R.string.sleep_history_title), shownHistory.size, LeafIcons.Bedtime)
                    if (history.isNotEmpty()) com.example.fitnesstracker.ui.components.ExpressiveChoiceRow(
                        listOf(stringResource(R.string.b13_history_all), stringResource(R.string.b13_history_tracked),
                            stringResource(R.string.b13_history_manual)),
                        listOf(LeafIcons.History, LeafIcons.GraphicEq, LeafIcons.Edit), historyFilter, { historyFilter = it })
                }
            }

            if (shownHistory.isEmpty()) {
                item(key = "sleep_history_empty") {
                    com.example.fitnesstracker.ui.components.FeatureEmpty(
                        stringResource(if (history.isEmpty()) R.string.sleep_records_empty else R.string.b13_history_filter_empty),
                        stringResource(R.string.sleep_records_empty_hint), LeafIcons.Bedtime)
                }
            } else {
                itemsIndexed(shownHistory, key = { _, record -> record.id }, contentType = { _, _ -> "sleep_record" }) { index, record ->
                    SleepRecordCard(
                        record = record, expanded = expandedRecordId == record.id,
                        onToggle = { expandedRecordId = if (expandedRecordId == record.id) null else record.id },
                        modifier = Modifier.animateItem(placementSpec = null), groupIndex = index, groupCount = shownHistory.size,
                        onDelete = {
                            com.example.fitnesstracker.sleep.SleepNightStore.startOf(it.note)?.let { start ->
                                com.example.fitnesstracker.sleep.SleepNightStore.delete(context, start)
                            }
                            vm.deleteRecord(it)
                        },
                        onEdit = { vm.saveRecord(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddSleepInline(selectedDay: Long, onSave: (SleepRecord) -> Unit) {
    var bedMinutes by androidx.compose.runtime.saveable.rememberSaveable(selectedDay) { mutableStateOf(22 * 60 + 30) }
    var wakeMinutes by androidx.compose.runtime.saveable.rememberSaveable(selectedDay) { mutableStateOf(7 * 60) }
    var quality by androidx.compose.runtime.saveable.rememberSaveable(selectedDay) { mutableStateOf(3) }
    var selectedTags by androidx.compose.runtime.saveable.rememberSaveable(selectedDay) { mutableStateOf(arrayListOf<String>()) }
    var saved      by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    fun buildTs(hour: Int, minute: Int, offset: Int = 0): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDay
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_MONTH, offset)
        }
        return cal.timeInMillis
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Bedtime, stringResource(R.string.sleep_add_record),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Tertiary)

            var manualEntry by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

            com.example.fitnesstracker.ui.components.ExpressiveChoiceRow(
                labels = listOf(stringResource(R.string.sleep_manual_entry), stringResource(R.string.sleep_use_dial)),
                icons = listOf(LeafIcons.Keyboard, LeafIcons.Schedule),
                selected = if (manualEntry) 0 else 1, onSelect = { manualEntry = it == 0 })

            androidx.compose.animation.AnimatedContent(
                targetState = manualEntry,
                modifier = Modifier.fillMaxWidth().clipToBounds(),
                contentAlignment = Alignment.TopCenter,
                transitionSpec = {
                    (fadeIn(tween(160)) togetherWith fadeOut(tween(100)))
                        .using(SizeTransform(clip = true, sizeAnimationSpec = { _, _ -> spring(1f, 550f) }))
                },
                label = "dialOrManual"
            ) { manual ->
                if (!manual) {
                    com.example.fitnesstracker.ui.components.SleepClockDial(
                        bedMinutes = bedMinutes,
                        wakeMinutes = wakeMinutes,
                        onBedChange = { bedMinutes = it },
                        onWakeChange = { wakeMinutes = it }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        com.example.fitnesstracker.ui.components.ExpressiveTimeInput(
                            stringResource(R.string.sleep_bedtime), LeafIcons.Bedtime, bedMinutes,
                            onChange = { bedMinutes = it })
                        com.example.fitnesstracker.ui.components.ExpressiveTimeInput(
                            stringResource(R.string.sleep_wake), LeafIcons.WbSunny, wakeMinutes,
                            onChange = { wakeMinutes = it })
                    }
                }
            }


            run {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.fitnesstracker.ui.components.LeafIconBadge(LeafIcons.Bedtime,
                                size = com.example.fitnesstracker.ui.components.LeafIconSize.Small,
                                tone = com.example.fitnesstracker.ui.components.LeafTone.Tertiary)
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.sleep_timeline_title),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        com.example.fitnesstracker.ui.components.SleepTimeline(
                            bedTime = if (wakeMinutes > bedMinutes) buildTs(bedMinutes / 60, bedMinutes % 60)
                                else buildTs(bedMinutes / 60, bedMinutes % 60, -1),
                            wakeTime = buildTs(wakeMinutes / 60, wakeMinutes % 60), quality = quality, showDuration = manualEntry)

                    }
                }
            }

            QualityPicker(
                quality = quality,
                onSelect = { value ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    quality = value
                }
            )

            com.example.fitnesstracker.ui.components.SleepTagPicker(selectedTags.toSet()) {
                selectedTags = ArrayList(it)
            }

            if (saved) {
                Text(stringResource(R.string.sleep_saved), color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium)
            }

            LeafButton(
                enabled = bedMinutes != wakeMinutes,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val bedRaw  = buildTs(bedMinutes / 60, bedMinutes % 60, 0)
                    val wakeTs  = buildTs(wakeMinutes / 60, wakeMinutes % 60, 0)
                    val bedTs   = if (wakeTs > bedRaw) bedRaw else bedRaw - 86400000L
                    onSave(SleepRecord(
                        bedTime = bedTs,
                        wakeTime = wakeTs,
                        qualityScore = quality,
                        tags = selectedTags.joinToString(",")
                    ))
                    saved = true
                    selectedTags = arrayListOf()
                    quality = 3
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.sleep_save_record))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SleepRecordCard(record: SleepRecord, onDelete: (SleepRecord) -> Unit, onEdit: (SleepRecord) -> Unit,
    modifier: Modifier = Modifier, groupIndex: Int = 0, groupCount: Int = 1,
    expanded: Boolean, onToggle: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    var showEdit    by remember { mutableStateOf(false) }
    if (showEdit) {
        EditSleepDialog(record = record, onSave = onEdit, onDismiss = { showEdit = false })
    }
    if (showConfirm) {
        LeafEditSheet(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.sleep_delete_q)) },
            confirmButton = {
                LeafButton(onClick = { onDelete(record); showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.common_cancel)) } }
        )
    }

    val tags = remember(record.tags) { record.tags.split(",").filter { it.isNotBlank() } }
    val tracked = remember(record.note) { com.example.fitnesstracker.sleep.SleepNightStore.startOf(record.note) != null }

    com.example.fitnesstracker.ui.components.HistoryRecordCard(
        title = "${DateUtils.formatTime(record.bedTime)} → ${DateUtils.formatTime(record.wakeTime)}",
        date = DateUtils.formatDate(record.bedTime),
        icon = if (tracked) LeafIcons.GraphicEq else qualityIcons[(record.qualityScore - 1).coerceIn(0, 4)],
        badgeColor = qualityTone(record.qualityScore).first, badgeInk = qualityTone(record.qualityScore).second,
        expanded = expanded, onToggle = onToggle,
        modifier = modifier, groupIndex = groupIndex, groupCount = groupCount,
        onEdit = { showEdit = true }, onDelete = { showConfirm = true }) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(50), color = qualityTone(record.qualityScore).first) {
                Text(qualityLabel(record.qualityScore), Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge, color = qualityTone(record.qualityScore).second)
            }
            if (tracked) Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                Row(Modifier.padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(LeafIcons.GraphicEq, null, Modifier.size(16.dp))
                    Text(stringResource(R.string.b13_tracked_badge), style = MaterialTheme.typography.labelLarge)
                }
            }
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Text(DateUtils.formatDuration(((record.wakeTime - record.bedTime) / 1000L).coerceAtLeast(0L)),
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
            }
        }
        com.example.fitnesstracker.ui.components.SleepTimeline(record.bedTime, record.wakeTime, record.qualityScore)
        SleepNightReport(record.note)
        if (tags.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(LeafIcons.Label, null, Modifier.size(16.dp))
                        Text(tag, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private val qualityIcons = listOf(LeafIcons.SentimentVeryDissatisfied, LeafIcons.SentimentDissatisfied,
    LeafIcons.SentimentNeutral, LeafIcons.SentimentSatisfied, LeafIcons.SentimentVerySatisfied)

@Composable
private fun qualityLabel(quality: Int): String = stringResource(when (quality.coerceIn(1, 5)) {
    1 -> R.string.sleep_quality_1
    2 -> R.string.sleep_quality_2
    3 -> R.string.sleep_quality_3
    4 -> R.string.sleep_quality_4
    else -> R.string.sleep_quality_5
})

@Composable
private fun qualityTone(quality: Int): Pair<Color, Color> {
    val dark = MaterialTheme.colorScheme.surface.luminance() < .5f
    val light = listOf(0xFFF0DFDD to 0xFF78494B, 0xFFEFE3D5 to 0xFF73573D,
        0xFFEEE8D5 to 0xFF69603C, 0xFFDDE9DD to 0xFF46604A, 0xFFD2E7DF to 0xFF355F52)
    val night = listOf(0xFF49383A to 0xFFE8C8CA, 0xFF494034 to 0xFFE6CFB6,
        0xFF454232 to 0xFFDDD5AF, 0xFF354539 to 0xFFC9DBCA, 0xFF30483E to 0xFFBEDACD)
    val pair = (if (dark) night else light)[(quality - 1).coerceIn(0, 4)]
    return Color(pair.first) to Color(pair.second)
}

@Composable
private fun QualityPicker(quality: Int, onSelect: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val palette = MaterialTheme.colorScheme
    val current = quality.coerceIn(1, 5)
    val tone = qualityTone(current)
    Surface(shape = RoundedCornerShape(28.dp), color = palette.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                val container by animateColorAsState(tone.second, tween(220), label = "qualityHero")
                val ink by animateColorAsState(tone.first, tween(220), label = "qualityHeroInk")
                Box(Modifier.size(64.dp).clip(com.example.fitnesstracker.ui.components.LeafRosette(8)).background(container),
                    contentAlignment = Alignment.Center) {
                    AnimatedContent(current, transitionSpec = {
                        (fadeIn(tween(160)) + androidx.compose.animation.scaleIn(spring(.6f, 500f), initialScale = .6f)) togetherWith fadeOut(tween(90))
                    }, label = "qualityFace") { value ->
                        Icon(com.example.fitnesstracker.ui.components.LeafFilledIcons.of(qualityIcons[value - 1]) ?: qualityIcons[value - 1],
                            null, Modifier.size(36.dp), tint = ink)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.sleep_quality), style = MaterialTheme.typography.labelLarge, color = palette.onSurfaceVariant)
                    AnimatedContent(current, transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(90)) },
                        label = "qualityLabel") { value ->
                        Text(qualityLabel(value), style = MaterialTheme.typography.headlineSmall, maxLines = 1)
                    }
                }
                Surface(shape = CircleShape, color = tone.first, contentColor = tone.second) {
                    Text("$current/5", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                qualityIcons.forEachIndexed { index, icon ->
                    val value = index + 1
                    val selected = current == value
                    val pair = qualityTone(value)
                    val weight by animateFloatAsState(if (selected) 1.9f else 1f, spring(.7f, 420f), label = "qualityWeight")
                    val outer by androidx.compose.animation.core.animateDpAsState(if (selected) 28.dp else 10.dp, spring(.8f, 500f),
                        label = "qualityCorner")
                    val start = if (index == 0) 28.dp else outer
                    val end = if (index == qualityIcons.lastIndex) 28.dp else outer
                    val bg by animateColorAsState(if (selected) pair.second else pair.first, tween(200), label = "qualityTone")
                    val ink by animateColorAsState(if (selected) pair.first else pair.second, tween(200), label = "qualityInk")
                    Box(Modifier.weight(weight).height(60.dp)
                        .clip(RoundedCornerShape(topStart = start, bottomStart = start, topEnd = end, bottomEnd = end)).background(bg)
                        .selectable(selected, role = androidx.compose.ui.semantics.Role.RadioButton,
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                            onClick = {
                                if (!selected) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(value) }
                            }), contentAlignment = Alignment.Center) {
                        Icon(if (selected) com.example.fitnesstracker.ui.components.LeafFilledIcons.of(icon) ?: icon else icon,
                            qualityLabel(value), Modifier.size(if (selected) 30.dp else 24.dp), tint = ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityStars(quality: Int, size: Dp = 22.dp, onSelect: ((Int) -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val filled = i <= quality
            val base = Modifier.size(size)
            Icon(
                imageVector = if (filled) LeafIcons.Star else LeafIcons.StarOutline,
                contentDescription = null,
                tint = if (filled) MaterialTheme.colorScheme.tertiary
                       else MaterialTheme.colorScheme.outlineVariant,
                modifier = if (onSelect != null)
                    base.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(i) }
                else base
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditSleepDialog(record: SleepRecord, onSave: (SleepRecord) -> Unit, onDismiss: () -> Unit) {
    fun part(ts: Long, field: Int): Int =
        Calendar.getInstance().apply { timeInMillis = ts }.get(field)

    var bedMinutes  by remember { mutableStateOf(part(record.bedTime, Calendar.HOUR_OF_DAY) * 60 + part(record.bedTime, Calendar.MINUTE)) }
    var wakeMinutes by remember { mutableStateOf(part(record.wakeTime, Calendar.HOUR_OF_DAY) * 60 + part(record.wakeTime, Calendar.MINUTE)) }
    var quality    by remember { mutableStateOf(record.qualityScore) }
    var selectedTags by remember {
        mutableStateOf(record.tags.split(",").filter { it.isNotBlank() }.toSet())
    }


    LeafEditSheet(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sleep_edit)) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                com.example.fitnesstracker.ui.components.SleepClockDial(
                    bedMinutes = bedMinutes,
                    wakeMinutes = wakeMinutes,
                    onBedChange = { bedMinutes = it },
                    onWakeChange = { wakeMinutes = it }
                )
                QualityPicker(
                    quality = quality,
                    onSelect = { value -> quality = value }
                )
                com.example.fitnesstracker.ui.components.SleepTagPicker(selectedTags) { selectedTags = it }
            }
        },
        confirmButton = {
            LeafButton(
                enabled = bedMinutes != wakeMinutes,
                onClick = {
                    fun rebuild(base: Long, h: Int, m: Int): Long =
                        Calendar.getInstance().apply {
                            timeInMillis = base
                            set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0)
                        }.timeInMillis
                    val newBed  = rebuild(record.bedTime, bedMinutes / 60, bedMinutes % 60)
                    var newWake = rebuild(record.bedTime, wakeMinutes / 60, wakeMinutes % 60)
                    if (newWake <= newBed) newWake += 86400000L
                    onSave(record.copy(
                        bedTime = newBed, wakeTime = newWake,
                        qualityScore = quality,
                        tags = selectedTags.joinToString(",")
                    ))
                    onDismiss()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Composable
private fun SleepWeekChart(hours: List<Float>) {
    val values = List(7) { hours.getOrElse(it) { 0f }.let { n -> if (n.isFinite()) n.coerceAtLeast(0f) else 0f } }
    val recorded = values.filter { it > 0f }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Bedtime, stringResource(R.string.sleep_week_title),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Tertiary)
            com.example.fitnesstracker.ui.components.LeafWeekHistory(
                com.example.fitnesstracker.ui.components.LeafWeekMetric.SLEEP, values,
                stringResource(R.string.unit_h), MaterialTheme.colorScheme.tertiary, decimals = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepDateSelectDialog(selectedDay: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    com.example.fitnesstracker.ui.components.LeafDatePicker(selectedDay, onSelect, onDismiss)
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SleepSummaryCard(history: List<SleepRecord>, selectedDay: Long) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = MaterialTheme.colorScheme
    val goal = remember { com.example.fitnesstracker.utils.GoalPreferences.getSleepGoalMinutes(context).coerceIn(60, 16 * 60) }
    val revision by com.example.fitnesstracker.sleep.SleepAlarms.revision.collectAsState()
    val alarm = remember(revision) { com.example.fitnesstracker.sleep.SleepAlarms.upcoming(context) }
    val today = remember(selectedDay) { DateUtils.dayStart(selectedDay) == DateUtils.todayStart() }
    val dayEnd = remember(selectedDay) { DateUtils.dayEnd(selectedDay) }
    val dayStart = remember(selectedDay) { DateUtils.dayStart(selectedDay) }
    val recent = remember(history, dayEnd) { history.filter { it.wakeTime <= dayEnd }.sortedByDescending { it.wakeTime }.take(7) }
    val last = remember(recent, today, dayStart) {
        if (today) recent.firstOrNull() else recent.firstOrNull { it.wakeTime >= dayStart || it.bedTime >= dayStart }
    }
    fun minutes(record: SleepRecord) = ((record.wakeTime - record.bedTime) / 60000L).toInt().coerceAtLeast(0)
    Surface(shape = RoundedCornerShape(32.dp), color = colors.primaryContainer, contentColor = colors.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            com.example.fitnesstracker.ui.components.LeafCardHeader(LeafIcons.Bedtime, stringResource(R.string.b14_summary_title),
                subtitle = stringResource(R.string.b14_summary_goal, goal / 60, goal % 60),
                tone = com.example.fitnesstracker.ui.components.LeafTone.Solid)
            if (last == null) Text(stringResource(if (recent.isEmpty()) R.string.b14_summary_empty else R.string.b15_summary_no_day,
                DateUtils.formatDate(selectedDay)), style = MaterialTheme.typography.bodyLarge)
            if (last != null || recent.isNotEmpty()) {
                if (last != null) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val slept = minutes(last)
                    Text(if (today) stringResource(R.string.b14_summary_last) else DateUtils.formatDate(last.wakeTime),
                        style = MaterialTheme.typography.labelLarge)
                    com.example.fitnesstracker.ui.components.LeafFitText(stringResource(R.string.b14_summary_duration, slept / 60, slept % 60),
                        style = MaterialTheme.typography.displayMedium)
                    com.example.fitnesstracker.ui.components.LeafWavyProgress(slept.toFloat() / goal, true,
                        Modifier.fillMaxWidth().height(18.dp), colors.primary, colors.onPrimaryContainer.copy(alpha = .16f))
                }
                val average = recent.sumOf { minutes(it) } / recent.size
                val debt = recent.sumOf { (goal - minutes(it)).coerceAtLeast(0) }
                val beds = recent.map { record ->
                    val calendar = Calendar.getInstance().apply { timeInMillis = record.bedTime }
                    (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) + 12 * 60) % (24 * 60)
                }
                val bedAverage = beds.sum() / beds.size
                val spread = if (beds.size < 2) 0 else beds.maxOf { kotlin.math.abs(it - bedAverage) }
                val bedClock = (bedAverage + 12 * 60) % (24 * 60)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        SummaryTile(stringResource(R.string.b14_summary_duration, average / 60, average % 60),
                            stringResource(R.string.b14_summary_average), 0, Modifier.weight(1f))
                        SummaryTile(stringResource(R.string.b14_summary_duration, debt / 60, debt % 60),
                            stringResource(R.string.b14_summary_debt), 1, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        SummaryTile("%02d:%02d".format(bedClock / 60, bedClock % 60), stringResource(R.string.b14_summary_bedtime), 2,
                            Modifier.weight(1f))
                        SummaryTile(stringResource(when { spread <= 30 -> R.string.b14_regular_high; spread <= 75 -> R.string.b14_regular_mid
                            else -> R.string.b14_regular_low }), stringResource(R.string.b14_summary_regular), 3, Modifier.weight(1f))
                    }
                }
            }
            if (alarm != null && today) {
                val bedtime = alarm.second - (goal + 15) * 60000L
                Surface(shape = RoundedCornerShape(20.dp), color = colors.primary, contentColor = colors.onPrimary,
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(LeafIcons.Lightbulb, null, Modifier.size(22.dp))
                        Text(stringResource(R.string.b14_summary_advice, DateUtils.formatTime(bedtime), DateUtils.formatTime(alarm.second)),
                            Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTile(value: String, label: String, index: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(topStart = if (index == 0) 20.dp else 6.dp, topEnd = if (index == 1) 20.dp else 6.dp,
        bottomStart = if (index == 2) 20.dp else 6.dp, bottomEnd = if (index == 3) 20.dp else 6.dp),
        color = colors.surface.copy(alpha = .55f), contentColor = colors.onSurface, modifier = modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            com.example.fitnesstracker.ui.components.LeafFitText(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}
