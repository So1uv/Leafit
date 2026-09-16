package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafButton
import androidx.compose.foundation.Canvas
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
    onDelete: (WeightRecord, (Boolean) -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    val points = remember(history) { history.filter { it.weightKg.isFinite() }.sortedBy { it.date }.takeLast(12) }
    val latest = points.lastOrNull()
    val delta = if (points.size > 1) points.last().weightKg - points[points.lastIndex - 1].weightKg else null
    val colors = MaterialTheme.colorScheme
    val lineColor = colors.tertiary
    if (showAdd) AlertDialog(
        onDismissRequest = { showAdd = false },
        title = { Text(stringResource(R.string.profile_weight_hint)) },
        text = {
            AppTextField(input, { input = it }, stringResource(R.string.unit_kg),
                modifier = Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
        },
        confirmButton = {
            val number = input.replace(',', '.').toFloatOrNull()
            LeafButton(enabled = number != null && number.isFinite() && number in 20f..350f,
                onClick = { number?.let(onAdd); input = ""; showAdd = false }) {
                Text(stringResource(R.string.note_save_action))
            }
        },
        dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.common_cancel)) } }
    )
    if (showHistory) WeightHistorySheet(history, onUpdate, onDelete) { showHistory = false }
    Card(onClick = { showHistory = true }, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(LeafIcons.MonitorWeight, null, tint = lineColor)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.profile_weight_tracking), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = { showAdd = true }) {
                    Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, stringResource(R.string.profile_weight_hint))
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricText(latest?.let { String.format(Locale.getDefault(), "%.1f", it.weightKg) } ?: "—",
                    style = MaterialTheme.typography.headlineLarge, weight = FontWeight.Medium)
                Text(stringResource(R.string.unit_kg), style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
            }
            if (delta != null) {
                Surface(shape = RoundedCornerShape(16.dp), color = colors.tertiaryContainer) {
                    Text(stringResource(R.string.weight_previous_delta, String.format(Locale.getDefault(), "%+.1f", delta)),
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge, color = colors.onTertiaryContainer)
                }
            }
            Text(stringResource(R.string.weight_manage_hint), style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant)
            if (points.size >= 2) {
                val description = stringResource(R.string.weight_chart_description,
                    String.format(Locale.getDefault(), "%.1f", points.first().weightKg),
                    String.format(Locale.getDefault(), "%.1f", points.last().weightKg))
                Canvas(Modifier.fillMaxWidth().height(84.dp).semantics { contentDescription = description }) {
                    val inset = 5.dp.toPx()
                    val chartW = (size.width - inset * 2).coerceAtLeast(1f)
                    val chartH = size.height - inset * 2
                    val min = points.minOf { it.weightKg } - 0.25f
                    val range = (points.maxOf { it.weightKg } - min + 0.25f).coerceAtLeast(0.5f)
                    val timeSpan = (points.last().date - points.first().date).coerceAtLeast(1L)
                    val offsets = points.map { record -> Offset(
                        inset + ((record.date - points.first().date).toDouble() / timeSpan * chartW).toFloat(),
                        inset + chartH * (1f - (record.weightKg - min) / range)) }
                    val path = Path().apply { offsets.forEachIndexed { i, p ->
                        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    } }
                    drawPath(path, lineColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    offsets.forEach { drawCircle(lineColor, 3.dp.toPx(), it) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(DateUtils.formatDate(points.first().date), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(DateUtils.formatDate(points.last().date), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
            } else {
                Text(stringResource(if (points.isEmpty()) R.string.weight_empty else R.string.weight_one_record),
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
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
