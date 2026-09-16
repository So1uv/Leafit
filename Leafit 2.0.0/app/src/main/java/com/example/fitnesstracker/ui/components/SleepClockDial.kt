package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.ui.theme.LeafTones
import kotlin.math.*

@Composable
fun SleepClockDial(bedMinutes: Int, wakeMinutes: Int, onBedChange: (Int) -> Unit,
    onWakeChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val bedtime = colors.tertiary
    val sunrise = LeafTones.dawn
    val bed = rememberUpdatedState(bedMinutes)
    val wake = rememberUpdatedState(wakeMinutes)
    val changeBed = rememberUpdatedState(onBedChange)
    val changeWake = rememberUpdatedState(onWakeChange)
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val faceShape = remember { LeafRosette(6) }
    val wakeShape = remember { LeafRosette(8) }
    var activeHandle by remember { mutableIntStateOf(0) }
    var editHandle by remember { mutableStateOf<Int?>(null) }
    var draggingHandle by remember { mutableStateOf<Int?>(null) }
    val duration = com.example.fitnesstracker.utils.SleepTimeMath.durationMinutes(bedMinutes, wakeMinutes)
    val locale = appLocale()
    fun time(minutes: Int) = String.format(locale, "%02d:%02d", minutes / 60, minutes % 60)
    fun angle(minutes: Int) = minutes / 1440f * 360f - 90f

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val diameter = maxWidth.coerceAtMost(320.dp)
            val half = with(density) { diameter.toPx() / 2 }
            val radius = half - with(density) { 28.dp.toPx() }
            fun handle(minutes: Int): Offset {
                val radians = angle(minutes) * PI / 180
                return Offset(half + radius * cos(radians).toFloat(), half + radius * sin(radians).toFloat())
            }
            Box(Modifier.size(diameter).pointerInput(diameter) {
                fun minuteAt(point: Offset): Int {
                    val degrees = Math.toDegrees(atan2((point.y - half).toDouble(), (point.x - half).toDouble())) + 90
                    val normalized = (degrees + 360) % 360
                    return ((normalized / 360 * 1440 / 5).roundToInt() * 5) % 1440
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val bedDistance = (down.position - handle(bed.value)).getDistance()
                    val wakeDistance = (down.position - handle(wake.value)).getDistance()
                    if (minOf(bedDistance, wakeDistance) > 24.dp.toPx()) return@awaitEachGesture
                    if (abs(bedDistance - wakeDistance) > 4.dp.toPx()) {
                        activeHandle = if (bedDistance < wakeDistance) 0 else 1
                    }
                    var lastMinute = if (activeHandle == 0) bed.value else wake.value
                    val start = awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                        ?: return@awaitEachGesture
                    fun updateTime(position: Offset) {
                        val minutes = minuteAt(position)
                        if (minutes != lastMinute) {
                            lastMinute = minutes
                            if (activeHandle == 0) changeBed.value(minutes) else changeWake.value(minutes)
                            if (minutes % 30 == 0) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    draggingHandle = activeHandle
                    try {
                        updateTime(start.position)
                        drag(start.id) { change -> updateTime(change.position); change.consume() }
                    } finally {
                        draggingHandle = null
                    }
                }
            }) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 22.dp.toPx()
                    drawCircle(colors.surfaceContainerHighest, radius, style = Stroke(10.dp.toPx()))
                    for (tick in 0 until 48) {
                        val radians = (angle(bedMinutes) + tick * 7.5f) * PI / 180
                        val start = radius - stroke / 2 - 8.dp.toPx()
                        val length = when {
                            tick % 12 == 0 -> 8.dp.toPx()
                            tick % 2 == 0 -> 5.dp.toPx()
                            else -> 2.dp.toPx()
                        }
                        drawLine(if (tick * 30 <= duration) bedtime else colors.onSurfaceVariant.copy(alpha = .3f),
                            center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * start,
                            center + Offset(cos(radians).toFloat(), sin(radians).toFloat()) * (start - length),
                            1.5.dp.toPx(), StrokeCap.Round)
                    }
                    val sweep = duration / 1440f * 360f
                    if (sweep > 0) {
                        drawArc(bedtime, angle(bedMinutes), sweep, false,
                            Offset(center.x - radius, center.y - radius), Size(radius * 2, radius * 2),
                            style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    listOf(bedMinutes, wakeMinutes).forEach { minutes ->
                        drawCircle(colors.surface, 27.dp.toPx(), handle(minutes))
                    }
                }
                Box(Modifier.align(Alignment.Center).size(diameter * .48f).clip(faceShape)
                    .background(lerp(colors.surface, colors.tertiaryContainer, .62f)))
                listOf(0, 6, 12, 18).forEach { hour ->
                    val radians = (angle(bedMinutes) + hour * 15) * PI / 180
                    val textRadius = radius - with(density) { 40.dp.toPx() }
                    Box(Modifier.absoluteOffset {
                        IntOffset((half + textRadius * cos(radians) - 14.dp.toPx()).roundToInt(),
                            (half + textRadius * sin(radians) - 12.dp.toPx()).roundToInt())
                    }.size(28.dp, 24.dp).clip(CircleShape)
                        .background(if (hour * 60 <= duration) colors.tertiaryContainer else colors.surface), Alignment.Center) {
                        Text("$hour", style = MaterialTheme.typography.labelMedium,
                            color = if (hour * 60 <= duration) colors.onTertiaryContainer else colors.onSurfaceVariant)
                    }
                }
                Column(Modifier.align(Alignment.Center).width(diameter * .42f),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(String.format(locale, "%d:%02d", duration / 60, duration % 60),
                        style = (if (density.fontScale > 1.3f) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.displaySmall).copy(fontFeatureSettings = "tnum"),
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.onTertiaryContainer)
                    Text(stringResource(R.string.sleep_dial_duration), style = MaterialTheme.typography.labelMedium,
                        color = colors.onTertiaryContainer, textAlign = TextAlign.Center, maxLines = 2)
                }
                listOf(bedMinutes, wakeMinutes).forEachIndexed { index, minutes ->
                    val emphasis = animateFloatAsState(if (draggingHandle == index || editHandle == index) 1.08f else 1f,
                        spring(.8f, 500f), label = "sleepHandle$index")
                    LeafSurface(onClick = { activeHandle = index; editHandle = if (editHandle == index) null else index },
                        shape = if (index == 0) CircleShape else wakeShape,
                        color = if (index == 0) bedtime else sunrise,
                        contentColor = if (index == 0) colors.onTertiary else LeafTones.dawnContainer,
                        modifier = Modifier.absoluteOffset {
                            val point = handle(minutes)
                            IntOffset((point.x - 24.dp.toPx()).roundToInt(), (point.y - 24.dp.toPx()).roundToInt())
                        }.size(48.dp).zIndex(if (activeHandle == index) 1f else 0f).graphicsLayer {
                            scaleX = emphasis.value
                            scaleY = emphasis.value
                        }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (index == 0) LeafIcons.Bedtime else LeafIcons.WbSunny,
                                stringResource(if (index == 0) R.string.sleep_bedtime else R.string.sleep_wake), Modifier.size(23.dp))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(bedMinutes, wakeMinutes).forEachIndexed { index, minutes ->
                LeafSurface(onClick = { activeHandle = index; editHandle = if (editHandle == index) null else index },
                    shape = RoundedCornerShape(22.dp), modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = if (index == 0) colors.tertiaryContainer else LeafTones.dawnContainer,
                    contentColor = if (index == 0) colors.onTertiaryContainer else LeafTones.onDawn) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(if (index == 0) LeafIcons.Bedtime else LeafIcons.WbSunny, null, Modifier.size(18.dp))
                            Text(stringResource(if (index == 0) R.string.sleep_bedtime else R.string.sleep_wake), Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium)
                            Icon(LeafIcons.Edit, null, Modifier.size(16.dp))
                        }
                        Text(time(minutes), style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
                    }
                }
            }
        }
        editHandle?.let { which ->
            androidx.compose.runtime.key(which) {
                ExpressiveTimeInput(stringResource(if (which == 0) R.string.sleep_bedtime else R.string.sleep_wake),
                    if (which == 0) LeafIcons.Bedtime else LeafIcons.WbSunny,
                    if (which == 0) bedMinutes else wakeMinutes,
                    onChange = { if (which == 0) onBedChange(it) else onWakeChange(it) })
            }
        }
        Text(stringResource(R.string.sleep_dial_hint), style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant)
    }
}
