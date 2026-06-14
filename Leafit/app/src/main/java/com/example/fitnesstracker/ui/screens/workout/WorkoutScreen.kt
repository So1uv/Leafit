package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.ui.components.AppTextField
import com.example.fitnesstracker.ui.components.bouncyClick
import com.example.fitnesstracker.ui.components.ExpressiveDialog
import com.example.fitnesstracker.ui.components.LargeScreenTitle
import com.example.fitnesstracker.ui.components.MetricText
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(vm: WorkoutViewModel) {
    val isRunning        by vm.isRunning.collectAsState()
    val isPaused         by vm.isPaused.collectAsState()
    val elapsed          by vm.elapsedSeconds.collectAsState()
    val calories         by vm.estimatedCalories.collectAsState()
    val history          by vm.history.collectAsState()
    val workoutSteps     by vm.workoutSteps.collectAsState()
    val sensorAvailable  by vm.stepsSensorAvailable.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var workoutNote    by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDay    by remember { mutableStateOf(DateUtils.todayStart()) }

    val listState = rememberLazyListState()
    val collapsedTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 24 }
    }
    val topTitleAlpha by animateFloatAsState(if (collapsedTitle) 1f else 0f, tween(180), label = "topTitle")
    val topBarColor by animateColorAsState(
        if (collapsedTitle) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        tween(220), label = "topBarBg"
    )

    val timerArc by animateFloatAsState(
        targetValue = if (isRunning) (elapsed % 60) / 60f else 0f,
        animationSpec = tween(500, easing = EaseOutCubic), label = "arc"
    )
    val primary   = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    val haptic    = LocalHapticFeedback.current

    val animCalories by animateFloatAsState(calories, tween(600, easing = EaseOutCubic), label = "calAnim")

    // «Жива» крапка статусу під час активного тренування
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotPulse by pulse.animateFloat(
        initialValue = 1f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dotPulse"
    )
    // Картка таймера м'яко підсвічується кольором під час тренування
    val timerCardColor by animateColorAsState(
        targetValue = if (isRunning && !isPaused)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(500), label = "timerCardColor"
    )

    if (showDatePicker) {
        WorkoutDatePicker(
            selected = selectedDay,
            onSelect = { selectedDay = it; showDatePicker = false },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showSaveDialog) {
        ExpressiveDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Зберегти тренування?") },
            content = {
                AppTextField(
                    value = workoutNote, onValueChange = { workoutNote = it },
                    label = "Нотатка (необов'язково)", modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { vm.stopAndSave(workoutNote); workoutNote = ""; showSaveDialog = false },
                    shape = RoundedCornerShape(50)) { Text("Зберегти") }
            },
            dismissButton = {
                OutlinedButton(onClick = { vm.stopAndDiscard(); showSaveDialog = false },
                    shape = RoundedCornerShape(50)) { Text("Видалити") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LargeScreenTitle("Тренування", icon = Icons.Default.FitnessCenter)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.bouncyClick { showDatePicker = true }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    if (selectedDay == DateUtils.todayStart()) "сьогодні"
                                    else DateUtils.formatDate(selectedDay),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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


            // Таймер-картка — M3 Expressive: статус-чіп, кільце з крапкою-наконечником,
            // «матові» чіпи статистики та pill-кнопки керування
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(28.dp),
                    colors   = CardDefaults.cardColors(containerColor = timerCardColor),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Статус-чіп з «живою» крапкою
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = when {
                                isRunning && !isPaused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                isPaused               -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                else                   -> MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Box(
                                    Modifier.size(7.dp).clip(CircleShape)
                                        .graphicsLayer { alpha = if (isRunning && !isPaused) dotPulse else 1f }
                                        .background(
                                            when {
                                                isRunning && !isPaused -> MaterialTheme.colorScheme.primary
                                                isPaused               -> MaterialTheme.colorScheme.tertiary
                                                else                   -> MaterialTheme.colorScheme.outline
                                            }
                                        )
                                )
                                Text(
                                    text = when {
                                        isPaused      -> "на паузі"
                                        isRunning     -> "активне тренування"
                                        elapsed > 0   -> "завершено"
                                        else          -> "готово до старту"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        isRunning && !isPaused -> MaterialTheme.colorScheme.primary
                                        isPaused               -> MaterialTheme.colorScheme.onTertiaryContainer
                                        else                   -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // Кільце таймера: товстий трек, дуга з заокругленням і крапка-наконечник
                        Box(
                            modifier = Modifier.size(176.dp).drawBehind {
                                val stroke = 13.dp.toPx()
                                val inset = stroke / 2f
                                val arcSize = Size(size.width - stroke, size.height - stroke)
                                drawArc(
                                    primary.copy(alpha = 0.12f), -90f, 360f, false,
                                    Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                                if (isRunning || elapsed > 0) {
                                    val sweep = 360f * timerArc
                                    drawArc(
                                        primary, -90f, sweep, false,
                                        Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                                    )
                                    // Крапка-наконечник на кінці дуги
                                    val angle = Math.toRadians((sweep - 90f).toDouble())
                                    val r = arcSize.width / 2f
                                    val cx = size.width / 2f + r * kotlin.math.cos(angle).toFloat()
                                    val cy = size.height / 2f + r * kotlin.math.sin(angle).toFloat()
                                    drawCircle(primary, radius = stroke * 0.72f, center = Offset(cx, cy))
                                    drawCircle(container, radius = stroke * 0.32f, center = Offset(cx, cy))
                                }
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                MetricText(
                                    DateUtils.formatDuration(elapsed),
                                    style = MaterialTheme.typography.displaySmall,
                                    weight = FontWeight.Black,
                                    color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "хв : сек",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Чіпи статистики у стилі water dock — матові плашки на картці
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            WorkoutStatChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.DirectionsWalk,
                                value = if (isRunning || workoutSteps > 0) "$workoutSteps" else "—",
                                label = if (!sensorAvailable) "кроки · нема датчика" else "кроки",
                                active = isRunning && sensorAvailable
                            )
                            WorkoutStatChip(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${animCalories.roundToInt()}",
                                label = "ккал",
                                active = isRunning
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        if (!isRunning) {
                            Button(
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.startWorkout() },
                                modifier = Modifier.fillMaxWidth().height(58.dp),
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Почати тренування", style = MaterialTheme.typography.titleSmall)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (isPaused) {
                                    FilledTonalButton(
                                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.resumeWorkout() },
                                        modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Продовжити", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); vm.pauseWorkout() },
                                        modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Rounded.Pause, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Пауза", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Button(
                                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); showSaveDialog = true },
                                    modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Rounded.Stop, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Стоп", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Список тренувань за обраний день
            val dayWorkouts = history.filter {
                it.startTime in DateUtils.dayStart(selectedDay) until DateUtils.dayEnd(selectedDay)
            }

            item {
                Text(
                    if (selectedDay == DateUtils.todayStart()) "Тренування за сьогодні"
                    else "Тренування за день",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            if (dayWorkouts.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (selectedDay == DateUtils.todayStart()) "Тренувань ще немає"
                                else "У цей день тренувань не було",
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (selectedDay == DateUtils.todayStart()) "Натисніть «Почати тренування» вище"
                                else "Оберіть іншу дату",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(dayWorkouts, key = { it.id }) { workout ->
                    WorkoutHistoryCard(workout = workout, onDelete = { vm.deleteWorkout(it) })
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(workout: Workout, onDelete: (Workout) -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        ExpressiveDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Видалити тренування?") },
            confirmButton = {
                Button(onClick = { onDelete(workout); showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(50)) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Скасувати") }
            }
        )
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) {
                Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(DateUtils.formatDate(workout.startTime), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(DateUtils.formatDuration(workout.durationSeconds),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("≈ ${workout.caloriesBurned.roundToInt()} ккал  •  ${workout.steps} кр.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (workout.note.isNotBlank())
                    Text(workout.note, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/** Матовий чіп статистики всередині таймер-картки */
@Composable
private fun WorkoutStatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    active: Boolean
) {
    Surface(
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (active) 0.16f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null, modifier = Modifier.size(19.dp),
                    tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                MetricText(
                    value,
                    style = MaterialTheme.typography.headlineSmall,
                    weight = FontWeight.Black,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun WeekStatTile(modifier: Modifier, value: String, label: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            MetricText(value, style = MaterialTheme.typography.titleLarge, weight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDatePicker(selected: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = selected)
    val colors = androidx.compose.material3.DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
        weekdayContentColor = MaterialTheme.colorScheme.primary
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        colors = colors,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onSelect(DateUtils.dayStart(it)) } ?: onDismiss()
            }, shape = RoundedCornerShape(50)) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Скасувати") } }
    ) {
        androidx.compose.material3.DatePicker(
            state = state,
            colors = colors,
            title = {
                Text("Виберіть дату",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp))
            }
        )
    }
}
