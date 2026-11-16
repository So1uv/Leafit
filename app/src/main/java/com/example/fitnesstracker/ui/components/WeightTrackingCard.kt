package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton as FilledTonalIconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton
import com.example.fitnesstracker.ui.components.LeafConfirmDialog as AlertDialog

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafButton
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.WeightRecord
import com.example.fitnesstracker.utils.DateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackingCard(history: List<WeightRecord>, onAdd: (Float) -> Unit,
    onUpdate: (WeightRecord, (Boolean) -> Unit) -> Unit,
    onDelete: (WeightRecord, (Boolean) -> Unit) -> Unit, modifier: Modifier = Modifier, embedded: Boolean = false) {
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    val points = remember(history) { history.filter { it.weightKg.isFinite() }.sortedBy { it.date }.takeLast(14) }
    val latest = points.lastOrNull()
    val delta = if (points.size > 1) points.last().weightKg - points[points.lastIndex - 1].weightKg else null
    val colors = MaterialTheme.colorScheme
    var selected by remember(points) { mutableIntStateOf(points.lastIndex) }
    val shown = points.getOrNull(selected) ?: latest
    if (showAdd) WeightAddSheet(latest?.weightKg, onDismiss = { showAdd = false }) { onAdd(it); showAdd = false }
    if (showHistory) WeightHistorySheet(history, onUpdate, onDelete) { showHistory = false }
    Card(onClick = { showHistory = true }, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (embedded) Color.Transparent else colors.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LeafCardHeader(LeafIcons.MonitorWeight, stringResource(R.string.profile_weight_tracking),
                subtitle = shown?.let { DateUtils.formatDate(it.date) }, tone = LeafTone.Tertiary) {
                FilledTonalIconButton(onClick = { showAdd = true }) {
                    Icon(LeafIcons.Add, stringResource(R.string.profile_weight_hint))
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricText(shown?.let { String.format(Locale.getDefault(), "%.1f", it.weightKg) } ?: "—",
                    style = MaterialTheme.typography.displaySmall)
                Text(stringResource(R.string.unit_kg), style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.weight(1f))
                if (delta != null) {
                    val down = delta < 0f
                    Surface(shape = CircleShape, color = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer) {
                        Row(Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(if (down) LeafIcons.KeyboardArrowDown else LeafIcons.KeyboardArrowUp, null, Modifier.size(18.dp))
                            Text(String.format(Locale.getDefault(), "%+.1f", delta), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            if (points.size >= 2) {
                val description = stringResource(R.string.weight_chart_description,
                    String.format(Locale.getDefault(), "%.1f", points.first().weightKg),
                    String.format(Locale.getDefault(), "%.1f", points.last().weightKg))
                LeafLineChart(points.map { it.weightKg }, points.map { it.date }, selected, { selected = it },
                    Modifier.fillMaxWidth().height(148.dp).semantics { contentDescription = description }, colors.tertiary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(DateUtils.formatDate(points.first().date), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(DateUtils.formatDate(points.last().date), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeightStat(stringResource(R.string.b11_weight_min), points.minOf { it.weightKg }, Modifier.weight(1f))
                    WeightStat(stringResource(R.string.b11_weight_max), points.maxOf { it.weightKg }, Modifier.weight(1f))
                    WeightStat(stringResource(R.string.b11_weight_period), points.last().weightKg - points.first().weightKg,
                        Modifier.weight(1f), signed = true)
                }
            } else {
                Text(stringResource(if (points.isEmpty()) R.string.weight_empty else R.string.weight_one_record),
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WeightStat(label: String, value: Float, modifier: Modifier = Modifier, signed: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerHigh) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LeafFitText(String.format(Locale.getDefault(), if (signed) "%+.1f" else "%.1f", value), style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun LeafLineChart(values: List<Float>, stamps: List<Long>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(values.size) { reveal.snapTo(0f); reveal.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val guide = colors.outlineVariant
    val surface = colors.surfaceContainerLow
    val current by rememberUpdatedState(selected)
    fun nearest(x: Float, width: Float): Int {
        if (values.size < 2) return 0
        val first = stamps.first()
        val span = (stamps.last() - first).coerceAtLeast(1L)
        val inset = width * .04f
        var best = 0
        var gap = Float.MAX_VALUE
        stamps.forEachIndexed { index, stamp ->
            val px = inset + ((stamp - first).toDouble() / span * (width - inset * 2)).toFloat()
            val d = kotlin.math.abs(px - x)
            if (d < gap) { gap = d; best = index }
        }
        return best
    }
    Canvas(modifier.pointerInput(values, stamps) {
        detectTapGestures { offset ->
            val index = nearest(offset.x, size.width.toFloat())
            if (index != current) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(index) }
        }
    }.pointerInput(values, stamps) {
        detectHorizontalDragGestures { change, _ ->
            val index = nearest(change.position.x, size.width.toFloat())
            if (index != current) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(index) }
        }
    }) {
        if (values.size < 2) return@Canvas
        val insetX = size.width * .04f
        val top = 14.dp.toPx()
        val bottom = size.height - 10.dp.toPx()
        val low = values.min()
        val high = values.max()
        val pad = ((high - low) * .18f).coerceAtLeast(.2f)
        val min = low - pad
        val range = (high + pad - min).coerceAtLeast(.4f)
        val first = stamps.first()
        val span = (stamps.last() - first).coerceAtLeast(1L)
        val offsets = values.mapIndexed { index, value ->
            Offset(insetX + ((stamps[index] - first).toDouble() / span * (size.width - insetX * 2)).toFloat(),
                top + (bottom - top) * (1f - (value - min) / range))
        }
        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx()))
        repeat(3) { step ->
            val y = top + (bottom - top) * step / 2f
            drawLine(guide, Offset(0f, y), Offset(size.width, y), 1.dp.toPx(), pathEffect = dash)
        }
        val line = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            for (i in 1 until offsets.size) {
                val a = offsets[i - 1]
                val b = offsets[i]
                val mid = (a.x + b.x) / 2f
                cubicTo(mid, a.y, mid, b.y, b.x, b.y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(offsets.last().x, size.height)
            lineTo(offsets.first().x, size.height)
            close()
        }
        clipRect(right = size.width * reveal.value) {
            drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = .32f), color.copy(alpha = 0f)), top, size.height))
            drawPath(line, color, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            offsets.forEachIndexed { index, point ->
                if (index != current) {
                    drawCircle(surface, 5.dp.toPx(), point)
                    drawCircle(color, 3.dp.toPx(), point)
                }
            }
        }
        offsets.getOrNull(current)?.let { point ->
            drawLine(color.copy(alpha = .35f), Offset(point.x, top - 8.dp.toPx()), Offset(point.x, size.height), 2.dp.toPx(),
                cap = StrokeCap.Round)
            drawCircle(color.copy(alpha = .22f), 14.dp.toPx() * reveal.value, point)
            drawCircle(color, 8.dp.toPx(), point)
            drawCircle(surface, 3.5.dp.toPx(), point)
        }
    }
}

@Composable
private fun WeightAddSheet(latest: Float?, onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val start = latest?.takeIf { it in 20f..350f } ?: 70f
    var input by rememberSaveable { mutableStateOf(String.format(Locale.US, "%.1f", start)) }
    val number = input.replace(',', '.').toFloatOrNull()
    val valid = number != null && number.isFinite() && number in 20f..350f
    fun shift(by: Float) {
        val base = number ?: start
        input = String.format(Locale.US, "%.1f", (base + by).coerceIn(20f, 350f))
    }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.b11_weight_add_title)) },
        confirmButton = {
            LeafButton(enabled = valid, onClick = { number?.let(onSave) }) {
                Icon(LeafIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { shift(-.1f) }, modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.surface, contentColor = colors.onSurface)) {
                    Icon(LeafIcons.Remove, "−0.1")
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicTextField(value = input, onValueChange = { text ->
                        input = text.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                    }, singleLine = true,
                        textStyle = MaterialTheme.typography.displayMedium.copy(color = if (valid) colors.onTertiaryContainer else colors.error,
                            textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(colors.tertiary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.unit_kg), style = MaterialTheme.typography.titleMedium)
                }
                FilledTonalIconButton(onClick = { shift(.1f) }, modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.surface, contentColor = colors.onSurface)) {
                    Icon(LeafIcons.Add, "+0.1")
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val steps = listOf(-1f, -.5f, .5f, 1f)
            steps.forEachIndexed { index, step ->
                Surface(onClick = { shift(step) }, modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(topStart = if (index == 0) 24.dp else 8.dp, bottomStart = if (index == 0) 24.dp else 8.dp,
                        topEnd = if (index == steps.lastIndex) 24.dp else 8.dp, bottomEnd = if (index == steps.lastIndex) 24.dp else 8.dp),
                    color = colors.surfaceContainerHigh) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(String.format(Locale.getDefault(), "%+.1f", step), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        if (latest != null && number != null && valid) {
            val diff = number - latest
            Text(stringResource(R.string.weight_previous_delta, String.format(Locale.getDefault(), "%+.1f", diff)),
                Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightHistorySheet(history: List<WeightRecord>,
    onUpdate: (WeightRecord, (Boolean) -> Unit) -> Unit,
    onDelete: (WeightRecord, (Boolean) -> Unit) -> Unit, onDismiss: () -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draft by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val editing = history.firstOrNull { it.id == editingId }
    val deleting = history.firstOrNull { it.id == deletingId }
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.85f).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.profile_weight_tracking), style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, enabled = !busy) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (history.isEmpty()) item { Text(stringResource(R.string.weight_empty), style = MaterialTheme.typography.bodyLarge) }
                items(history.sortedWith(compareByDescending<WeightRecord> { it.date }.thenByDescending { it.id }), key = { it.id }) { record ->
                    Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainer) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(DateUtils.formatDate(record.date) + " · " + DateUtils.formatTime(record.date),
                                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(String.format(Locale.getDefault(), "%.1f", record.weightKg) + " " + stringResource(R.string.unit_kg),
                                    style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                                FilledTonalIconButton(enabled = !busy, onClick = { editingId = record.id; draft = record.weightKg.toString(); error = false }) {
                                    Icon(LeafIcons.Edit, stringResource(R.string.common_edit))
                                }
                                IconButton(enabled = !busy, onClick = { deletingId = record.id; error = false }) {
                                    Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete), tint = colors.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (editing != null) LeafEditSheet(
        onDismissRequest = { if (!busy) { editingId = null; error = false } },
        title = { Text(stringResource(R.string.common_edit)) },
        confirmButton = {
            val value = draft.replace(',', '.').toFloatOrNull()
            LeafButton(enabled = !busy && value != null && value.isFinite() && value in 20f..350f,
                onClick = {
                    value?.let { kg -> busy = true; error = false
                        onUpdate(editing.copy(weightKg = kg)) { success ->
                            busy = false; error = !success; if (success) editingId = null
                        }
                    }
                }) { Text(stringResource(R.string.common_save)) }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = { editingId = null; error = false }) { Text(stringResource(R.string.common_cancel)) } },
        content = {
            Text(DateUtils.formatDate(editing.date), style = MaterialTheme.typography.labelLarge)
            AppTextField(draft, { draft = it }, stringResource(R.string.unit_kg), Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal, isError = error,
                supportingText = if (error) stringResource(R.string.record_change_error) else null)
        })
    if (deleting != null) LeafEditSheet(
        onDismissRequest = { if (!busy) { deletingId = null; error = false } },
        title = { Text(stringResource(R.string.weight_delete_question)) },
        content = {
            Text(DateUtils.formatDate(deleting.date) + " · " + String.format(Locale.getDefault(), "%.1f", deleting.weightKg) + " " + stringResource(R.string.unit_kg))
            if (error) Text(stringResource(R.string.record_change_error), color = colors.error)
        },
        confirmButton = { LeafButton(enabled = !busy, onClick = {
            busy = true; error = false
            onDelete(deleting) { success -> busy = false; error = !success; if (success) deletingId = null }
        }, colors = ButtonDefaults.buttonColors(containerColor = colors.error)) { Text(stringResource(R.string.common_delete)) } },
        dismissButton = { TextButton(enabled = !busy, onClick = { deletingId = null; error = false }) { Text(stringResource(R.string.common_cancel)) } })
}
