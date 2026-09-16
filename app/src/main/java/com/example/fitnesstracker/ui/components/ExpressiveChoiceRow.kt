package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.ui.theme.animatedLeafBackground

@Composable
fun ExpressiveChoiceRow(labels: List<String>, icons: List<ImageVector>, selected: Int,
    onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (labels.isEmpty()) return
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val position = animateFloatAsState(selected.toFloat(), spring(.9f, 600f), label = "choicePosition")
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(28.dp))
        .animatedLeafBackground(colors.surfaceContainerLow).padding(4.dp).selectableGroup()
        .drawBehind {
            val slot = size.width / labels.size
            val logical = position.value.coerceIn(0f, (labels.size - 1).toFloat()) * slot
            val x = if (rtl) size.width - logical - slot else logical
            drawRoundRect(colors.surfaceContainerHighest, Offset(x + 2.dp.toPx(), 0f),
                Size((slot - 4.dp.toPx()).coerceAtLeast(0f), size.height), CornerRadius(24.dp.toPx()))
        }) {
        labels.forEachIndexed { index, label ->
            val active = index == selected
            Column(Modifier.weight(1f).fillMaxHeight().heightIn(min = 64.dp).clip(RoundedCornerShape(24.dp))
                .selectable(active, role = Role.Tab, interactionSource = remember { MutableInteractionSource() },
                    indication = null, onClick = {
                        if (!active) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(index) }
                    }).padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icons[index], null, Modifier.size(22.dp),
                    tint = if (active) colors.primary else colors.onSurfaceVariant)
                Text(label, style = MaterialTheme.typography.labelLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (active) colors.onSurface else colors.onSurfaceVariant)
            }
        }
    }
}
