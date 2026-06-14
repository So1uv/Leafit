package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Хвилястий слайдер у стилі Material 3 Expressive:
 * активна частина — анімована синусоїда, неактивна — пряма лінія,
 * повзунок — вертикальна «пігулка».
 */
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val range = valueRange.endInclusive - valueRange.start
    val fraction = ((value - valueRange.start) / range).coerceIn(0f, 1f)

    // Форма хвилі залишається однаковою, змінюється тільки швидкість анімації.
    // Чим вище якість/значення, тим швидше «дихає» синусоїда.
    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(fraction) {
        var lastFrame = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastFrame != 0L) {
                    val dt = (now - lastFrame) / 1_000_000_000f
                    val cyclesPerSecond = 0.28f + fraction * 1.45f
                    phase = (phase + 2f * PI.toFloat() * cyclesPerSecond * dt) % (2f * PI.toFloat())
                }
                lastFrame = now
            }
        }
    }

    fun snap(f: Float): Float {
        val raw = valueRange.start + f.coerceIn(0f, 1f) * range
        return if (steps > 0) {
            val stepSize = range / (steps + 1)
            (((raw - valueRange.start) / stepSize).roundToInt() * stepSize + valueRange.start)
                .coerceIn(valueRange.start, valueRange.endInclusive)
        } else raw
    }

    // Легка хаптика при «защіпуванні» на крок
    val haptic = LocalHapticFeedback.current
    var lastSnapped by remember { mutableStateOf(value) }
    fun emitChange(f: Float) {
        val v = snap(f)
        if (steps > 0 && v != lastSnapped) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastSnapped = v
        }
        onValueChange(v)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(valueRange, steps) {
                detectTapGestures { offset ->
                    emitChange(offset.x / size.width)
                }
            }
            .pointerInput(valueRange, steps) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    emitChange(change.position.x / size.width)
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cy        = size.height / 2f
            val padding   = 6.dp.toPx()
            val usableW   = size.width - padding * 2
            val thumbX    = padding + fraction * usableW
            val amplitude = 4.dp.toPx()
            val waveLen   = 26.dp.toPx()
            val strokeW   = 5.dp.toPx()

            // Активна хвиляста частина
            if (thumbX > padding + 1f) {
                val path = Path()
                var x = padding
                path.moveTo(x, cy + amplitude * sin(2f * PI.toFloat() * x / waveLen - phase))
                while (x <= thumbX) {
                    path.lineTo(x, cy + amplitude * sin(2f * PI.toFloat() * x / waveLen - phase))
                    x += 2f
                }
                drawPath(path, color, style = Stroke(strokeW, cap = StrokeCap.Round))
            }

            // Неактивна пряма частина
            if (thumbX < size.width - padding) {
                drawLine(
                    trackColor,
                    Offset(thumbX + 8.dp.toPx(), cy),
                    Offset(size.width - padding, cy),
                    strokeW, StrokeCap.Round
                )
            }

            // Повзунок — вертикальна пігулка
            drawRoundRect(
                color = color,
                topLeft = Offset(thumbX - 2.5f.dp.toPx(), cy - 13.dp.toPx()),
                size = Size(5.dp.toPx(), 26.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
        }
    }
}
