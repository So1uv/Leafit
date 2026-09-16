package com.example.fitnesstracker.ui.screens.home

import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.animatedLeafBackground
import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.DayNoteCard
import com.example.fitnesstracker.ui.components.NoteHistoryDialog
import com.example.fitnesstracker.ui.components.WeightTrackingCard
import com.example.fitnesstracker.ui.components.GlowProgressBar
import com.example.fitnesstracker.ui.components.RollingNumber
import com.example.fitnesstracker.ui.components.bouncyClick
import com.example.fitnesstracker.utils.HealthCalc
import com.example.fitnesstracker.viewmodel.HomeViewModel
import java.io.File
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSleepClick: () -> Unit = {},
    onWorkoutClick: () -> Unit = {}
) {
    val profile         by vm.profile.collectAsState()
    val weights by vm.weightHistory.collectAsState()
    val caloriesBurned  by vm.todayCaloriesBurned.collectAsState()
    val lastSleep       by vm.lastSleep.collectAsState()
    val achievements    by vm.unlockedAchievements.collectAsState()
    val allAchievements by vm.allAchievements.collectAsState()
    val workoutCount by vm.workoutCount.collectAsState()
    val sleepCount by vm.sleepCount.collectAsState()
    val weekMinutes     by vm.weekActivityMinutes.collectAsState()
    val weekStepsByDay  by vm.weekStepsByDay.collectAsState()
    val steps           by vm.steps.collectAsState()
    val stepsAvailable  by vm.stepsSensorAvailable.collectAsState()

    var showAchievements by remember { mutableStateOf(false) }
    var showNoteHistory by remember { mutableStateOf(false) }
    val note by vm.todayNote.collectAsState()
    val noteHistory by vm.noteHistory.collectAsState()

    val headerState = rememberLeafHeaderState()
    val headerHeight = leafHeaderInset()
    val context = LocalContext.current
    val dailyCal      = profile?.let { HealthCalc.dailyCalories(it.weightKg, it.heightCm, it.age, it.gender) } ?: 2000f
    val stepsGoal     = remember { com.example.fitnesstracker.utils.GoalPreferences.getStepsGoal(context) }
    val sleepGoalH    = remember { com.example.fitnesstracker.utils.GoalPreferences.getSleepGoalMinutes(context) / 60f }
    val sleep         = lastSleep
    val sleepH        = sleep?.let { (it.wakeTime - it.bedTime) / 3600000f } ?: 0f

    val calProgress   = (caloriesBurned / dailyCal).coerceIn(0f, 1f)
    val sleepProgress = (sleepH / sleepGoalH).coerceIn(0f, 1f)

    val haptic = LocalHapticFeedback.current

    if (showAchievements) {
        com.example.fitnesstracker.ui.components.AchievementsSheet(allAchievements, workoutCount, sleepCount,
            onDismiss = { showAchievements = false })
    }
    if (showNoteHistory) {
        NoteHistoryDialog(notes = noteHistory, onDismiss = { showNoteHistory = false },
            onDelete = vm::deleteNote, onTogglePin = vm::toggleNotePin, onSave = vm::updateNote)
    }

    Box(Modifier.fillMaxSize().animatedLeafBackground(MaterialTheme.colorScheme.surface).nestedScroll(headerState.connection)) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = headerHeight + 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "activity") { DailySummaryCard(
                calories = caloriesBurned.roundToInt(),
                steps = steps,
                stepsGoal = stepsGoal,
                stepsAvailable = stepsAvailable,
                stepsProgress = (steps.toFloat() / stepsGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                calProgress = calProgress,
                sleepProgress = sleepProgress,
                onWorkoutClick = onWorkoutClick
            )
            }
            item(key = "weight") { WeightTrackingCard(history = weights, onAdd = vm::addWeight,
                onUpdate = vm::updateWeight, onDelete = vm::deleteWeight,
                modifier = Modifier.padding(horizontal = 16.dp)) }

            item(key = "week") { WeekActivityCard(
                weekMinutes = weekMinutes,
                weekSteps = weekStepsByDay
            )

            }
            item(key = "note") { DayNoteCard(
                note = note,
                onSave = { vm.saveNote(it) },
                onHistory = { showNoteHistory = true },
                onDelete = vm::deleteNote,
                onTogglePin = vm::toggleNotePin
            ) }
        }

        HomeFloatingHeader(
            headerState = headerState,
            profileName = profile?.name ?: stringResource(R.string.home_default_name),
            avatarUri = profile?.avatarUri,
            achievementsCount = achievements.size,
            onProfileClick = onProfileClick,
            onAchievementsClick = { showAchievements = true },
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .leafHeaderScrim(MaterialTheme.colorScheme.surface, headerState)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HomeFloatingHeader(
    headerState: LeafHeaderState,
    profileName: String,
    avatarUri: String?,
    achievementsCount: Int,
    onProfileClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    Row(modifier.fillMaxWidth().height(com.example.fitnesstracker.ui.components.appHeaderHeight()),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AvatarBubble(profileName, avatarUri, size = 48.dp, onClick = onProfileClick)
        Column(Modifier.weight(1f).leafHeaderMotion(headerState)) {
            LeafTitle(greeting, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            LeafTitle(profileName, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
        HeaderIconButton(LeafIcons.EmojiEvents, achievementsCount, onAchievementsClick)
        HeaderIconButton(LeafIcons.Settings, null, onSettingsClick)
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, badgeCount: Int?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(48.dp).bouncyClick(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(8.dp).size(17.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    Alignment.Center
                ) {
                    Text("$badgeCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun AvatarBubble(name: String, avatarUri: String?, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri != null) {
            AsyncImage(
                model = if (avatarUri.startsWith("/")) File(avatarUri) else avatarUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun DailySummaryCard(
    calories: Int,
    steps: Int,
    stepsGoal: Int,
    stepsAvailable: Boolean,
    stepsProgress: Float,
    calProgress: Float,
    sleepProgress: Float,
    onWorkoutClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = colors.primaryContainer) {
                    Box(Modifier.size(42.dp), Alignment.Center) {
                        Icon(LeafIcons.DonutLarge, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(22.dp))
                    }
                }
                Text(stringResource(R.string.home_daily_activity), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BoxWithConstraints(Modifier.weight(1.08f)) {
                    ActivityRings(
                        outer = stepsProgress, outerColor = if (colors.surface.luminance() < .5f) Color(0xFFC5E8D3) else Color(0xFF355E48),
                        middle = calProgress, middleColor = if (colors.surface.luminance() < .5f) Color(0xFFF1D5B8) else Color(0xFF795536),
                        inner = sleepProgress, innerColor = if (colors.surface.luminance() < .5f) Color(0xFFDCD3F0) else Color(0xFF63517C),
                        diameter = maxWidth.coerceAtMost(208.dp))
                }
                Box(Modifier.weight(1f)) {
                    RingProgressDock(caloriesProgress = calProgress, stepsProgress = stepsProgress, sleepProgress = sleepProgress)
                }
            }
            Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerLow) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.home_steps_label), style = MaterialTheme.typography.labelLarge,
                                color = colors.onSurfaceVariant)
                            if (stepsAvailable) RollingNumber(steps, style = MaterialTheme.typography.headlineMedium,
                                weight = FontWeight.Medium, color = colors.onSurface)
                            else Text("—", style = MaterialTheme.typography.headlineMedium)
                        }
                        FilledTonalIconButton(onClick = onWorkoutClick) {
                            Icon(com.example.fitnesstracker.ui.components.LeafIcons.DirectionsRun, stringResource(R.string.nav_workout))
                        }
                    }
                    LinearProgressIndicator(progress = { if (stepsAvailable) stepsProgress else 0f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = colors.primary, trackColor = colors.secondaryContainer)
                    Text(if (stepsAvailable) stringResource(R.string.home_steps_goal, stepsGoal, (stepsProgress * 100).roundToInt())
                        else stringResource(R.string.home_no_sensor), style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WeekActivityCard(weekMinutes: List<Float>, weekSteps: List<Int>) {
    var selectedMetric by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }
    Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(Modifier.size(48.dp), Alignment.Center) {
                        Icon(LeafIcons.CalendarViewWeek, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_week_activity), style = MaterialTheme.typography.titleLarge)
                }
            }
            WeekMetricSwitch(selectedMetric) { selectedMetric = it }
            com.example.fitnesstracker.ui.components.LeafWeekHistory(
                metric = if (selectedMetric == 0) com.example.fitnesstracker.ui.components.LeafWeekMetric.STEPS else com.example.fitnesstracker.ui.components.LeafWeekMetric.MOVEMENT,
                currentValues = if (selectedMetric == 0) weekSteps.map { it.toFloat() } else weekMinutes,
                unit = stringResource(if (selectedMetric == 0) R.string.unit_steps_short else R.string.unit_min_short),
                accent = MaterialTheme.colorScheme.primary)

        }
    }
}

@Composable
private fun WeekMetricSwitch(selectedMetric: Int, onSelect: (Int) -> Unit) {
    com.example.fitnesstracker.ui.components.ExpressiveChoiceRow(
        labels = listOf(stringResource(R.string.home_week_metric_steps), stringResource(R.string.home_week_metric_minutes)),
        icons = listOf(LeafIcons.DirectionsWalk, LeafIcons.Timer),
        selected = selectedMetric, onSelect = onSelect)
}

@Composable
private fun ActivityRings(
    outer: Float,
    outerColor: Color,
    middle: Float,
    middleColor: Color,
    inner: Float,
    innerColor: Color,
    diameter: androidx.compose.ui.unit.Dp = 184.dp
) {
    val aOuter by animateFloatAsState(outer, spring(dampingRatio = .85f, stiffness = 140f), label = "o")
    val aMiddle by animateFloatAsState(middle, spring(dampingRatio = .85f, stiffness = 140f), label = "m")
    val aInner by animateFloatAsState(inner, spring(dampingRatio = .85f, stiffness = 140f), label = "i")

    Box(
        modifier = Modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        val size = diameter
        Box(
            modifier = Modifier.size(size).drawBehind {
                val stroke = this.size.width * 0.072f
                fun ring(progress: Float, color: Color, inset: Float) {
                    val ringSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                    val offset = Offset(inset, inset)
                    val radius = ringSize.width / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    drawCircle(color.copy(alpha = .15f), radius, center, style = Stroke(stroke))
                    val value = progress.coerceIn(0f, 1f)
                    if (value > .001f) {
                        val brush = Brush.sweepGradient(listOf(color,
                            androidx.compose.ui.graphics.lerp(color, Color.White, .22f), color), center)
                        rotate(-90f, center) {
                            if (value >= .9999f) drawCircle(brush, radius, center, style = Stroke(stroke))
                            else drawArc(brush, 0f, 360f * value, false, offset, ringSize,
                                style = Stroke(stroke, cap = StrokeCap.Round))
                        }
                    }
                }
                ring(aOuter, outerColor, stroke / 2f)
                ring(aMiddle, middleColor, stroke / 2f + stroke + stroke * 0.44f)
                ring(aInner, innerColor, stroke / 2f + (stroke + stroke * 0.44f) * 2)
            },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(diameter * .27f),
                shape = com.example.fitnesstracker.ui.components.LeafRosette(),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        com.example.fitnesstracker.ui.components.LeafIcons.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RingProgressDock(
    caloriesProgress: Float,
    stepsProgress: Float,
    sleepProgress: Float
) {
    val mint = com.example.fitnesstracker.ui.theme.LeafTones.mint
    val mintInk = com.example.fitnesstracker.ui.theme.LeafTones.onMint
    val peach = com.example.fitnesstracker.ui.theme.LeafTones.warm
    val peachInk = com.example.fitnesstracker.ui.theme.LeafTones.onWarm
    val lavender = com.example.fitnesstracker.ui.theme.LeafTones.sleep
    val lavenderInk = com.example.fitnesstracker.ui.theme.LeafTones.onSleep
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RingProgressItem(
            modifier = Modifier.fillMaxWidth(),
            icon = LeafIcons.LocalFireDepartment,
            label = stringResource(R.string.home_legend_cal),
            progress = caloriesProgress,
            color = peachInk, container = peach
        )
        RingProgressItem(
            modifier = Modifier.fillMaxWidth(),
            icon = com.example.fitnesstracker.ui.components.LeafIcons.DirectionsWalk,
            label = stringResource(R.string.home_steps_label),
            progress = stepsProgress,
            color = mintInk, container = mint
        )
        RingProgressItem(
            modifier = Modifier.fillMaxWidth(),
            icon = com.example.fitnesstracker.ui.components.LeafIcons.Bedtime,
            label = stringResource(R.string.home_legend_sleep),
            progress = sleepProgress,
            color = lavenderInk, container = lavender
        )
    }
}

@Composable
private fun RingProgressItem(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    progress: Float,
    color: Color,
    container: Color
) {
    val pct = (progress.coerceIn(0f, 1f) * 100).roundToInt()
    Surface(
        modifier = modifier, shape = RoundedCornerShape(20.dp),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Text(
                    "$pct%",
                    style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ThinProgressBar(progress: Float, color: Color) {
    GlowProgressBar(progress = progress, color = color, height = 5.dp)
}

@Composable
private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..10  -> stringResource(R.string.greeting_morning)
    in 11..16 -> stringResource(R.string.greeting_day)
    in 17..21 -> stringResource(R.string.greeting_evening)
    else      -> stringResource(R.string.greeting_night)
}

private fun greetingIconForHour(hour: Int): ImageVector = when (hour) {
    in 5..16 -> LeafIcons.WbSunny
    in 17..21 -> com.example.fitnesstracker.ui.components.LeafIcons.NightsStay
    else -> com.example.fitnesstracker.ui.components.LeafIcons.Bedtime
}
