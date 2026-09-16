package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.LeafButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.WaterPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationCard(waterMl: Int, goalMl: Int, onAdd: (Int) -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedVolume by rememberSaveable { mutableStateOf(WaterPreferences.getDefaultVolume(context)) }
    var showVolumes by rememberSaveable { mutableStateOf(false) }
    var rememberChoice by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    fun add(volume: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onAdd(volume)
    }
    if (showVolumes) ModalBottomSheet(
        onDismissRequest = { showVolumes = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.water_add_action), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.home_water_volume_title), style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant)
            listOf(listOf(150, 250, 350), listOf(500, 750, 1000)).forEach { volumes ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    volumes.forEach { volume ->
                        LeafTonalButton(onClick = {
                            selectedVolume = volume
                            if (rememberChoice) WaterPreferences.setDefaultVolume(context, volume)
                            showVolumes = false
                            add(volume)
                        }, modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Text("$volume ${stringResource(R.string.unit_ml)}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            LeafCheckboxRow(checked = rememberChoice,
                onCheckedChange = { rememberChoice = it },
                label = stringResource(R.string.home_water_remember))
        }
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer)) {
        Box(Modifier.fillMaxWidth()) {
            WaterWave(progress = (waterMl.toFloat() / goalMl.coerceAtLeast(1)).coerceIn(0f, 1f),
                color = colors.primary, modifier = Modifier.matchParentSize())
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(14.dp), color = colors.primaryContainer) {
                        Box(Modifier.size(38.dp), Alignment.Center) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.WaterDrop, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(stringResource(R.string.nutrition_water_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RollingNumber(waterMl, style = MaterialTheme.typography.headlineMedium, weight = FontWeight.SemiBold)
                    Text(stringResource(R.string.unit_ml), style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                }
                Text("$waterMl / $goalMl ${stringResource(R.string.unit_ml)}", style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(enabled = waterMl > 0, onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onRemove()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Remove, stringResource(R.string.water_remove_action))
                    }
                    LeafButton(onClick = { add(selectedVolume) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("$selectedVolume ${stringResource(R.string.unit_ml)}")
                    }
                    FilledTonalIconButton(onClick = { showVolumes = true }, modifier = Modifier.size(48.dp)) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.ExpandMore, stringResource(R.string.home_water_volume_title))
                    }
                }
            }
        }
    }
}
