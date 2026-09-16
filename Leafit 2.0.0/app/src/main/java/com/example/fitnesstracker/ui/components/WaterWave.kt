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
    val phase by if (LocalLeafPageActive.current && progress > .01f) {
        rememberInfiniteTransition(label = "waterWave").animateFloat(0f, 2f * PI.toFloat(),
            infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "phase")
    } else rememberUpdatedState(0f)
    val level by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        tween(900, easing = EaseOutCubic),
        label = "level"
    )

    Canvas(modifier) {
        if (level <= 0.01f) return@Canvas

        val surfaceY = size.height * (1f - level * 0.9f)

        fun drawWave(p: Float, amplitude: Float, alpha: Float) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, surfaceY)
                var x = 0f
                while (x <= size.width + 4f) {
                    lineTo(x, surfaceY + amplitude * sin(2f * PI.toFloat() * (x / size.width) * 1.6f + p))
                    x += 4f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color.copy(alpha = alpha))
        }
        drawWave(phase, 5.dp.toPx(), 0.10f)
        drawWave(phase + 2.1f, 7.dp.toPx(), 0.07f)
    }
}
