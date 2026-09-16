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

/** A decorative running state. Pausing cancels the animation at its current phase. */
@Composable
fun LeafRunningWave(running: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val phase = remember { Animatable(0f) }
    val active = running && LocalLeafPageActive.current
    LaunchedEffect(active) {
        if (active) phase.animateTo(phase.value + 1f,
            infiniteRepeatable(tween(1600, easing = LinearEasing)))
    }
    val amplitude by animateFloatAsState(if (running) 1f else .15f, spring(1f, 500f), label = "waveRest")
    Canvas(modifier) {
        val path = Path()
        repeat(121) { sample ->
            val x = size.width * sample / 120f
            val y = size.height / 2f + sin((sample / 120f * 6f + phase.value) * 2f * PI).toFloat() * size.height * .28f * amplitude
            if (sample == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color.copy(alpha = if (running) .75f else .35f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }
}
