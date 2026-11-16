package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LeafWavyProgress(progress: Float, animate: Boolean = true, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary, trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest) {
    val target = progress.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    val shown by animateFloatAsState(target, spring(dampingRatio = .9f, stiffness = 180f), label = "wavyProgress")
    val wave by animateFloatAsState(if (animate && target > 0f && target < 1f) 1f else 0f, tween(400), label = "wavyAmplitude")
    val phase = remember { Animatable(0f) }
    LaunchedEffect(animate) {
        if (animate) phase.animateTo(phase.value + 2f * PI.toFloat(), infiniteRepeatable(tween(1400, easing = LinearEasing)))
    }
    Canvas(modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) }) {
        val stroke = 4.dp.toPx()
        val middle = size.height / 2f
        val amplitude = ((size.height - stroke) / 2f).coerceAtMost(4.dp.toPx()) * wave
        val length = 24.dp.toPx()
        val end = (size.width - stroke) * shown + stroke / 2f
        val gap = 6.dp.toPx()
        if (end + gap < size.width - stroke / 2f) drawLine(trackColor, Offset(end + gap, middle), Offset(size.width - stroke / 2f, middle),
            stroke, StrokeCap.Round)
        if (shown > .001f) {
            val path = Path()
            var x = stroke / 2f
            path.moveTo(x, middle + amplitude * sin(x / length * 2f * PI.toFloat() + phase.value))
            while (x < end) {
                x = (x + 2.dp.toPx()).coerceAtMost(end)
                path.lineTo(x, middle + amplitude * sin(x / length * 2f * PI.toFloat() + phase.value))
            }
            drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        drawCircle(color, stroke / 2f, Offset(size.width - stroke / 2f, middle))
    }
}
