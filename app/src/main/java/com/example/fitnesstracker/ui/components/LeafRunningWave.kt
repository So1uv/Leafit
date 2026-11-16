package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LeafRunningWave(running: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val cycle = remember { Animatable(0f) }
    val allowed = running && LocalLeafMotionActive.current
    LaunchedEffect(allowed) {
        if (allowed) cycle.animateTo(cycle.value + 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)))
    }
    val amplitude = animateFloatAsState(if (running) .28f else .045f, spring(1f, 480f), label = "runningAmplitude")
    val line = remember { Path() }
    Canvas(modifier) {
        if (size.width <= 0f) return@Canvas
        line.reset()
        val samples = (size.width / 3.dp.toPx()).toInt().coerceIn(24, 120)
        for (index in 0..samples) {
            val fraction = index.toFloat() / samples
            val y = size.height * (.5f + amplitude.value * sin((fraction * 6f + cycle.value) * 2f * PI).toFloat())
            if (index == 0) line.moveTo(0f, y) else line.lineTo(size.width * fraction, y)
        }
        drawPath(line, color.copy(alpha = if (running) .75f else .35f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}
