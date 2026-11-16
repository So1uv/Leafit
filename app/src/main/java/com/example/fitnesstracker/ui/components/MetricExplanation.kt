package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface

@Composable
fun MetricExplanation(title: String, details: List<String>, missing: String? = null) {
    var visible by rememberSaveable(title) { mutableStateOf(false) }
    IconButton(onClick = { visible = true }) {
        Icon(LeafIcons.Info, stringResource(R.string.metric_explain) + ": " + title,
            Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (visible) LeafInfoSheet(title = title, details = details, missing = missing, onDismiss = { visible = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeafInfoSheet(title: String, details: List<String>, onDismiss: () -> Unit, missing: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = LeafIcons.Info, tone: LeafTone = LeafTone.Primary) {
    val colors = MaterialTheme.colorScheme
    val facts = details.filter { it.isNotBlank() }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = colors.surfaceContainerLow, tonalElevation = 0.dp) {
        Column(Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LeafIconBadge(icon, size = LeafIconSize.Large, tone = tone, shape = LeafRosette(8))
                Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(stringResource(R.string.b7_info_caption), style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            if (facts.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                facts.forEachIndexed { index, fact ->
                    LeafSurface(Modifier.fillMaxWidth(), shape = groupedLeafShape(index, facts.size), color = colors.surfaceContainer) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            LeafSurface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                                Box(Modifier.size(28.dp), Alignment.Center) {
                                    Text("${index + 1}", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Text(fact, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                        }
                    }
                }
            }
            missing?.takeIf { it.isNotBlank() }?.let { message ->
                LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(LeafIcons.Lightbulb, null, Modifier.size(22.dp))
                        Text(message, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            LeafButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(50)) { Text(stringResource(R.string.b7_info_ok)) }
        }
    }
}
