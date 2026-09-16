package com.example.fitnesstracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafCard
import com.example.fitnesstracker.ui.theme.LeafSurface

@Composable
fun HistoryRecordCard(title: String, date: String, icon: ImageVector,
    badgeColor: Color, badgeInk: Color, onEdit: () -> Unit, onDelete: () -> Unit,
    expanded: Boolean, onToggle: () -> Unit,
    modifier: Modifier = Modifier, groupIndex: Int = 0, groupCount: Int = 1,
    content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val rotation = animateFloatAsState(if (expanded) 180f else 0f, spring(.95f, 550f), label = "historyChevron")
    LeafCard(modifier.fillMaxWidth(), shape = groupedLeafShape(groupIndex, groupCount),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)) {
        LeafSurface(onClick = onToggle, color = Color.Transparent, shape = groupedLeafShape(groupIndex, groupCount)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LeafSurface(shape = RoundedCornerShape(16.dp), color = badgeColor) {
                    Box(Modifier.size(44.dp), Alignment.Center) { Icon(icon, null, Modifier.size(24.dp), tint = badgeInk) }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(date, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(LeafIcons.ExpandMore, stringResource(if (expanded) R.string.history_collapse else R.string.history_expand),
                    Modifier.size(22.dp).graphicsLayer { rotationZ = rotation.value }, tint = colors.onSurfaceVariant)
            }
        }
        // AnimatedVisibility alone owns height. Hidden details leave composition after the exit.
        AnimatedVisibility(expanded,
            enter = expandVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(150)),
            exit = shrinkVertically(spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow)) + fadeOut(tween(100))) {
            Column(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LeafSurface(shape = RoundedCornerShape(20.dp), color = colors.surfaceContainerLow) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    LeafTonalButton(onClick = onEdit, shape = CircleShape) {
                        Icon(LeafIcons.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.common_edit))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete), tint = colors.error)
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySectionLabel(title: String, count: Int, icon: ImageVector) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
            Box(Modifier.size(32.dp), Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
            }
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(count.toString(), Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
