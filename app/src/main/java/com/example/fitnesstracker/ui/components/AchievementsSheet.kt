package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.localizedDesc
import com.example.fitnesstracker.data.entities.localizedTitle
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.ui.theme.LeafTones
import com.example.fitnesstracker.utils.DateUtils

private fun achievementIcon(id: String): ImageVector = when {
    id == "first_workout" || id == "ten_workouts" -> LeafIcons.Exercise
    id == "water_week" -> LeafIcons.WaterFull
    id.contains("sleep") -> LeafIcons.Bedtime
    id.startsWith("note_") -> LeafIcons.EditNote
    id == "first_meal" || id == "calorie_goal" -> LeafIcons.Restaurant
    id == "profile_complete" -> LeafIcons.Person
    else -> LeafIcons.Trophy
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsSheet(achievements: List<Achievement>, workoutCount: Int, sleepCount: Int, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var openedId by rememberSaveable { mutableStateOf<String?>(null) }
    val earnedCount = achievements.count { it.unlockedAt != null }
    val ordered = remember(achievements, filter) {
        achievements.filter { filter == 0 || (it.unlockedAt != null) == (filter == 1) }
            .sortedWith(compareByDescending<Achievement> { it.unlockedAt != null }.thenByDescending { it.unlockedAt ?: 0L })
    }
    fun progress(a: Achievement): Pair<Int, Int>? = when (a.id) {
        "first_workout" -> workoutCount.coerceIn(0, 1) to 1
        "ten_workouts" -> workoutCount.coerceIn(0, 10) to 10
        "first_sleep" -> sleepCount.coerceIn(0, 1) to 1
        "sleep_week" -> sleepCount.coerceIn(0, 7) to 7
        "sleep_three" -> sleepCount.coerceIn(0, 3) to 3
        "sleep_fourteen" -> sleepCount.coerceIn(0, 14) to 14
        "sleep_thirty" -> sleepCount.coerceIn(0, 30) to 30
        "sleep_sixty" -> sleepCount.coerceIn(0, 60) to 60
        else -> null
    }
    fun status(a: Achievement): String = a.unlockedAt?.let {
        context.getString(R.string.achievements_earned_date, DateUtils.formatDate(it))
    } ?: progress(a)?.let {
        context.getString(R.string.achievements_progress_count, it.first, it.second)
    } ?: context.getString(R.string.achievements_locked)
    val nearest = remember(achievements, workoutCount, sleepCount) {
        achievements.filter { it.unlockedAt == null }
            .mapNotNull { a -> progress(a)?.let { a to it.first.toFloat() / it.second.coerceAtLeast(1) } }
            .maxByOrNull { it.second }?.first
    }
    achievements.firstOrNull { it.id == openedId }?.let { opened ->
        LeafInfoSheet(title = opened.localizedTitle(context), details = listOf(opened.localizedDesc(context), status(opened)),
            onDismiss = { openedId = null }, icon = achievementIcon(opened.id),
            tone = if (opened.unlockedAt == null) LeafTone.Neutral else if (opened.id.contains("sleep")) LeafTone.Tertiary else LeafTone.Primary)
    }
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow, tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                LeafTitle(stringResource(R.string.achievements_title), style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f))
                LeafIconButton(onClick = onDismiss) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
            }
            LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AchievementHero(earnedCount, achievements.size, nearest?.localizedTitle(context), nearest?.let { status(it) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ExpressiveChoiceRow(
                        labels = listOf(stringResource(R.string.achievements_filter_all),
                            stringResource(R.string.achievements_filter_earned), stringResource(R.string.achievements_locked)),
                        icons = listOf(LeafIcons.Trophy, LeafIcons.CheckCircle, LeafIcons.Lock),
                        selected = filter, onSelect = { filter = it }, modifier = Modifier.padding(vertical = 4.dp))
                }
                if (ordered.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    LeafSurface(color = colors.surfaceContainer, shape = RoundedCornerShape(24.dp)) {
                        LeafEmptyState(stringResource(R.string.achievements_filter_empty), LeafIcons.Trophy)
                    }
                }
                itemsIndexed(ordered, key = { _, a -> a.id }) { index, a ->
                    val earned = a.unlockedAt != null
                    val count = progress(a)
                    val sleepBadge = a.id.contains("sleep")
                    val noteBadge = a.id.startsWith("note_")
                    val container = if (!earned) colors.surfaceContainerHighest else when {
                        sleepBadge -> colors.tertiary
                        noteBadge -> LeafTones.onWarm
                        else -> colors.primary
                    }
                    val ink = if (!earned) colors.onSurfaceVariant else when {
                        sleepBadge -> colors.onTertiary
                        noteBadge -> LeafTones.warm
                        else -> colors.onPrimary
                    }
                    val tile = if (!earned) colors.surfaceContainer else when {
                        sleepBadge -> colors.tertiaryContainer
                        noteBadge -> LeafTones.warm
                        else -> colors.primaryContainer
                    }
                    val tileInk = if (!earned) colors.onSurface else when {
                        sleepBadge -> colors.onTertiaryContainer
                        noteBadge -> LeafTones.onWarm
                        else -> colors.onPrimaryContainer
                    }
                    val reveal = remember(a.id) { Animatable(0f) }
                    LaunchedEffect(a.id) {
                        kotlinx.coroutines.delay((index.coerceAtMost(10)) * 35L)
                        reveal.animateTo(1f, spring(.62f, 380f))
                    }
                    LeafSurface(onClick = { openedId = a.id }, modifier = Modifier.animateItem().fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp), color = tile, contentColor = tileInk) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(84.dp).graphicsLayer {
                                val scale = .6f + .4f * reveal.value
                                scaleX = scale; scaleY = scale; alpha = reveal.value.coerceIn(0f, 1f)
                            }, contentAlignment = Alignment.Center) {
                                if (!earned && count != null) {
                                    val track = colors.outlineVariant
                                    val bar = colors.primary
                                    val share = count.first.toFloat() / count.second.coerceAtLeast(1)
                                    Canvas(Modifier.fillMaxSize()) {
                                        val stroke = 5.dp.toPx()
                                        val inset = stroke / 2f
                                        val arc = Size(size.width - stroke, size.height - stroke)
                                        drawArc(track, -90f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
                                        drawArc(bar, -90f, 360f * share * reveal.value.coerceIn(0f, 1f), false, Offset(inset, inset), arc,
                                            style = Stroke(stroke, cap = StrokeCap.Round))
                                    }
                                }
                                LeafSurface(shape = if (earned) LeafRosette(8) else CircleShape, color = container, contentColor = ink) {
                                    Box(Modifier.size(if (earned) 76.dp else 64.dp), Alignment.Center) {
                                        Icon(if (earned) LeafIcons.filled(achievementIcon(a.id)) else achievementIcon(a.id), null,
                                            Modifier.size(if (earned) 36.dp else 28.dp))
                                    }
                                }
                            }
                            Text(a.localizedTitle(context), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
                                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            LeafSurface(shape = CircleShape, color = if (earned) colors.surface.copy(alpha = .55f) else colors.surfaceContainerHighest,
                                contentColor = if (earned) tileInk else colors.onSurfaceVariant) {
                                Row(Modifier.padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(if (earned) LeafIcons.filled(LeafIcons.CheckCircle) else LeafIcons.Lock, null, Modifier.size(14.dp))
                                    Text(if (earned) DateUtils.formatDate(a.unlockedAt ?: 0L)
                                        else count?.let { "${it.first}/${it.second}" } ?: stringResource(R.string.achievements_locked),
                                        style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementHero(earned: Int, total: Int, nearestTitle: String?, nearestStatus: String?) {
    val colors = MaterialTheme.colorScheme
    val share = if (total > 0) earned.toFloat() / total else 0f
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(share) { sweep.animateTo(share, tween(900, easing = FastOutSlowInEasing)) }
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                    val track = colors.onSecondaryContainer.copy(alpha = .14f)
                    val bar = colors.primary
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val inset = stroke / 2f
                        val arc = Size(size.width - stroke, size.height - stroke)
                        drawArc(track, -90f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
                        drawArc(bar, -90f, 360f * sweep.value, false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    LeafIconBadge(LeafIcons.Trophy, size = LeafIconSize.Large, tone = LeafTone.Solid, shape = LeafRosette(8))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeafFitText("$earned / $total", style = MaterialTheme.typography.displaySmall)
                    Text(stringResource(R.string.achievements_unlocked), style = MaterialTheme.typography.bodyLarge)
                    LeafSurface(shape = CircleShape, color = colors.surface.copy(alpha = .6f), contentColor = colors.onSurface) {
                        Text("${(share * 100).toInt()}%", Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (nearestTitle != null) LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                color = colors.surface.copy(alpha = .6f), contentColor = colors.onSurface) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(LeafIcons.filled(LeafIcons.Flag), null, Modifier.size(22.dp), tint = colors.primary)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.b11_achievement_next), style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant)
                        Text(nearestTitle, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (nearestStatus != null) Text(nearestStatus, style = MaterialTheme.typography.labelLarge, color = colors.primary,
                        maxLines = 1)
                }
            }
        }
    }
}
