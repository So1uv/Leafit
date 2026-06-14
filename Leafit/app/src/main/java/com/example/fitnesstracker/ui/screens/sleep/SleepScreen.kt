package com.example.fitnesstracker.ui.screens.sleep

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.ExpressiveDialog
import com.example.fitnesstracker.ui.components.LargeScreenTitle
import com.example.fitnesstracker.ui.components.MetricText
import com.example.fitnesstracker.ui.components.bouncyClick
import com.example.fitnesstracker.ui.components.StepperIntField
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.viewmodel.SleepViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(vm: SleepViewModel, onBack: () -> Unit = {}) {
    val history       by vm.history.collectAsState()
    val selectedDay   by vm.selectedDay.collectAsState()
    val weekHours     by vm.weekSleepHours.collectAsState()

    val listState = rememberLazyListState()
    val collapsedTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24 }
    }
    val topTitleAlpha by animateFloatAsState(if (collapsedTitle) 1f else 0f, tween(180), label = "topTitle")
    val topBarColor by animateColorAsState(
        if (collapsedTitle) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        tween(220), label = "topBarBg"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Bedtime, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    "Сон",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarColor)
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item { Spacer(Modifier.height(2.dp)) }

            // Inline форма додавання сну
            item {
                AddSleepInline(
                    selectedDay = selectedDay,
                    onSave = { vm.saveRecord(it) }
                )
            }

            // Міні-графік сну за тиждень
            item {
                SleepWeekChart(hours = weekHours)
            }

            if (history.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bedtime, null, modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Записів сну немає", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text("Заповніть форму вище", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(history, key = { it.id }) { record ->
                    SleepRecordCard(
                        record = record,
                        onDelete = { vm.deleteRecord(it) },
                        onEdit = { vm.saveRecord(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSleepInline(selectedDay: Long, onSave: (SleepRecord) -> Unit) {
    val availableTags = listOf("Хропіння","Різкі пробудження","Кошмари","Безсоння","Добрий сон")
    var bedHour    by remember { mutableStateOf(22) }
    var bedMinute  by remember { mutableStateOf(30) }
    var wakeHour   by remember { mutableStateOf(7) }
    var wakeMinute by remember { mutableStateOf(0) }
    var quality    by remember { mutableStateOf(3) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Bedtime, null, tint = MaterialTheme.colorScheme.tertiary)
                Text("Додати запис сну", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            Text("Час відходу", style = MaterialTheme.typography.labelLarge)
            TimeRow(bedHour, bedMinute, { bedHour = it }, { bedMinute = it })

            Text("Час пробудження", style = MaterialTheme.typography.labelLarge)
            TimeRow(wakeHour, wakeMinute, { wakeHour = it }, { wakeMinute = it })

            QualityPicker(
                quality = quality,
                onSelect = { value ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    quality = value
                }
            )

            Text("Теги", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                availableTags.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { tag ->
                            TagChip(
                                text = tag,
                                selected = tag in selectedTags,
                                onClick = {
                                    selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                                }
                            )
                        }
                    }
                }
            }

            if (saved) {
                Text("✓ Збережено!", color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val bedTs  = buildTs(bedHour, bedMinute, if (bedHour < 6) -1 else 0)
                    val wakeTs = buildTs(wakeHour, wakeMinute, 0)
                    onSave(SleepRecord(
                        bedTime = bedTs,
                        wakeTime = if (wakeTs > bedTs) wakeTs else wakeTs + 86400000L,
                        qualityScore = quality,
                        tags = selectedTags.joinToString(",")
                    ))
                    saved = true
                    selectedTags = setOf()
                    quality = 3
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Зберегти запис")
            }
        }
    }
}

@Composable
private fun SleepRecordCard(record: SleepRecord, onDelete: (SleepRecord) -> Unit, onEdit: (SleepRecord) -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    var showEdit    by remember { mutableStateOf(false) }
    if (showEdit) {
        EditSleepDialog(record = record, onSave = onEdit, onDismiss = { showEdit = false })
    }
    if (showConfirm) {
        ExpressiveDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Видалити запис?") },
            confirmButton = {
                Button(onClick = { onDelete(record); showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)) { Text("Видалити") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Скасувати") } }
        )
    }

    val durationH = (record.wakeTime - record.bedTime) / 3600000f
    val tags      = record.tags.split(",").filter { it.isNotBlank() }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer), Alignment.Center) {
                    Icon(Icons.Default.Bedtime, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(DateUtils.formatDate(record.bedTime), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${DateUtils.formatTime(record.bedTime)} → ${DateUtils.formatTime(record.wakeTime)}",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${"%.1f".format(durationH)} год", style = MaterialTheme.typography.bodySmall)
                        QualityStars(quality = record.qualityScore, size = 14.dp)
                    }
                }
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showConfirm = true }) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.take(3).forEach { tag ->
                        Box(Modifier.clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(tag, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRow(hour: Int, minute: Int, onHour: (Int) -> Unit, onMinute: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        StepperIntField(value = hour, label = "Год", range = 0..23, onChange = onHour)
        Text(":", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        StepperIntField(value = minute, label = "Хв", range = 0..59, step = 5, onChange = onMinute)
    }
}

@Composable
private fun QualityPicker(quality: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("Важко", "Нестабільно", "Нормально", "Добре", "Відмінно")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.46f)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Якість сну", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(labels.getOrElse((quality - 1).coerceIn(0, 4)) { "Нормально" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier.clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.62f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    MetricText("$quality/5", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, weight = FontWeight.Black)
                }
            }
            QualityStars(quality = quality, size = 30.dp, onSelect = onSelect)
        }
    }
}

