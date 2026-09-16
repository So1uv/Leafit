package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlin.math.*

@Composable
fun LeafTitle(text: String, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.titleLarge,
              color: Color = MaterialTheme.colorScheme.onSurface, maxLines: Int = 2) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val reveal by animateFloatAsState(if (appeared) 1f else 0f, spring(.88f, 380f), label = "titleReveal")
    AnimatedContent(text, modifier = modifier.graphicsLayer {
        alpha = reveal.coerceIn(0f, 1f)
        translationY = (1f - reveal) * 8.dp.toPx()
        scaleX = .975f + .025f * reveal; scaleY = scaleX
    }, transitionSpec = {
        (fadeIn(tween(180)) + slideInVertically(spring(.88f, 380f)) { it / 5 } + scaleIn(spring(.9f, 380f), initialScale = .97f))
            .togetherWith(fadeOut(tween(110)) + slideOutVertically(tween(150)) { -it / 6 })
            .using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> spring(.95f, 420f) }))
    }, label = "leafTitle") { value ->
        Text(value, style = style, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

/** Uses the navigation transition's progress, so predictive back can seek and cancel it. */
@Composable
fun AnimatedContentScope.LeafPage(content: @Composable () -> Unit) {
    val radius = transition.animateDp(label = "pageCorners") { state ->
        if (state == EnterExitState.Visible) 0.dp else 28.dp
    }
    Surface(modifier = Modifier.fillMaxSize().graphicsLayer {
        shape = RoundedCornerShape(radius.value)
        clip = radius.value > 0.dp
    }, color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) { content() }
}

@Composable
fun LeafButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) .965f else 1f, spring(.82f, 650f), label = "buttonPress")
    val radius by animateDpAsState(if (pressed && enabled) 18.dp else 40.dp, spring(.92f, 600f), label = "buttonShape")
    val haptic = LocalHapticFeedback.current
    androidx.compose.material3.Button(onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
    }, enabled = enabled, modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        shape = if (shape == CircleShape) RoundedCornerShape(radius) else shape,
        colors = colors, elevation = elevation, border = border, contentPadding = contentPadding,
        interactionSource = source, content = content)
}

@Composable
fun LeafTonalButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit) {
    LeafButton(onClick, modifier, enabled, shape, colors, elevation, border, contentPadding, interactionSource, content)
}

@Composable
fun LeafSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null, enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
    interactionSource: MutableInteractionSource? = null) {
    val scale by animateFloatAsState(if (checked) 1.04f else 1f, spring(.7f, 500f), label = "switchScale")
    androidx.compose.material3.Switch(checked, onCheckedChange, modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        thumbContent = {
            AnimatedContent(checked, transitionSpec = {
                (fadeIn(tween(140)) + scaleIn(spring(.82f, 500f), initialScale = .7f))
                    .togetherWith(fadeOut(tween(90)) + scaleOut(targetScale = .7f))
            }, label = "switchThumb") { active ->
                if (thumbContent != null) thumbContent()
                else Icon(if (active) LeafIcons.Check else LeafIcons.Remove, null, Modifier.size(16.dp))
            }
        }, enabled = enabled, colors = colors, interactionSource = interactionSource)
}

class LeafRosette(private val lobes: Int = 8) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        for (i in 0..160) {
            val a = i * 2.0 * PI / 160
            val radius = size.minDimension * (.455 + .035 * cos(a * lobes))
            val x = (size.width / 2 + cos(a) * radius).toFloat()
            val y = (size.height / 2 + sin(a) * radius).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close(); return Outline.Generic(path)
    }
}

@Composable
fun LeafLoadingIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    val phase by rememberInfiniteTransition(label = "leafLoading").animateFloat(0f, 360f,
        infiniteRepeatable(tween(1900, easing = LinearEasing)), label = "loadingPhase")
    Canvas(modifier.size(44.dp).semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }) {
        val path = Path()
        for (i in 0..120) {
            val angle = i * 2.0 * PI / 120
            val wave = sin(angle * 5 + phase * PI / 180)
            val radius = size.minDimension * (.32 + .055 * wave)
            val x = (center.x + cos(angle + phase * PI / 180) * radius).toFloat()
            val y = (center.y + sin(angle + phase * PI / 180) * radius).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close(); drawPath(path, color)
    }
}

fun groupedLeafShape(index: Int, count: Int): RoundedCornerShape = RoundedCornerShape(
    topStart = if (index == 0) 28.dp else 10.dp, topEnd = if (index == 0) 28.dp else 10.dp,
    bottomStart = if (index == count - 1) 28.dp else 10.dp, bottomEnd = if (index == count - 1) 28.dp else 10.dp)

@Composable
fun LeafEmptyState(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = LeafRosette(), color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(Modifier.size(54.dp), Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
        }
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
