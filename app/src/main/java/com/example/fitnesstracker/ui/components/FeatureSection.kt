package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.ui.theme.LeafSurface

@Composable
fun FeatureSection(title: String, modifier: Modifier = Modifier, subtitle: String? = null,
    icon: ImageVector? = null, tone: LeafTone = LeafTone.Primary,
    content: @Composable ColumnScope.() -> Unit) {
    LeafSurface(modifier.fillMaxWidth(), shape = RoundedCornerShape(LeafCorners.card),
        color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(LeafSpacing.card), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LeafCardHeader(icon, title, subtitle = subtitle, tone = tone)
            content()
        }
    }
}

@Composable
fun FeatureBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    LeafIconBadge(icon, modifier, size = LeafIconSize.Medium, tone = LeafTone.Primary)
}

@Composable
fun FeatureEmpty(title: String, detail: String, icon: ImageVector) {
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(LeafCorners.card),
        color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            LeafIconBadge(icon, size = LeafIconSize.Large, tone = LeafTone.Secondary, shape = LeafRosette(8))
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun FeatureChoice(selected: Boolean, label: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    val corner by animateDpAsState(if (selected) 16.dp else 28.dp, spring(.9f, 500f), label = "choiceShape")
    val tone by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHigh,
        spring(stiffness = 500f), label = "choiceTone")
    LeafSurface(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(corner), color = tone,
        contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selected) Icon(LeafIcons.CheckCircle, null, Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
