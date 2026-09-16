package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R

@Composable
fun OnboardingProgress(step: Int, labels: List<String>, onSelect: (Int) -> Unit) {
    val palette = MaterialTheme.colorScheme
    val progress by animateFloatAsState((step + 1f) / labels.size.coerceAtLeast(1),
        spring(dampingRatio = 1f, stiffness = 400f), label = "setupProgress")
    Surface(shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 30.dp, bottomStart = 12.dp),
        color = palette.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val inset = 4.dp.toPx()
                        repeat(labels.size) { i ->
                            val angle = 360f / labels.size
                            val arc = Size(size.width - inset * 2, size.height - inset * 2)
                            drawArc(palette.onSecondaryContainer.copy(alpha = .12f), -90f + angle * i + 6f,
                                angle - 12f, false, Offset(inset, inset), arc,
                                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                            val filled = (progress * labels.size - i).coerceIn(0f, 1f)
                            if (filled > 0f) drawArc(palette.primary, -90f + angle * i + 6f,
                                (angle - 12f) * filled, false, Offset(inset, inset), arc,
                                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                        }
                    }
                    Icon(LeafIcons.Eco, null, Modifier.size(30.dp), tint = palette.primary)
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.onboarding_progress_label, step + 1, labels.size),
                        style = MaterialTheme.typography.labelLarge, color = palette.onSecondaryContainer)
                    Text(labels[step], style = MaterialTheme.typography.headlineSmall, color = palette.onSecondaryContainer)
                }
            }
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val icons = listOf(LeafIcons.Person, LeafIcons.Straighten, LeafIcons.Tune)
                labels.forEachIndexed { index, label ->
                    val selected = index == step
                    val bg by animateColorAsState(if (selected) palette.surfaceContainerLowest else palette.secondaryContainer,
                        label = "setupStage")
                    Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(if (selected) 24.dp else 14.dp)).background(bg)
                        .selectable(selected, enabled = index <= step, role = Role.Tab, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(index) }),
                        contentAlignment = Alignment.Center) {
                        Icon(if (index < step) LeafIcons.Check else icons[index], label,
                            tint = if (index <= step) palette.primary else palette.onSecondaryContainer.copy(alpha = .55f),
                            modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeafGenderChoices(selected: String, onSelect: (String) -> Unit, options: List<Pair<String, String>>) {
    val haptic = LocalHapticFeedback.current
    FlowRow(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (key, label) ->
            FilterChip(selected = selected == key, onClick = {
                if (selected != key) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelect(key) }
            }, label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                leadingIcon = { Icon(when (key) { "male" -> LeafIcons.Male; "female" -> LeafIcons.Female; else -> LeafIcons.Eco },
                    null, Modifier.size(18.dp)) },
                modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(50), border = null,
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer))
        }
    }
    if (selected == "other") Text(stringResource(R.string.profile_leaf_explanation),
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp))
}
