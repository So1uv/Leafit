package com.example.fitnesstracker.ui.screens.workout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.sharing.ShareWorkoutButton
import com.example.fitnesstracker.tracking.SavedRouteButton
import com.example.fitnesstracker.tracking.SavedWorkoutTelemetry
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafCardHeader
import com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton
import com.example.fitnesstracker.ui.components.LeafFitText
import com.example.fitnesstracker.ui.components.LeafFullscreenDialog
import com.example.fitnesstracker.ui.components.LeafIconBadge
import com.example.fitnesstracker.ui.components.LeafIconSize
import com.example.fitnesstracker.ui.components.LeafIcons
import com.example.fitnesstracker.ui.components.LeafRosette
import com.example.fitnesstracker.ui.components.LeafTone
import com.example.fitnesstracker.ui.components.MetricExplanation
import com.example.fitnesstracker.ui.components.groupedLeafShape
import com.example.fitnesstracker.ui.components.leafToneColors
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.HapticTexture
import com.example.fitnesstracker.utils.Haptics
import com.example.fitnesstracker.workout.ExerciseBest
import com.example.fitnesstracker.workout.WorkoutJournal
import com.example.fitnesstracker.workout.beats
import com.example.fitnesstracker.workout.exerciseKey
import java.util.Locale

private class ResultTileData(val icon: ImageVector, val label: String, val value: String, val tone: LeafTone,
    val info: (@Composable () -> Unit)? = null)

