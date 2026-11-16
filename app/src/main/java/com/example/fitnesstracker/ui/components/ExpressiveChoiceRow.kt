package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveChoiceRow(labels: List<String>, icons: List<ImageVector>, selected: Int,
    onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (labels.isEmpty()) return
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        labels.forEachIndexed { index, label ->
            val active = index == selected
            val outer = 24.dp
            val inner = 8.dp
            fun corner(isOuter: Boolean): Dp = if (active) outer else if (isOuter) outer else inner
            val spec = spring<Dp>(dampingRatio = .7f, stiffness = 500f)
            val topStart by animateDpAsState(corner(index == 0), spec, label = "ts")
            val bottomStart by animateDpAsState(corner(index == 0), spec, label = "bs")
            val topEnd by animateDpAsState(corner(index == labels.lastIndex), spec, label = "te")
            val bottomEnd by animateDpAsState(corner(index == labels.lastIndex), spec, label = "be")
            val container by animateColorAsState(if (active) colors.primaryContainer else colors.surfaceContainerHigh, label = "seg")
            val ink by animateColorAsState(if (active) colors.onPrimaryContainer else colors.onSurfaceVariant, label = "segInk")
            val shape = RoundedCornerShape(topStart = topStart, topEnd = topEnd, bottomEnd = bottomEnd, bottomStart = bottomStart)
            Column(Modifier.weight(1f).fillMaxHeight().heightIn(min = 64.dp).clip(shape).background(container)
                .selectable(active, role = Role.Tab, interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.material3.ripple(), onClick = {
                        if (!active) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(index) }
                    }).padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp,
                    Alignment.CenterVertically)) {
                if (index < icons.size) Icon(if (active) LeafIcons.filled(icons[index]) else icons[index], null,
                    Modifier.size(22.dp), tint = ink)
                LeafFitText(label, style = MaterialTheme.typography.labelLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = ink)
            }
        }
    }
}