/** Векторні зірки якості сну; за наявності onSelect — клікабельні */
@Composable
private fun QualityStars(quality: Int, size: Dp = 22.dp, onSelect: ((Int) -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val filled = i <= quality
            val base = Modifier.size(size)
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
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

/** Тег сну: м'яка заливка без рамок, з галочкою при виборі */
@Composable
private fun TagChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "tagBg"
    )
    Row(
        Modifier.clip(RoundedCornerShape(50.dp))
            .background(bg)
            .bouncyClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (selected) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer)
        }
        Text(text, style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Діалог редагування запису сну */
@Composable
private fun EditSleepDialog(record: SleepRecord, onSave: (SleepRecord) -> Unit, onDismiss: () -> Unit) {
    fun part(ts: Long, field: Int): Int =
        Calendar.getInstance().apply { timeInMillis = ts }.get(field)

    var bedHour    by remember { mutableStateOf(part(record.bedTime, Calendar.HOUR_OF_DAY)) }
    var bedMinute  by remember { mutableStateOf(part(record.bedTime, Calendar.MINUTE)) }
    var wakeHour   by remember { mutableStateOf(part(record.wakeTime, Calendar.HOUR_OF_DAY)) }
    var wakeMinute by remember { mutableStateOf(part(record.wakeTime, Calendar.MINUTE)) }
    var quality    by remember { mutableStateOf(record.qualityScore) }
    var selectedTags by remember {
        mutableStateOf(record.tags.split(",").filter { it.isNotBlank() }.toSet())
    }
    val availableTags = listOf("Хропіння","Різкі пробудження","Кошмари","Безсоння","Добрий сон")

    ExpressiveDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редагувати сон") },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Час відходу", style = MaterialTheme.typography.labelLarge)
                TimeRow(bedHour, bedMinute, { bedHour = it }, { bedMinute = it })
                Text("Час пробудження", style = MaterialTheme.typography.labelLarge)
                TimeRow(wakeHour, wakeMinute, { wakeHour = it }, { wakeMinute = it })
                QualityPicker(
                    quality = quality,
                    onSelect = { value -> quality = value }
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableTags.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { tag ->
                                TagChip(
                                    text = tag,
                                    selected = tag in selectedTags,
                                    onClick = {
                                        selectedTags = if (tag in selectedTags) selectedTags - tag
                                                       else selectedTags + tag
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    fun rebuild(base: Long, h: Int, m: Int): Long =
                        Calendar.getInstance().apply {
                            timeInMillis = base
                            set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0)
                        }.timeInMillis
                    val newBed  = rebuild(record.bedTime, bedHour, bedMinute)
                    var newWake = rebuild(record.wakeTime, wakeHour, wakeMinute)
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
            ) { Text("Зберегти") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@Composable
private fun SleepWeekChart(hours: List<Float>) {
    val maxH = (hours.maxOrNull() ?: 0f).coerceAtLeast(8f)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Тиждень сну", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                val activeDays = hours.filter { it > 0f }
                if (activeDays.isNotEmpty()) {
                    MetricText("сер. ${"%.1f".format(activeDays.sum() / activeDays.size)} год",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary, weight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf("Пн","Вт","Ср","Чт","Пт","Сб","Нд").forEachIndexed { idx, label ->
                    val h      = hours.getOrElse(idx) { 0f }
                    val active = h > 0f
                    val frac   = (h / maxH).coerceIn(0f, 1f)
                    val target = if (active) (12f + 44f * frac) else 6f
                    val animH by animateDpAsState(
                        targetValue   = target.dp,
                        animationSpec = tween(700, idx * 60, EaseOutCubic),
                        label = "sleepBar$idx"
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(22.dp).height(58.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                Modifier.width(22.dp).height(animH)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
