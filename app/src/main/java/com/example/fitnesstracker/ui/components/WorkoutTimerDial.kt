package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorkoutTimerDial(elapsedSeconds: Long, running: Boolean, paused: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val fontScale = LocalDensity.current.fontScale
    val faceShape = remember { LeafRosette(8) }
    val accent = animateColorAsState(if (paused) colors.tertiary else colors.primary,
        tween(220), label = "timerAccent")
    // Animate elapsed time before wrapping, so 59 → 60 moves forward through the top.
    val seconds = animateFloatAsState(elapsedSeconds.coerceAtLeast(0).toFloat(),
        tween(if (paused || !running) 0 else 420, easing = LinearEasing), label = "timerSeconds")
    val status = stringResource(when {
        paused -> R.string.workout_status_paused
        running -> R.string.workout_status_active
        else -> R.string.workout_status_ready
    })
    val faceColor = if (paused) colors.tertiaryContainer else lerp(colors.surfaceContainer, colors.primaryContainer, .7f)
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val diameter = maxWidth.coerceAtMost(240.dp * fontScale.coerceIn(1f, 1.4f))
        Box(Modifier.size(diameter).semantics(mergeDescendants = true) {}, Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2 - 10.dp.toPx()
                val stroke = 7.dp.toPx()
                val ringSize = Size(radius * 2, radius * 2)
                val origin = center - Offset(radius, radius)
                drawCircle(colors.surfaceContainerHighest, radius, style = Stroke(stroke))
                val fraction = if (running) (seconds.value % 60f) / 60f else 0f
                for (tick in 0 until 60) {
                    val a = tick * PI / 30 - PI / 2
                    val direction = Offset(cos(a).toFloat(), sin(a).toFloat())
                    val inner = radius - 12.dp.toPx()
                    val length = if (tick % 5 == 0) 6.dp.toPx() else 2.dp.toPx()
                    drawLine(if (running && tick / 60f < fraction) accent.value else colors.onSurfaceVariant.copy(alpha = .25f),
                        center + direction * inner, center + direction * (inner - length),
                        if (tick % 5 == 0) 2.dp.toPx() else 1.5.dp.toPx(), StrokeCap.Round)
                }
                if (running && fraction > 0f) {
                    drawArc(accent.value, -90f, fraction * 360f, false, origin, ringSize,
                        style = Stroke(stroke, cap = StrokeCap.Round))
                }
                val angle = fraction * 2 * PI - PI / 2
                val marker = center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * radius
                drawCircle(colors.surfaceContainer, 10.dp.toPx(), marker)
                drawCircle(accent.value, 6.dp.toPx(), marker)
            }
            LeafSurface(shape = faceShape, color = faceColor, modifier = Modifier.size(diameter * .72f)) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (paused) LeafIcons.Pause else LeafIcons.Timer, null, Modifier.size(22.dp),
                        tint = if (paused) colors.onTertiaryContainer else colors.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(DateUtils.formatDuration(elapsedSeconds), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = (if (elapsedSeconds >= 3600) MaterialTheme.typography.headlineSmall
                            else MaterialTheme.typography.displaySmall).copy(fontFeatureSettings = "tnum"),
                        color = colors.onSurface)
                    Spacer(Modifier.height(8.dp))
                    LeafSurface(shape = CircleShape, color = colors.surfaceContainerLow,
                        contentColor = colors.onSurfaceVariant) {
                        Text(status, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    }
                }
            }
        }
    }
}
