package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** One semantic/touch target for the label and checkbox. */
@Composable
fun LeafCheckboxRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String,
                    modifier: Modifier = Modifier, enabled: Boolean = true) {
    val interactions = remember { MutableInteractionSource() }
    val palette = MaterialTheme.colorScheme
    val fill by animateColorAsState(
        if (checked) palette.primaryContainer else palette.surfaceContainerHighest,
        spring(stiffness = 450f), label = "checkboxFill")
    val check by animateFloatAsState(if (checked) 1f else 0f,
        spring(dampingRatio = .65f, stiffness = 500f), label = "checkboxCheck")
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp)
        .toggleable(value = checked, enabled = enabled, role = Role.Checkbox,
            interactionSource = interactions, indication = null, onValueChange = onCheckedChange)
        .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp),
            contentAlignment = Alignment.Center) {
            Box(Modifier.size(26.dp).graphicsLayer { alpha = if (enabled) 1f else .38f }
                .background(fill, RoundedCornerShape(8.dp))
                ,
                contentAlignment = Alignment.Center) {
                Icon(LeafIcons.Check, null, tint = palette.onPrimaryContainer,
                    modifier = Modifier.size(20.dp).graphicsLayer {
                        scaleX = check.coerceAtLeast(0f); scaleY = check.coerceAtLeast(0f)
                        alpha = check.coerceIn(0f, 1f)
                    })
            }
        }
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = palette.onSurface.copy(alpha = if (enabled) 1f else .38f),
            modifier = Modifier.weight(1f))
    }
}
