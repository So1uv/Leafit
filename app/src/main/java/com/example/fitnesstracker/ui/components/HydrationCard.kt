package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.WaterPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationCard(waterMl: Int, goalMl: Int, onAdd: (Int) -> Unit, onRemove: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    var selectedVolume by rememberSaveable { mutableStateOf(WaterPreferences.getDefaultVolume(context)) }
    var showVolumes by rememberSaveable { mutableStateOf(false) }
    var custom by rememberSaveable { mutableIntStateOf(300) }
    val goal = goalMl.coerceAtLeast(1)
    val percent = (waterMl.toLong() * 100 / goal).toInt().coerceIn(0, 999)
    val reached = waterMl >= goal
    val left = (goal - waterMl).coerceAtLeast(0)
    val glassesTotal = ((goal + selectedVolume - 1) / selectedVolume.coerceAtLeast(1)).coerceIn(1, 12)
    val glassesDone = (waterMl / selectedVolume.coerceAtLeast(1)).coerceAtMost(glassesTotal)
    fun add(volume: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onAdd(volume)
    }
    fun pick(volume: Int) {
        selectedVolume = volume
        WaterPreferences.setDefaultVolume(context, volume)
    }
    if (showVolumes) ModalBottomSheet(onDismissRequest = { showVolumes = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), containerColor = colors.surfaceContainerLow, tonalElevation = 0.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LeafCardHeader(LeafIcons.WaterFull, stringResource(R.string.water_add_action),
                subtitle = stringResource(R.string.b12_water_default) + " · $selectedVolume " + stringResource(R.string.unit_ml))
            listOf(listOf(150, 200, 250), listOf(350, 500, 750)).forEachIndexed { row, volumes ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    volumes.forEachIndexed { column, volume ->
                        val active = volume == selectedVolume
                        LeafSurface(onClick = { pick(volume); showVolumes = false; add(volume) },
                            modifier = Modifier.weight(1f).height(64.dp),
                            shape = RoundedCornerShape(
                                topStart = if (row == 0 && column == 0) 28.dp else 8.dp, topEnd = if (row == 0 && column == 2) 28.dp else 8.dp,
                                bottomStart = if (row == 1 && column == 0) 28.dp else 8.dp, bottomEnd = if (row == 1 && column == 2) 28.dp else 8.dp),
                            color = if (active) colors.primaryContainer else colors.surfaceContainerHigh,
                            contentColor = if (active) colors.onPrimaryContainer else colors.onSurface) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Text(volume.toString(), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.unit_ml), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.b12_water_custom), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeafFilledTonalIconButton(onClick = { custom = (custom - 50).coerceAtLeast(50) }, modifier = Modifier.size(52.dp)) {
                            Icon(LeafIcons.Remove, "−50")
                        }
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                            RollingNumber(custom, style = MaterialTheme.typography.displaySmall, weight = FontWeight.SemiBold)
                            Text(" " + stringResource(R.string.unit_ml), Modifier.padding(bottom = 6.dp), style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurfaceVariant)
                        }
                        LeafFilledTonalIconButton(onClick = { custom = (custom + 50).coerceAtMost(2000) }, modifier = Modifier.size(52.dp)) {
                            Icon(LeafIcons.Add, "+50")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LeafTonalButton(onClick = { pick(custom); showVolumes = false }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                            LeafFitText(stringResource(R.string.home_water_remember))
                        }
                        LeafButton(onClick = { showVolumes = false; add(custom) }, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                            Icon(LeafIcons.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                            LeafFitText(stringResource(R.string.water_add_action))
                        }
                    }
                }
            }
        }
    }
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer) {
        Box(Modifier.fillMaxWidth()) {
            WaterWave(progress = (waterMl.toFloat() / goal).coerceIn(0f, 1f), color = colors.primary, modifier = Modifier.matchParentSize())
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LeafCardHeader(if (reached) LeafIcons.CheckCircle else LeafIcons.WaterFull, stringResource(R.string.nutrition_water_title),
                    subtitle = stringResource(R.string.water_daily_target, goalMl), tone = if (reached) LeafTone.Solid else LeafTone.Primary) {
                    LeafSurface(shape = CircleShape, color = if (reached) colors.primary else colors.primaryContainer,
                        contentColor = if (reached) colors.onPrimary else colors.onPrimaryContainer) {
                        Text("$percent%", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RollingNumber(waterMl, style = MaterialTheme.typography.displayMedium, weight = FontWeight.SemiBold)
                    Text(stringResource(R.string.unit_ml), style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp))
                    Spacer(Modifier.weight(1f))
                    Text(if (reached) stringResource(R.string.b12_water_done) else stringResource(R.string.b12_water_left, left),
                        Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(glassesTotal) { index ->
                        val filled = index < glassesDone
                        val tone by animateColorAsState(if (filled) colors.primary else colors.surfaceContainerHighest, label = "glass")
                        val scale by animateFloatAsState(if (filled) 1f else .82f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                            label = "glassScale")
                        Box(Modifier.weight(1f).height(28.dp).graphicsLayer { scaleY = scale }, contentAlignment = Alignment.Center) {
                            LeafSurface(Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 12.dp,
                                bottomEnd = 12.dp), color = tone) {}
                        }
                    }
                }
                Text(stringResource(R.string.b12_water_glasses, glassesDone) + " / $glassesTotal · $selectedVolume " + stringResource(R.string.unit_ml),
                    style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val quick = listOf(150, 250, 500)
                    quick.forEachIndexed { index, volume ->
                        LeafSurface(onClick = { add(volume) }, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(topStart = if (index == 0) 24.dp else 8.dp, bottomStart = if (index == 0) 24.dp else 8.dp,
                                topEnd = if (index == quick.lastIndex) 24.dp else 8.dp, bottomEnd = if (index == quick.lastIndex) 24.dp else 8.dp),
                            color = colors.surfaceContainerHighest, contentColor = colors.onSurface) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+$volume", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafFilledTonalIconButton(enabled = waterMl > 0, onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onRemove()
                    }, modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.surfaceContainerHighest)) {
                        Icon(LeafIcons.Restore, stringResource(R.string.b12_water_undo))
                    }
                    LeafButton(onClick = { add(selectedVolume) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                        Icon(LeafIcons.filled(LeafIcons.WaterFull), null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("+ $selectedVolume ${stringResource(R.string.unit_ml)}", style = MaterialTheme.typography.titleMedium)
                    }
                    LeafFilledTonalIconButton(onClick = { showVolumes = true }, modifier = Modifier.size(56.dp)) {
                        Icon(LeafIcons.Tune, stringResource(R.string.home_water_volume_title))
                    }
                }
            }
        }
    }
}
