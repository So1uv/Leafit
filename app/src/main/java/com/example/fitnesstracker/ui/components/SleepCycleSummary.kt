package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.GoalPreferences

@Composable
fun SleepCycleSummary(durationMinutes: Int) {
    val context = LocalContext.current
    val active = LocalLeafPageActive.current
    val target = remember(active) { GoalPreferences.getSleepGoalMinutes(context).coerceAtLeast(1) }
    var info by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val reached = durationMinutes >= target
    val cycles = ((durationMinutes - TIME_TO_FALL_ASLEEP).coerceAtLeast(0) / CYCLE_MINUTES.toFloat())
    val missing = (target - durationMinutes).coerceAtLeast(0)
    LeafSurface(shape = RoundedCornerShape(20.dp), color = if (reached) colors.primaryContainer else colors.tertiaryContainer) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (reached) LeafIcons.CheckCircle else LeafIcons.Bedtime, null,
                    tint = if (reached) colors.onPrimaryContainer else colors.onTertiaryContainer)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (reached) stringResource(R.string.sleep_goal_met) else stringResource(R.string.sleep_goal_remaining,
                        missing / 60, missing % 60), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.sleep_cycle_estimate, cycles),
                        style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                IconButton(onClick = { info = true }) { Icon(LeafIcons.Info, stringResource(R.string.sleep_cycle_info_title)) }
            }
            Canvas(Modifier.fillMaxWidth().height(8.dp)) {
                val count = kotlin.math.ceil(target / CYCLE_MINUTES.toFloat()).toInt().coerceIn(1, 16)
                val gap = 4.dp.toPx()
                val width = (size.width - gap * (count - 1)) / count
                repeat(count) { i ->
                    val x = i * (width + gap)
                    val corner = CornerRadius(4.dp.toPx())
                    drawRoundRect(colors.onSurface.copy(alpha = .09f), Offset(x, 0f), Size(width, size.height), corner)
                    val fill = (durationMinutes.toFloat() / target * count - i).coerceIn(0f, 1f)
                    if (fill > 0) drawRoundRect(if (reached) colors.primary else colors.tertiary,
                        Offset(x, 0f), Size(width * fill, size.height), corner)
                }
            }
        }
    }
    if (info) LeafEditSheet(onDismissRequest = { info = false },
        title = { Text(stringResource(R.string.sleep_cycle_info_title)) },
        content = { Text(stringResource(R.string.sleep_cycle_info), style = MaterialTheme.typography.bodyLarge) },
        confirmButton = { LeafButton(onClick = { info = false }) { Text(stringResource(R.string.common_close)) } })
}
