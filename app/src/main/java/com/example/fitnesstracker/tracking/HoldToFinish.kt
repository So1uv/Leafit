package com.example.fitnesstracker.tracking

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LeafIcons
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
internal fun HoldToFinish(enabled: Boolean, onHeld: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val currentAction by rememberUpdatedState(onHeld)
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val label = stringResource(R.string.live_hold_stop)
    val accessible = stringResource(R.string.live_finish_confirmation)
    Surface(shape = CircleShape, color = colors.tertiaryContainer,
        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button; contentDescription = label
                if (!enabled) disabled()
                // Accessibility activation opens a confirmation; it never saves/discards directly.
                onClick(label = accessible) { if (enabled) currentAction(); enabled }
            }.pointerInput(enabled) {
                detectTapGestures(onPress = {
                    if (enabled) coroutineScope {
                        val hold = launch {
                            progress.snapTo(0f)
                            // Safety holds use real time even when system animations are disabled.
                            val start = android.os.SystemClock.elapsedRealtime()
                            do {
                                val fraction = ((android.os.SystemClock.elapsedRealtime() - start) / 1400f).coerceIn(0f, 1f)
                                progress.snapTo(fraction)
                                if (fraction < 1f) delay(16)
                            } while (progress.value < 1f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentAction()
                        }
                        try { tryAwaitRelease() } finally { hold.cancelAndJoin(); progress.snapTo(0f) }
                    }
                })
            }) {
        Row(Modifier.drawBehind {
            drawRect(colors.onTertiaryContainer.copy(alpha = .12f), size = Size(size.width * progress.value, size.height))
        }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp), Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val inset = 2.dp.toPx()
                    drawArc(colors.onTertiaryContainer, -90f, 360f * progress.value, false,
                        Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2),
                        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                }
                Icon(LeafIcons.Stop, null, tint = colors.onTertiaryContainer, modifier = Modifier.size(20.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onTertiaryContainer)
        }
    }
}
