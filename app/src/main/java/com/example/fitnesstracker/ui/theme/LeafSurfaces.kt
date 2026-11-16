package com.example.fitnesstracker.ui.theme

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun Modifier.animatedLeafBackground(color: Color, shape: Shape = RectangleShape): Modifier {
    val tone = remember { mutableStateOf(color) }
    LaunchedEffect(color) {
        val from = tone.value
        if (from != color) animate(0f, 1f, animationSpec = tween(260)) { fraction, _ -> tone.value = lerp(from, color, fraction) }
    }
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind { drawOutline(outline, tone.value) }
    }
}

@Composable
private fun Modifier.leafPress(source: MutableInteractionSource, enabled: Boolean): Modifier {
    val scale = remember { mutableFloatStateOf(1f) }
    LaunchedEffect(source, enabled) {
        if (!enabled) { scale.floatValue = 1f; return@LaunchedEffect }
        var job: Job? = null
        source.interactions.collect { interaction ->
            val target = if (interaction is PressInteraction.Press) .975f else 1f
            if (interaction is PressInteraction.Press || interaction is PressInteraction.Release || interaction is PressInteraction.Cancel) {
                job?.cancel()
                val from = scale.floatValue
                job = launch { animate(from, target, animationSpec = spring(.7f, 700f)) { value, _ -> scale.floatValue = value } }
            }
        }
    }
    return graphicsLayer { scaleX = scale.floatValue; scaleY = scale.floatValue }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafSurface(modifier: Modifier = Modifier, shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface, contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp, shadowElevation: Dp = 0.dp, border: BorderStroke? = null,
    content: @Composable () -> Unit) {
    Surface(modifier.animatedLeafBackground(color, shape), shape = shape,
        color = Color.Transparent, contentColor = contentColor, tonalElevation = 0.dp,
        shadowElevation = 0.dp, border = border, content = content)
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafSurface(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = RectangleShape, color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color), tonalElevation: Dp = 0.dp, shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null, interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit) {
    val feedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Surface({ feedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onClick() },
        modifier.leafPress(source, enabled).animatedLeafBackground(color, shape), enabled = enabled,
        shape = shape, color = Color.Transparent, contentColor = contentColor,
        tonalElevation = 0.dp, shadowElevation = 0.dp, border = border,
        interactionSource = source, content = content)
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafCard(modifier: Modifier = Modifier, shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(), elevation: CardElevation = CardDefaults.cardElevation(0.dp),
    border: BorderStroke? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.animatedLeafBackground(colors.containerColor, shape), shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = colors.contentColor,
            disabledContainerColor = Color.Transparent, disabledContentColor = colors.disabledContentColor), elevation = CardDefaults.cardElevation(0.dp),
        border = border, content = content)
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafCard(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CardDefaults.shape, colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(0.dp), border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null, content: @Composable ColumnScope.() -> Unit) {
    val feedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Card({ feedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onClick() },
        modifier.leafPress(source, enabled).animatedLeafBackground(if (enabled) colors.containerColor else colors.disabledContainerColor, shape),
        enabled = enabled, shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = colors.contentColor,
            disabledContainerColor = Color.Transparent, disabledContentColor = colors.disabledContentColor),
        elevation = CardDefaults.cardElevation(0.dp), border = border,
        interactionSource = source, content = content)
}
