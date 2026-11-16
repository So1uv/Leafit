package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.ui.theme.LeafSurface

@Composable
fun LeafViewSelector(second: Boolean, firstLabel: String, secondLabel: String, onSelect: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(firstLabel, secondLabel).forEachIndexed { index, label ->
            val active = second == (index == 1)
            val inner by animateDpAsState(if (active) 24.dp else 8.dp, spring(.7f, 500f), label = "viewSelectorShape")
            LeafSurface(onClick = { onSelect(index == 1) },
                modifier = Modifier.weight(1f).semantics { selected = active; role = Role.Tab },
                shape = RoundedCornerShape(topStart = if (index == 0) 24.dp else inner, bottomStart = if (index == 0) 24.dp else inner,
                    topEnd = if (index == 1) 24.dp else inner, bottomEnd = if (index == 1) 24.dp else inner),
                color = if (active) colors.primaryContainer else colors.surfaceContainerHigh,
                contentColor = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant) {
                Row(Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (active) {
                        androidx.compose.material3.Icon(LeafIcons.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    LeafFitText(label, style = MaterialTheme.typography.labelLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}
