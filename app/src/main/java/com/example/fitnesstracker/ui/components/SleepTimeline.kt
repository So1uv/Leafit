package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.GoalPreferences
import java.time.Instant
import java.time.ZoneId

@Composable
private fun SleepDurationTimeline(bedTime: Long, wakeTime: Long, quality: Int? = null, modifier: Modifier = Modifier, showDuration: Boolean = true) {
    val context = LocalContext.current
    val targetMinutes = GoalPreferences.getSleepGoalMinutes(context).coerceAtLeast(1)
    val minutes = ((wakeTime - bedTime).coerceAtLeast(0L) / 60000L).toInt()
    val zone = remember { ZoneId.systemDefault() }
    val start = remember(bedTime) {
        val bed = Instant.ofEpochMilli(bedTime).atZone(zone)
        bed.toLocalDate().minusDays(if (bed.hour < 12) 1 else 0).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    }
    val goalEnd = bedTime + targetMinutes * 60000L
    val end = maxOf(start + 24L * 3600000L, wakeTime, goalEnd)
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (showDuration) Text(stringResource(R.string.sleep_timeline_duration, minutes / 60, minutes % 60),
                    style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Text(stringResource(R.string.sleep_timeline_goal, targetMinutes / 60, targetMinutes % 60),
                    style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            MetricExplanation(stringResource(R.string.sleep_timeline_title), listOf(stringResource(R.string.metric_sleep_source)),
                if (quality == null) stringResource(R.string.metric_sleep_missing) else null)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.sleep_timeline_bed, DateUtils.formatTime(bedTime)), style = MaterialTheme.typography.labelLarge)
                Text(DateUtils.formatDate(bedTime), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.sleep_timeline_wake, DateUtils.formatTime(wakeTime)), style = MaterialTheme.typography.labelLarge)
                Text(DateUtils.formatDate(wakeTime), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }

        }
        Canvas(Modifier.fillMaxWidth().height(40.dp).padding(horizontal = 6.dp)) {
            fun x(time: Long) = ((time - start).toDouble() / (end - start).coerceAtLeast(1)).toFloat().coerceIn(0f, 1f) * size.width
            val y = size.height / 2
            drawLine(colors.surfaceContainerHighest, Offset(0f, y), Offset(size.width, y), 12.dp.toPx(), StrokeCap.Round)
            drawLine(colors.tertiaryContainer, Offset(x(bedTime), y), Offset(x(goalEnd), y), 22.dp.toPx(), StrokeCap.Round)
            drawLine(colors.tertiary, Offset(x(bedTime), y), Offset(x(wakeTime), y), 12.dp.toPx(), StrokeCap.Round)
            drawLine(colors.primary, Offset(x(goalEnd), 0f), Offset(x(goalEnd), size.height), 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())))
            drawCircle(colors.onTertiary, 3.dp.toPx(), Offset(x(bedTime), y))
            drawCircle(colors.onTertiary, 3.dp.toPx(), Offset(x(wakeTime), y))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(DateUtils.formatTime(start), style = MaterialTheme.typography.labelSmall)
            Text(DateUtils.formatTime(start + (end - start) / 2), style = MaterialTheme.typography.labelSmall)
            Text(DateUtils.formatTime(end), style = MaterialTheme.typography.labelSmall)
        }
        if (quality != null) Text(stringResource(R.string.sleep_timeline_quality, quality.coerceIn(1, 5)),
            style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
    }
}

@Composable
fun SleepTimeline(bedTime: Long, wakeTime: Long, quality: Int? = null, modifier: Modifier = Modifier, showDuration: Boolean = true) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("sleep_display", android.content.Context.MODE_PRIVATE) }
    val cycles = remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("cycles", false)) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LeafViewSelector(cycles.value, stringResource(R.string.b6_duration), stringResource(R.string.b6_cycles)) {
            cycles.value = it
            prefs.edit().putBoolean("cycles", it).apply()
        }
        androidx.compose.animation.Crossfade(targetState = cycles.value,
            animationSpec = androidx.compose.animation.core.tween(160), label = "sleepView") { showCycles ->
            if (showCycles) SleepCycleGraph(bedTime, wakeTime)
            else SleepDurationTimeline(bedTime, wakeTime, quality, showDuration = showDuration)
        }
    }
}

@Composable
private fun SleepCycleGraph(bedTime: Long, wakeTime: Long) {
    val minutes = ((wakeTime - bedTime) / 60000L).coerceIn(0L, 1440L).toInt()
    SleepStageChart(minutes, DateUtils.formatTime(bedTime), DateUtils.formatTime(wakeTime))
}
