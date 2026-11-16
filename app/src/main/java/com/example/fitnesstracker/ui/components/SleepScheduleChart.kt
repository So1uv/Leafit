package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle

@Composable
fun SleepScheduleChart(records: List<SleepRecord>) {
    val zone = remember { ZoneId.systemDefault() }
    val today by com.example.fitnesstracker.utils.currentDayFlow().collectAsLeafState(DateUtils.todayStart())
    val last = remember(today) { Instant.ofEpochMilli(today).atZone(zone).toLocalDate() }
    val days = remember(last) { (6L downTo 0L).map { last.minusDays(it) } }
    val grouped = remember(records, last) { records.groupBy { Instant.ofEpochMilli(it.wakeTime).atZone(zone).toLocalDate() } }
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.sleep_schedule_title), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                MetricExplanation(stringResource(R.string.sleep_schedule_title), listOf(stringResource(R.string.sleep_schedule_source)))
            }
            days.forEach { day ->
                val entries = grouped[day].orEmpty()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, appLocale()), Modifier.width(32.dp), style = MaterialTheme.typography.labelMedium)
                    Column(Modifier.weight(1f)) {
                        Canvas(Modifier.fillMaxWidth().height(18.dp)) {
                            val noon = day.minusDays(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
                            val next = day.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
                            fun x(time: Long) = ((time - noon).toDouble() / (next - noon)).toFloat().coerceIn(0f, 1f) * size.width
                            drawLine(colors.surfaceContainerHighest, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 7.dp.toPx(), StrokeCap.Round)
                            entries.forEach { row -> drawLine(colors.tertiary, Offset(x(row.bedTime), size.height / 2),
                                Offset(x(row.wakeTime), size.height / 2), 9.dp.toPx(), StrokeCap.Round) }
                        }
                        Text(if (entries.isEmpty()) "—" else entries.joinToString(" · ") {
                            DateUtils.formatTime(it.bedTime) + "–" + DateUtils.formatTime(it.wakeTime)
                        }, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                    Text(if (entries.isEmpty()) "—" else DateUtils.formatDuration(entries.sumOf { (it.wakeTime - it.bedTime).coerceAtLeast(0) } / 1000),
                        style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(stringResource(R.string.sleep_schedule_axis), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}
