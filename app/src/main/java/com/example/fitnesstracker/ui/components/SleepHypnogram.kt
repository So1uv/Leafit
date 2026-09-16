package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

const val CYCLE_MINUTES = 90
const val TIME_TO_FALL_ASLEEP = 15

enum class SleepStage(val level: Int) {
    AWAKE(0), REM(1), LIGHT(2), DEEP(3)
}

data class HypnoSegment(val startMin: Float, val endMin: Float, val stage: SleepStage)

fun buildHypnogram(totalMinutes: Int): List<HypnoSegment> {
    val segments = mutableListOf<HypnoSegment>()
    if (totalMinutes <= 0) return segments
    if (totalMinutes <= TIME_TO_FALL_ASLEEP) return listOf(HypnoSegment(0f, totalMinutes.toFloat(), SleepStage.AWAKE))

    segments.add(HypnoSegment(0f, TIME_TO_FALL_ASLEEP.toFloat(), SleepStage.AWAKE))

    var t = TIME_TO_FALL_ASLEEP.toFloat()
    val end = totalMinutes.toFloat()
    var cycleIndex = 0

    while (t < end) {
        val cycleLen = minOf(CYCLE_MINUTES.toFloat(), end - t)
        if (cycleLen < 8f) {
            segments.add(HypnoSegment(t, end, SleepStage.LIGHT))
            break
        }

        // Deep-sleep fraction: high early, decays each cycle
        val deepFrac = (0.55f - cycleIndex * 0.14f).coerceIn(0.0f, 0.6f)
        // REM fraction: low early, grows each cycle
        val remFrac = (0.08f + cycleIndex * 0.10f).coerceIn(0.05f, 0.35f)
        // Light sleep fills the rest, minus a short onset
        val onsetLight = 0.12f
        val lightFrac = (1f - deepFrac - remFrac - onsetLight).coerceAtLeast(0.1f)

        var c = t
        val onsetLen = cycleLen * onsetLight
        segments.add(HypnoSegment(c, c + onsetLen, SleepStage.LIGHT)); c += onsetLen
        // deep N3 — may be zero in late cycles
        if (deepFrac > 0.01f) {
            val deepLen = cycleLen * deepFrac
            segments.add(HypnoSegment(c, c + deepLen, SleepStage.DEEP)); c += deepLen
        }
        val lightLen = cycleLen * lightFrac
        segments.add(HypnoSegment(c, c + lightLen, SleepStage.LIGHT)); c += lightLen
        val remLen = (t + cycleLen) - c
        if (remLen > 1f) segments.add(HypnoSegment(c, t + cycleLen, SleepStage.REM))

        t += cycleLen
        cycleIndex++
    }
    return segments
}

fun cycleCount(totalMinutes: Int): Int =
    ((totalMinutes - TIME_TO_FALL_ASLEEP).coerceAtLeast(0) / CYCLE_MINUTES)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SleepHypnogram(durationMinutes: Int, modifier: Modifier = Modifier, startMinutes: Int? = null) {
    val colors = MaterialTheme.colorScheme
    val awake = com.example.fitnesstracker.ui.theme.LeafTones.onWarm
    val rem = colors.tertiary
    val light = androidx.compose.ui.graphics.lerp(colors.tertiary, colors.surfaceContainer, .42f)
    val deep = colors.primary
    val segments = androidx.compose.runtime.remember(durationMinutes) { buildHypnogram(durationMinutes.coerceIn(0, 1440)) }
    fun color(stage: SleepStage) = when (stage) {
        SleepStage.AWAKE -> awake
        SleepStage.REM -> rem
        SleepStage.LIGHT -> light
        SleepStage.DEEP -> deep
    }
    val locale = appLocale()
    fun time(minutes: Int): String {
        val value = if (startMinutes == null) minutes else (startMinutes + minutes) % 1440
        return String.format(locale, "%d:%02d", value / 60, value % 60)
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.fillMaxWidth().height(116.dp)) {
            val margin = 6.dp.toPx()
            val width = size.width - 2 * margin
            val rowHeight = size.height / 4
            val thickness = 10.dp.toPx()
            fun y(stage: SleepStage) = rowHeight * (stage.level + .5f)
            fun x(minute: Float) = margin + minute / durationMinutes.coerceAtLeast(1) * width
            for (row in 0..3) {
                drawRoundRect(colors.surfaceContainerHighest.copy(alpha = .32f),
                    Offset(0f, rowHeight * row + 3.dp.toPx()),
                    androidx.compose.ui.geometry.Size(size.width, rowHeight - 6.dp.toPx()),
                    androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()))
            }
            segments.zipWithNext().forEach { (previous, next) ->
                if (previous.stage != next.stage) {
                    val joinX = x(next.startMin)
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(joinX - 3.dp.toPx(), y(previous.stage))
                        cubicTo(joinX + 5.dp.toPx(), y(previous.stage), joinX - 5.dp.toPx(), y(next.stage),
                            joinX + 3.dp.toPx(), y(next.stage))
                    }
                    drawPath(path, color(next.stage).copy(alpha = .32f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            segments.forEach { segment ->
                val x1 = x(segment.startMin)
                val x2 = x(segment.endMin)
                drawRoundRect(color(segment.stage), Offset(x1, y(segment.stage) - thickness / 2),
                    androidx.compose.ui.geometry.Size((x2 - x1).coerceAtLeast(1f), thickness),
                    androidx.compose.ui.geometry.CornerRadius(thickness / 2))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(time(0), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(time(durationMinutes.coerceAtLeast(0)), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HypnoLegend(deep, androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.sleep_phase_deep))
            HypnoLegend(light, androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.sleep_phase_light))
            HypnoLegend(rem, androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.sleep_phase_rem))
            HypnoLegend(awake, androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.sleep_phase_awake))
        }
    }
}

@Composable
private fun HypnoLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Canvas(Modifier.size(9.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
