package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterWave(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val target = progress.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    val phase = remember { Animatable(0f) }
    val animated = LocalLeafMotionActive.current && target > .01f
    LaunchedEffect(animated) {
        if (animated) phase.animateTo(phase.value + 2f * PI.toFloat(),
            infiniteRepeatable(tween(3400, easing = LinearEasing)))
    }
    val fill = animateFloatAsState(target, tween(750, easing = EaseOutCubic), label = "waterFill")
    val path = remember { Path() }
    Canvas(modifier) {
        if (size.width <= 0f || fill.value <= .01f) return@Canvas
        val baseline = size.height * (1f - .9f * fill.value)
        val samples = (size.width / 4.dp.toPx()).toInt().coerceIn(20, 100)
        repeat(2) { layer ->
            path.reset()
            path.moveTo(0f, size.height)
            for (i in 0..samples) {
                val fraction = i.toFloat() / samples
                val displacement = sin(fraction * 3.2f * PI.toFloat() + phase.value + layer * 2.1f)
                path.lineTo(size.width * fraction, baseline + displacement * (5f + 2f * layer).dp.toPx())
            }
            path.lineTo(size.width, size.height)
            path.close()
            drawPath(path, color.copy(alpha = if (layer == 0) .10f else .07f))
        }
    }
}