private fun Modifier.rise(progress: Animatable<Float, AnimationVector1D>, order: Int): Modifier = graphicsLayer {
    val t = (progress.value * 2.2f - order.coerceAtMost(6) * .18f).coerceIn(0f, 1f)
    alpha = t
    translationY = (1f - t) * 40.dp.toPx()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutResultScreen(workout: Workout, onClose: () -> Unit, previous: Map<String, ExerciseBest> = emptyMap(), showRecords: Boolean = true) {
    val plan = remember(workout.metrics) { WorkoutJournal.decode(workout.metrics) }
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val enter = remember { Animatable(0f) }
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        Haptics.play(context, HapticTexture.CONFIRM)
        pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val spin by rememberInfiniteTransition(label = "resultSpin").animateFloat(0f, 360f,
        infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "resultAngle")
    val records = remember(plan, previous) {
        plan?.exercises?.filter { entry -> entry.beats(previous[exerciseKey(entry.name, entry.mode)]) }.orEmpty()
    }
    val doneExercises = remember(plan) { plan?.exercises?.filter { it.sets.any { set -> set.completed } }.orEmpty() }
    val tiles = buildList {
        if (workout.distanceMeters > 0f) add(ResultTileData(LeafIcons.Distance, stringResource(R.string.live_km),
            String.format(Locale.getDefault(), "%.2f", workout.distanceMeters / 1000f), LeafTone.Primary))
        if (workout.steps > 0) add(ResultTileData(LeafIcons.Steps, stringResource(R.string.unit_steps_short),
            workout.steps.toString(), LeafTone.Secondary))
        if (workout.caloriesBurned > 0f) add(ResultTileData(LeafIcons.LocalFireDepartment, stringResource(R.string.unit_kcal),
            workout.caloriesBurned.toInt().toString(), LeafTone.Tertiary))
        if (plan != null) {
            add(ResultTileData(LeafIcons.Checklist, stringResource(R.string.share_sets), plan.completedCount.toString(), LeafTone.Primary))
            add(ResultTileData(LeafIcons.FitnessCenter, stringResource(R.string.share_exercises), doneExercises.size.toString(),
                LeafTone.Secondary))
            if (plan.volumeKg > 0) {
                val volumeTitle = stringResource(R.string.workout_result_volume)
                val volumeFacts = listOf(stringResource(R.string.metric_volume_formula),
                    stringResource(R.string.metric_workout_sets, plan.completedCount))
                add(ResultTileData(LeafIcons.Scale, volumeTitle, String.format(Locale.getDefault(), "%.0f %s", plan.volumeKg,
                    stringResource(R.string.unit_kg)), LeafTone.Tertiary) { MetricExplanation(volumeTitle, volumeFacts) })
            }
        }
    }
    LeafFullscreenDialog(onDismissRequest = onClose) {
        LeafSurface(Modifier.fillMaxSize(), color = colors.surface) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LeafFilledTonalIconButton(onClick = onClose) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
                    Spacer(Modifier.weight(1f))
                    MetricExplanation(stringResource(R.string.workout_result_title), listOf(stringResource(R.string.workout_result_estimate)))
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(112.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }, contentAlignment = Alignment.Center) {
                            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = spin }.clip(LeafRosette(8)).background(colors.primary))
                            Icon(LeafIcons.Check, null, Modifier.size(52.dp), tint = colors.onPrimary)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.b10_result_hero), Modifier.rise(enter, 0), style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center)
                        Text(listOfNotNull(plan?.title?.takeIf { it.isNotBlank() }, DateUtils.formatDate(workout.startTime)).joinToString(" · "),
                            Modifier.rise(enter, 0), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }

                    LeafSurface(Modifier.fillMaxWidth().rise(enter, 1), shape = RoundedCornerShape(36.dp), color = colors.primaryContainer,
                        contentColor = colors.onPrimaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LeafSurface(shape = CircleShape, color = colors.primary, contentColor = colors.onPrimary) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(LeafIcons.Timer, null, Modifier.size(16.dp))
                                    Text(stringResource(R.string.workout_result_duration), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                }
                            }
                            LeafFitText(DateUtils.formatDuration(workout.durationSeconds), style = MaterialTheme.typography.displayLarge,
                                textAlign = TextAlign.Center)
                        }
                    }

                    if (tiles.isNotEmpty()) Column(Modifier.rise(enter, 2), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tiles.chunked(2).forEach { pair ->
                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { ResultTile(it, Modifier.weight(1f).fillMaxHeight()) }
                            }
                        }
                    }

                    if (showRecords && records.isNotEmpty()) LeafSurface(Modifier.fillMaxWidth().rise(enter, 3),
                        shape = RoundedCornerShape(28.dp), color = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LeafIconBadge(LeafIcons.Trophy, container = colors.tertiary, content = colors.onTertiary, shape = LeafRosette(8))
                                Text(stringResource(R.string.workout_result_records, records.size), Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleLarge)
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                records.forEach { ResultChip(it.name, colors.surface.copy(alpha = .6f), colors.onTertiaryContainer) }
                            }
                        }
                    }

                    if (workout.distanceMeters > 0f) Column(Modifier.rise(enter, 4), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SavedWorkoutTelemetry(workout.metrics, workout.durationSeconds, workout.distanceMeters)
                        if (workout.metrics.contains("leaf_route_v1")) SavedRouteButton(workout.id, workout.metrics)
                    }

                    if (plan != null) LeafSurface(Modifier.fillMaxWidth().rise(enter, 5), shape = RoundedCornerShape(28.dp),
                        color = colors.surfaceContainerLow) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LeafCardHeader(LeafIcons.FitnessCenter, stringResource(R.string.workout_result_exercises),
                                Modifier.padding(horizontal = 4.dp),
                                subtitle = if (plan.completedCount == 0) stringResource(R.string.workout_result_empty)
                                    else stringResource(R.string.b10_result_sets, plan.completedCount))
                            if (doneExercises.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                doneExercises.forEachIndexed { index, entry ->
                                    val sets = entry.sets.filter { it.completed && it.hasResult(entry.mode) }
                                    val record = showRecords && records.any { it === entry }
                                    LeafSurface(Modifier.fillMaxWidth(), shape = groupedLeafShape(index, doneExercises.size),
                                        color = colors.surfaceContainerHigh) {
                                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(entry.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                                if (record) Icon(LeafIcons.Trophy, null, Modifier.size(20.dp), tint = colors.tertiary)
                                                ResultChip(entry.sets.count { it.completed }.toString(), colors.primaryContainer,
                                                    colors.onPrimaryContainer)
                                            }
                                            if (sets.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                sets.forEachIndexed { number, set ->
                                                    ResultChip("${number + 1} · " + setLabel(set, entry.mode), colors.surface, colors.onSurface)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (workout.note.isNotBlank()) LeafSurface(Modifier.fillMaxWidth().rise(enter, 6), shape = RoundedCornerShape(28.dp),
                        color = colors.surfaceContainerLow) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LeafCardHeader(LeafIcons.EditNote, stringResource(R.string.b10_result_note), tone = LeafTone.Tertiary)
                            Text(workout.note, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShareWorkoutButton(workout.id, Modifier.weight(1f).heightIn(min = 56.dp))
                    LeafButton(onClick = onClose, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                        Icon(LeafIcons.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.workout_result_done), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultTile(tile: ResultTileData, modifier: Modifier = Modifier) {
    val tone = leafToneColors(tile.tone)
    LeafSurface(modifier, shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                LeafIconBadge(tile.icon, size = LeafIconSize.Small, container = tone.container, content = tone.content, shape = CircleShape)
                Spacer(Modifier.weight(1f))
                tile.info?.invoke()
            }
            LeafFitText(tile.value, Modifier.padding(end = 8.dp), style = MaterialTheme.typography.headlineMedium)
            Text(tile.label, Modifier.padding(end = 8.dp), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ResultChip(text: String, container: androidx.compose.ui.graphics.Color, content: androidx.compose.ui.graphics.Color) {
    LeafSurface(shape = RoundedCornerShape(12.dp), color = container, contentColor = content) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, maxLines = 1,
            overflow = TextOverflow.Ellipsis)
    }
}
