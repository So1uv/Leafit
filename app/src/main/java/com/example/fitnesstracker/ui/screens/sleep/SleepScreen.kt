package com.example.fitnesstracker.ui.screens.sleep

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
    val history       by vm.history.collectAsState()
    val selectedDay   by vm.selectedDay.collectAsState()
    val weekHours     by vm.weekSleepHours.collectAsState()

    val newestId = history.maxByOrNull { it.wakeTime }?.id
    var expandedRecordId by androidx.compose.runtime.saveable.rememberSaveable(newestId) { mutableStateOf(newestId) }
    val listState = rememberLazyListState()
    var showDatePicker by remember { mutableStateOf(false) }

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



            item {
                AddSleepInline(
                    selectedDay = selectedDay,
                    onSave = { vm.saveRecord(it) }
                )
            }

            item {
                SleepWeekChart(hours = weekHours)
            }

            item {
                com.example.fitnesstracker.ui.components.HistorySectionLabel(
                    stringResource(R.string.sleep_history_title), history.size, LeafIcons.Bedtime)
            }

            if (history.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(com.example.fitnesstracker.ui.components.LeafIcons.Bedtime, null, modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(R.string.sleep_records_empty), style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.sleep_records_empty_hint), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(history, key = { _, record -> record.id }) { index, record ->
                    SleepRecordCard(
                        record = record, expanded = expandedRecordId == record.id,
                        onToggle = { expandedRecordId = if (expandedRecordId == record.id) null else record.id },
                        modifier = Modifier.animateItem(placementSpec = null), groupIndex = index, groupCount = history.size,
                        onDelete = { vm.deleteRecord(it) },
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
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Box(Modifier.size(44.dp), Alignment.Center) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Bedtime, null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Text(stringResource(R.string.sleep_add_record), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

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
                val durMin = com.example.fitnesstracker.utils.SleepTimeMath.durationMinutes(bedMinutes, wakeMinutes)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.Bedtime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.sleep_cycles_title),
                                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f))
                            val cycles = com.example.fitnesstracker.ui.components.cycleCount(durMin)
                            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                                Text(stringResource(R.string.sleep_cycles_count, cycles),
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        com.example.fitnesstracker.ui.components.SleepHypnogram(durationMinutes = durMin, startMinutes = bedMinutes)
                        Spacer(Modifier.height(12.dp))
                        com.example.fitnesstracker.ui.components.SleepCycleSummary(durMin)

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

    val durationH = (record.wakeTime - record.bedTime) / 3600000f
    val tags = remember(record.tags) { record.tags.split(",").filter { it.isNotBlank() } }

    com.example.fitnesstracker.ui.components.HistoryRecordCard(
        title = "${DateUtils.formatTime(record.bedTime)} → ${DateUtils.formatTime(record.wakeTime)}",
        date = DateUtils.formatDate(record.bedTime),
        icon = qualityIcons[(record.qualityScore - 1).coerceIn(0, 4)],
        badgeColor = qualityTone(record.qualityScore).first, badgeInk = qualityTone(record.qualityScore).second,
        expanded = expanded, onToggle = onToggle,
        modifier = modifier, groupIndex = groupIndex, groupCount = groupCount,
        onEdit = { showEdit = true }, onDelete = { showConfirm = true }) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${"%.1f".format(durationH)} ${stringResource(R.string.unit_h)}",
                style = MaterialTheme.typography.headlineSmall)
            Surface(shape = RoundedCornerShape(50), color = qualityTone(record.qualityScore).first) {
                Text(qualityLabel(record.qualityScore), Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge, color = qualityTone(record.qualityScore).second)
            }
        }
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
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val palette = MaterialTheme.colorScheme
    val (selectedBg, selectedFg) = qualityTone(quality)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.sleep_quality), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            qualityIcons.forEachIndexed { index, icon ->
                val selected = quality == index + 1
                val (container, foreground) = qualityTone(index + 1)
                val label = qualityLabel(index + 1)
                val interactions = remember { MutableInteractionSource() }
                val pressed by interactions.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed) 0.9f else if (selected) 1.08f else 1f,
                    spring(dampingRatio = 0.7f, stiffness = 650f), label = "qualitySelection")
                val bg by animateColorAsState(if (selected) androidx.compose.ui.graphics.lerp(container, foreground, .13f) else container,
                    tween(180), label = "qualityTone")
                val corner by androidx.compose.animation.core.animateDpAsState(if (selected) 18.dp else 32.dp,
                    spring(.85f, 500f), label = "qualityCorner")
                Box(Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(corner))
                    .background(bg).selectable(selected, role = androidx.compose.ui.semantics.Role.RadioButton,
                        interactionSource = interactions, indication = null,
                        onClick = { if (!selected) { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onSelect(index + 1) } }), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(icon, label, tint = foreground,
                            modifier = Modifier.size(30.dp).graphicsLayer { scaleX = scale; scaleY = scale })
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(qualityLabel(1), style = MaterialTheme.typography.labelSmall, color = palette.onSurfaceVariant,
                modifier = Modifier.weight(1f))
            Text(qualityLabel(5), style = MaterialTheme.typography.labelSmall, color = palette.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1f))
        }
        Surface(shape = RoundedCornerShape(18.dp), color = selectedBg) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(qualityIcons[(quality - 1).coerceIn(0, 4)], null, tint = selectedFg, modifier = Modifier.size(20.dp))
                LeafTitle(qualityLabel(quality), style = MaterialTheme.typography.labelLarge, color = selectedFg,
                    modifier = Modifier.weight(1f))
                Text("$quality / 5", style = MaterialTheme.typography.labelMedium, color = selectedFg)
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
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Box(Modifier.size(48.dp), Alignment.Center) {
                        Icon(LeafIcons.Bedtime, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.sleep_week_title), style = MaterialTheme.typography.titleLarge)
                }
            }
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

