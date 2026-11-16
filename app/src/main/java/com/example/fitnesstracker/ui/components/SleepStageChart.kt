package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.SleepCycleModel

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun SleepStageChart(durationMinutes: Int, startLabel: String, endLabel: String,
    modifier: Modifier = Modifier) {
    val minutes = durationMinutes.coerceIn(0, 1440)
    val model = remember(minutes) { SleepCycleModel.build(minutes) }
    val colors = MaterialTheme.colorScheme
    val tones = listOf(colors.tertiary, colors.secondary, colors.primary, colors.outline)
    val labels = listOf(stringResource(R.string.b6_deep), stringResource(R.string.b6_light),
        stringResource(R.string.b6_rem), stringResource(R.string.sleep_awake_short))
    val hint = stringResource(R.string.b6_cycles_hint)
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall
    val labelWidth = with(LocalDensity.current) {
        labels.maxOf { textMeasurer.measure(it, labelStyle).size.width }.toDp() + 4.dp
    }
    val rowHeight = 40.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    val totals = remember(model) { (0..3).map { level ->
        model.segments.filter { it.stage.level == level }.sumOf { it.endMin - it.startMin }
    } }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(hint, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerHigh) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.width(labelWidth)) {
                        (3 downTo 0).forEach { level ->
                            Box(Modifier.height(rowHeight).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                Text(labels[level], style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant)
                            }
                        }
                    }
                    Canvas(Modifier.weight(1f).height(rowHeight * 4).semantics { contentDescription = hint }) {
                        val inset = 5.dp.toPx()
                        val width = (size.width - 2 * inset).coerceAtLeast(0f)
                        val lane = size.height / 4
                        fun x(minute: Int) = inset + width * minute / minutes.coerceAtLeast(1)
                        fun y(level: Int) = lane * (3 - level + .5f)
                        val stroke = 8.dp.toPx()
                        repeat(4) { level ->
                            drawLine(colors.outlineVariant.copy(alpha = .45f),
                                Offset(inset, y(level)), Offset(size.width - inset, y(level)), 1.dp.toPx())
                        }
                        model.segments.zipWithNext().forEach { (previous, next) ->
                            if (previous.stage != next.stage) {
                                val boundary = x(next.startMin)
                                val bend = minOf(5.dp.toPx(),
                                    (x(previous.endMin) - x(previous.startMin)) / 3,
                                    (x(next.endMin) - x(next.startMin)) / 3)
                                val path = Path().apply {
                                    moveTo(boundary - bend, y(previous.stage.level))
                                    cubicTo(boundary + bend, y(previous.stage.level),
                                        boundary - bend, y(next.stage.level), boundary + bend, y(next.stage.level))
                                }
                                drawPath(path, colors.tertiary.copy(alpha = .35f),
                                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                        model.segments.forEach { segment ->
                            val start = x(segment.startMin)
                            val end = x(segment.endMin)
                            val thickness = minOf(stroke, (end - start).coerceAtLeast(0f))
                            if (thickness > 0f) drawLine(tones[segment.stage.level],
                                Offset(start + thickness / 2, y(segment.stage.level)),
                                Offset(end - thickness / 2, y(segment.stage.level)), thickness, StrokeCap.Round)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(startLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Text(endLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(listOf(0, 1), listOf(2, 3)).forEachIndexed { rowIndex, pair ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pair.forEachIndexed { colIndex, level ->
                        val big = 20.dp; val small = 6.dp
                        val shape = RoundedCornerShape(
                            topStart = if (rowIndex == 0 && colIndex == 0) big else small,
                            topEnd = if (rowIndex == 0 && colIndex == 1) big else small,
                            bottomStart = if (rowIndex == 1 && colIndex == 0) big else small,
                            bottomEnd = if (rowIndex == 1 && colIndex == 1) big else small)
                        LeafSurface(Modifier.weight(1f).fillMaxHeight(), shape = shape, color = colors.surfaceContainerHigh) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Canvas(Modifier.size(10.dp)) { drawCircle(tones[level]) }
                                    Text(labels[level], style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurfaceVariant, maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Text(stringResource(R.string.sleep_duration_compact, totals[level] / 60, totals[level] % 60),
                                    style = MaterialTheme.typography.titleMedium, color = colors.onSurface,
                                    fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}
