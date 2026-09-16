package com.example.fitnesstracker.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Read the animation only during drawing: no per-frame theme or text recomposition. */
@Composable
fun Modifier.animatedLeafBackground(color: Color, shape: Shape = RectangleShape): Modifier {
    val tone = animateColorAsState(color, tween(260), label = "surfaceTone")
    return drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        onDrawBehind { drawOutline(outline, tone.value) }
    }
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
    Surface(onClick, modifier.animatedLeafBackground(color, shape), enabled = enabled,
        shape = shape, color = Color.Transparent, contentColor = contentColor,
        tonalElevation = 0.dp, shadowElevation = 0.dp, border = border,
        interactionSource = interactionSource, content = content)
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
    Card(onClick, modifier.animatedLeafBackground(if (enabled) colors.containerColor else colors.disabledContainerColor, shape),
        enabled = enabled, shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = colors.contentColor,
            disabledContainerColor = Color.Transparent, disabledContentColor = colors.disabledContentColor),
        elevation = CardDefaults.cardElevation(0.dp), border = border,
        interactionSource = interactionSource, content = content)
}
