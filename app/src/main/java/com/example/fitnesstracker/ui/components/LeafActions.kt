package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun LeafIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(), interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit) {
    val feedback = LocalHapticFeedback.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (enabled && pressed) .94f else 1f, spring(.85f, 650f), label = "iconPress")
    androidx.compose.material3.IconButton(onClick = {
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
    }, modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).graphicsLayer { scaleX = scale; scaleY = scale }, enabled = enabled, colors = colors, interactionSource = source, content = content)
}

@Composable
fun LeafFilledTonalIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource? = null, content: @Composable () -> Unit) {
    val feedback = LocalHapticFeedback.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (enabled && pressed) .94f else 1f, spring(.85f, 650f), label = "tonalIconPress")
    val corner by animateDpAsState(if (enabled && pressed) 14.dp else 28.dp, spring(.9f, 600f), label = "tonalIconShape")
    androidx.compose.material3.FilledTonalIconButton(onClick = {
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
    }, modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).graphicsLayer { scaleX = scale; scaleY = scale }, enabled = enabled,
        shape = if (shape == CircleShape) RoundedCornerShape(corner) else shape, colors = colors, interactionSource = source, content = content)
}

@Composable
fun LeafTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = CircleShape, colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null, content: @Composable RowScope.() -> Unit) {
    val feedback = LocalHapticFeedback.current
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (enabled && pressed) .975f else 1f, spring(.85f, 650f), label = "textActionPress")
    androidx.compose.material3.TextButton(onClick = {
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick()
    }, modifier = modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).graphicsLayer { scaleX = scale; scaleY = scale }, enabled = enabled, shape = shape, colors = colors, elevation = elevation,
        border = border, contentPadding = contentPadding, interactionSource = source, content = content)
}

@Composable
fun LeafCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier,
    enabled: Boolean = true, colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource? = null) {
    val feedback = LocalHapticFeedback.current
    androidx.compose.material3.Checkbox(checked, onCheckedChange?.let { change -> { value ->
        feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove); change(value)
    } }, modifier, enabled, colors, interactionSource)
}

@Composable
fun LeafConfirmDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier, dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null, title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        com.example.fitnesstracker.ui.theme.LeafSurface(modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                icon?.let { content ->
                    com.example.fitnesstracker.ui.theme.LeafSurface(shape = RoundedCornerShape(LeafIconSize.Medium.corner),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                        Box(Modifier.size(LeafIconSize.Medium.container), contentAlignment = androidx.compose.ui.Alignment.Center) { content() }
                    }
                }
                title?.let { ProvideTextStyle(MaterialTheme.typography.titleLarge) { it() } }
                text?.let { body ->
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) { body() }
                    }
                }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    confirmButton(); dismissButton?.invoke()
                }
            }
        }
    }
}
