package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.localizedTitle
import com.example.fitnesstracker.data.entities.localizedDesc
import com.example.fitnesstracker.utils.DateUtils
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsSheet(achievements: List<Achievement>, workoutCount: Int, sleepCount: Int, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    var filter by rememberSaveable { mutableIntStateOf(0) }
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
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                LeafTitle(stringResource(R.string.achievements_title), style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val minCell = if (density.fontScale > 1.3f) 400.dp else 300.dp
                val available = (maxWidth - 40.dp).coerceAtLeast(1.dp)
                val columnCount = floor((available + 12.dp) / (minCell + 12.dp)).toInt().coerceAtLeast(1)
                val width = (available - 12.dp * (columnCount - 1)) / columnCount
                val bodyWidth = with(density) { ((width - 40.dp).roundToPx() - 2).coerceAtLeast(1) }
                val titleWidth = with(density) { ((width - 40.dp - 68.dp).roundToPx() - 2).coerceAtLeast(1) }
                val titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                val bodyStyle = MaterialTheme.typography.bodyMedium
                val statusStyle = MaterialTheme.typography.labelLarge
                fun height(text: String, style: TextStyle, widthPx: Int): Int =
                    measurer.measure(text, style = style, constraints = Constraints(maxWidth = widthPx)).size.height
                // Shared measured slots keep cards equal without truncating long localized text.
                val titleHeight = with(density) {
                    (achievements.maxOfOrNull { height(it.localizedTitle(context), titleStyle, titleWidth) } ?: 0).toDp()
                }.coerceAtLeast(56.dp)
                val bodyHeight = with(density) {
                    (achievements.maxOfOrNull { height(it.localizedDesc(context), bodyStyle, bodyWidth) } ?: 0).toDp()
                }
                val statusHeight = with(density) {
                    (achievements.maxOfOrNull { height(status(it), statusStyle, (bodyWidth - 36.dp.roundToPx()).coerceAtLeast(1)) } ?: 0).toDp()
                }.coerceAtLeast(24.dp)
                LazyVerticalGrid(columns = GridCells.Adaptive(minCell),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(shape = RoundedCornerShape(28.dp), color = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Icon(LeafIcons.EmojiEvents, null, Modifier.size(40.dp))
                                    Column(Modifier.weight(1f)) {
                                        LeafTitle("$earnedCount / ${achievements.size}", style = MaterialTheme.typography.headlineLarge)
                                        Text(stringResource(R.string.achievements_unlocked), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                LinearProgressIndicator(progress = { earnedCount.toFloat() / achievements.size.coerceAtLeast(1) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                                    color = colors.onSecondaryContainer,
                                    trackColor = colors.onSecondaryContainer.copy(alpha = .14f))
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ExpressiveChoiceRow(
                            labels = listOf(stringResource(R.string.achievements_filter_all),
                                stringResource(R.string.achievements_filter_earned), stringResource(R.string.achievements_locked)),
                            icons = listOf(LeafIcons.EmojiEvents, LeafIcons.CheckCircle, LeafIcons.Lock),
                            selected = filter, onSelect = { filter = it })
                    }
                    if (ordered.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(24.dp)) {
                            LeafEmptyState(stringResource(R.string.achievements_filter_empty), LeafIcons.EmojiEvents)
                        }
                    }
                    items(ordered, key = { it.id }) { a ->
                        val earned = a.unlockedAt != null
                        val count = progress(a)
                        val sleepBadge = a.id.contains("sleep")
                        val noteBadge = a.id.startsWith("note_")
                        val badgeColor = if (!earned) colors.surfaceContainerLow else when {
                            sleepBadge -> colors.tertiaryContainer
                            noteBadge -> com.example.fitnesstracker.ui.theme.LeafTones.warm
                            else -> colors.primaryContainer
                        }
                        val badgeInk = if (!earned) colors.onSurfaceVariant else when {
                            sleepBadge -> colors.onTertiaryContainer
                            noteBadge -> com.example.fitnesstracker.ui.theme.LeafTones.onWarm
                            else -> colors.onPrimaryContainer
                        }
                        var revealed by remember(a.id) { mutableStateOf(false) }
                        LaunchedEffect(a.id) { revealed = true }
                        val badgeScale by animateFloatAsState(if (revealed) 1f else .88f, spring(.82f, 400f), label = "badgeReveal")
                        Card(modifier = Modifier.animateItem(), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp,
                            bottomEnd = 28.dp, bottomStart = if (earned) 12.dp else 28.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer)) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(Modifier.fillMaxWidth().height(titleHeight),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(modifier = Modifier.graphicsLayer { scaleX = badgeScale; scaleY = badgeScale },
                                        shape = if (earned) LeafRosette() else RoundedCornerShape(28.dp), color = badgeColor) {
                                        Box(Modifier.size(56.dp), Alignment.Center) {
                                            Icon(when (a.id) {
                                                "first_workout", "ten_workouts" -> LeafIcons.Exercise
                                                "water_week" -> LeafIcons.WaterDrop
                                                "first_sleep", "sleep_week", "sleep_three", "sleep_fourteen", "sleep_thirty", "sleep_sixty", "sleep_context" -> LeafIcons.Bedtime
                                                "note_first", "note_seven", "note_fourteen", "note_thirty", "note_pinned" -> LeafIcons.Edit
                                                "first_meal", "calorie_goal" -> LeafIcons.Dining
                                                "profile_complete" -> LeafIcons.Person
                                                else -> LeafIcons.EmojiEvents
                                            }, null, Modifier.size(28.dp), tint = badgeInk)
                                        }
                                    }
                                    Text(a.localizedTitle(context), style = titleStyle, modifier = Modifier.weight(1f))
                                }
                                Text(a.localizedDesc(context), style = bodyStyle,
                                    color = colors.onSurfaceVariant, modifier = Modifier.fillMaxWidth().height(bodyHeight))
                                Spacer(Modifier.height(2.dp))
                                Row(Modifier.fillMaxWidth().height(statusHeight), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(if (earned) LeafIcons.CheckCircle else LeafIcons.Lock, null,
                                        Modifier.size(24.dp), tint = colors.onSurfaceVariant)
                                    Text(status(a), style = statusStyle, modifier = Modifier.weight(1f), color = colors.onSurface)
                                }
                                Box(Modifier.fillMaxWidth().height(6.dp)) {
                                    if (!earned && count != null) LinearProgressIndicator(
                                        progress = { count.first.toFloat() / count.second },
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50)),
                                        color = colors.primary, trackColor = colors.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
