package com.example.fitnesstracker.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.ui.components.ExpressiveDialog
import com.example.fitnesstracker.ui.components.GlowProgressBar
import com.example.fitnesstracker.ui.components.MetricText
import com.example.fitnesstracker.ui.components.MetricValueWithSuffix
import com.example.fitnesstracker.ui.components.WaterWave
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
    onNutritionClick: () -> Unit = {},
    onWorkoutClick: () -> Unit = {}
) {
    val profile         by vm.profile.collectAsState()
    val waterMl         by vm.totalWaterMl.collectAsState()
    val caloriesBurned  by vm.todayCaloriesBurned.collectAsState()
    val lastSleep       by vm.lastSleep.collectAsState()
    val achievements    by vm.unlockedAchievements.collectAsState()
    val allAchievements by vm.allAchievements.collectAsState()
    val weekDays        by vm.weekActivityDays.collectAsState()
    val weekMinutes     by vm.weekActivityMinutes.collectAsState()
    val steps           by vm.steps.collectAsState()
    val stepsAvailable  by vm.stepsSensorAvailable.collectAsState()

    var showAchievements by remember { mutableStateOf(false) }

    val dailyCal      = profile?.let { HealthCalc.dailyCalories(it.weightKg, it.heightCm, it.age, it.gender) } ?: 2000f
    val waterGoalMl   = 2000
    val sleepGoalH    = 8f
    val sleep         = lastSleep
    val sleepH        = sleep?.let { (it.wakeTime - it.bedTime) / 3600000f } ?: 0f

    val calProgress   = (caloriesBurned / dailyCal).coerceIn(0f, 1f)
    val waterProgress = (waterMl.toFloat() / waterGoalMl).coerceIn(0f, 1f)
    val sleepProgress = (sleepH / sleepGoalH).coerceIn(0f, 1f)

    val animCalories by animateFloatAsState(caloriesBurned, tween(900, easing = EaseOutCubic), label = "calCount")
    val animSteps    by animateIntAsState(steps, tween(900, easing = EaseOutCubic), label = "stepCount")
    val animWater    by animateIntAsState(waterMl, tween(700, easing = EaseOutCubic), label = "waterCount")
    val haptic = LocalHapticFeedback.current

    if (showAchievements) {
        AchievementsDialog(allAchievements = allAchievements, onDismiss = { showAchievements = false })
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            run {
                DailySummaryCard(
                    calories = animCalories.roundToInt(),
                    steps = animSteps,
                    stepsAvailable = stepsAvailable,
                    stepsProgress = (animSteps.toFloat() / 10000f).coerceIn(0f, 1f),
                    waterMl = animWater,
                    waterProgress = waterProgress,
                    calProgress = calProgress,
                    sleepProgress = sleepProgress,
                    onWorkoutClick = onWorkoutClick,
                    onAddWater = { ml ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.addWater(ml)
                    },
                    onRemoveWater = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.removeLastWater()
                    }
                )
            }

            WeekActivityCard(
                weekDays = weekDays,
                weekMinutes = weekMinutes,
                onWorkoutClick = onWorkoutClick
            )

            LastSleepCard(
                sleep = sleep,
                sleepH = sleepH,
                onSleepClick = onSleepClick
            )
        }

        HomeFloatingHeader(
            collapsed = false,
            profileName = profile?.name ?: "Спортсмен",
            avatarUri = profile?.avatarUri,
            achievementsCount = achievements.size,
            onProfileClick = onProfileClick,
            onAchievementsClick = { showAchievements = true },
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HomeFloatingHeader(
    collapsed: Boolean,
    profileName: String,
    avatarUri: String?,
    achievementsCount: Int,
    onProfileClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(32.dp)
    val greeting = greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    val greetingIcon = greetingIconForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)),
        shape = shape,
        color = if (collapsed) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Crossfade(
            targetState = collapsed,
            animationSpec = tween(durationMillis = 240, easing = EaseOutCubic),
            label = "headerCrossfade"
        ) { isCollapsed ->
            if (isCollapsed) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarBubble(profileName, avatarUri, size = 40.dp, onClick = onProfileClick)
                    Column(Modifier.weight(1f)) {
                        Text(profileName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(greeting, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    HeaderIconButton(Icons.Default.EmojiEvents, achievementsCount, onAchievementsClick)
                    HeaderIconButton(Icons.Default.Settings, null, onSettingsClick)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvatarBubble(profileName, avatarUri, size = 52.dp, onClick = onProfileClick)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                Alignment.Center
                            ) {
                                Icon(greetingIcon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(17.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(greeting, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(profileName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HeaderIconButton(Icons.Default.EmojiEvents, achievementsCount, onAchievementsClick)
                    HeaderIconButton(Icons.Default.Settings, null, onSettingsClick)
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(icon: ImageVector, badgeCount: Int?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(52.dp).bouncyClick(onClick = onClick),
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
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun DailySummaryCard(
    calories: Int,
    steps: Int,
    stepsAvailable: Boolean,
    stepsProgress: Float,
    waterMl: Int,
    waterProgress: Float,
    calProgress: Float,
    sleepProgress: Float,
    onWorkoutClick: () -> Unit,
    onAddWater: (Int) -> Unit,
    onRemoveWater: () -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.05f)) {
                ActivityRings(
                    outer  = calProgress,
                    outerColor = MaterialTheme.colorScheme.error,
                    middle = waterProgress,
                    middleColor = MaterialTheme.colorScheme.primary,
                    inner  = sleepProgress,
                    innerColor = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(12.dp))
                MetricText("$calories", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.error, weight = FontWeight.Black)
                Text("ккал спалено", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RingLegend("Кал", MaterialTheme.colorScheme.error)
                    RingLegend("Вода", MaterialTheme.colorScheme.primary)
                    RingLegend("Сон", MaterialTheme.colorScheme.tertiary)
                }
            }
            Column(Modifier.weight(0.95f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    icon = Icons.Default.DirectionsWalk,
                    title = "КРОКИ",
                    value = if (stepsAvailable) "$steps" else "—",
                    subtitle = if (stepsAvailable) "сьогодні" else "немає датчика",
                    progress = if (stepsAvailable) stepsProgress else 0f,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = onWorkoutClick
                )
                WaterMetricTile(
                    waterMl = waterMl,
                    waterProgress = waterProgress,
                    onAddWater = onAddWater,
                    onRemoveWater = onRemoveWater
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: Color,
    onClick: (() -> Unit)?
) {
    val base = Modifier.fillMaxWidth().height(118.dp)
    val clickableModifier = if (onClick != null) base.bouncyClick(onClick = onClick) else base
    Card(
        modifier = clickableModifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (title == "ВОДА") {
                WaterWave(progress = progress, color = color, modifier = Modifier.fillMaxSize())
            }
            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(color.copy(alpha = 0.14f)), Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(21.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        MetricText(value, style = MaterialTheme.typography.headlineSmall, weight = FontWeight.Black)
                        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                ThinProgressBar(progress = progress, color = color)
            }
        }
    }
}

@Composable
private fun WaterMetricTile(
    waterMl: Int,
    waterProgress: Float,
    onAddWater: (Int) -> Unit,
    onRemoveWater: () -> Unit
) {
    val color = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            WaterWave(progress = waterProgress, color = color, modifier = Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(13.dp))
                            .background(color.copy(alpha = 0.14f)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.WaterDrop, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "ВОДА",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        MetricValueWithSuffix(
                            value = if (waterMl >= 1000) "%.1f".format(waterMl / 1000f) else waterMl.toString(),
                            suffix = if (waterMl >= 1000) " л" else " мл",
                            valueStyle = MaterialTheme.typography.headlineSmall,
                            suffixStyle = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            valueWeight = FontWeight.Black
                        )
                        Text("ціль 2.0л", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(8.dp))
                ThinProgressBar(progress = waterProgress, color = color)
                Spacer(Modifier.height(9.dp))

                WaterActionDock(
                    waterMl = waterMl,
                    onAddWater = onAddWater,
                    onRemoveWater = onRemoveWater
                )
            }
        }
    }
}

@Composable
private fun WaterActionDock(
    waterMl: Int,
    onAddWater: (Int) -> Unit,
    onRemoveWater: () -> Unit
) {
    var showVolumes by remember { mutableStateOf(false) }
    var selectedVolume by remember { mutableStateOf(250) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = if (waterMl > 0) Modifier.size(32.dp).bouncyClick(onClick = onRemoveWater) else Modifier.size(32.dp),
            shape = CircleShape,
            color = if (waterMl > 0) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Зменшити воду",
                    modifier = Modifier.size(18.dp),
                    tint = if (waterMl > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .bouncyClick { onAddWater(selectedVolume) },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Додати $selectedVolume мл",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Box {
            Surface(
                modifier = Modifier.size(32.dp).bouncyClick { showVolumes = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Обрати об'єм",
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showVolumes) {
                WaterVolumePopup(
                    selected = selectedVolume,
                    onSelect = { volume ->
                        selectedVolume = volume
                        showVolumes = false
                        onAddWater(volume)
                    },
                    onDismiss = { showVolumes = false }
                )
            }
        }
    }
}

/**
 * Маленьке плаваюче вікно вибору об'єму — спливає над кнопкою налаштувань
 * з пружинною анімацією масштабу, в стилі контекстних меню iOS,
 * але на токенах Material 3.
 */
@Composable
private fun WaterVolumePopup(
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val yOffset = with(density) { (-8).dp.roundToPx() }

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.72f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMedium),
        label = "popupScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(140, easing = EaseOutCubic),
        label = "popupAlpha"
    )

    Popup(
        alignment = Alignment.BottomEnd,
        offset = IntOffset(0, yOffset),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    transformOrigin = TransformOrigin(1f, 1f)
                },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Об'єм порції",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(150, 250, 350).forEach { v ->
                        VolumeOptionPill(volume = v, selected = v == selected) { onSelect(v) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(500, 750, 1000).forEach { v ->
                        VolumeOptionPill(volume = v, selected = v == selected) { onSelect(v) }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeOptionPill(volume: Int, selected: Boolean, onClick: () -> Unit) {
    val label = if (volume >= 1000) "1 л" else "$volume"
    Surface(
        modifier = Modifier
            .width(56.dp)
            .height(34.dp)
            .bouncyClick(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeekActivityCard(weekDays: Set<Int>, weekMinutes: List<Float>, onWorkoutClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().bouncyClick(onClick = onWorkoutClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Тиждень активності", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    DatePill(todayLabel())
                }
                MetricText("${weekDays.size}/7", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, weight = FontWeight.Black)
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(14.dp))
            val maxMinutes = (weekMinutes.maxOrNull() ?: 0f).coerceAtLeast(1f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                listOf("Пн","Вт","Ср","Чт","Пт","Сб","Нд").forEachIndexed { idx, label ->
                    val minutes = weekMinutes.getOrElse(idx) { 0f }
                    val active  = minutes > 0f
                    val frac    = (minutes / maxMinutes).coerceIn(0f, 1f)
                    val targetH = if (active) (14f + 46f * frac) else 6f
                    val animH by animateDpAsState(targetH.dp, tween(700, idx * 60, EaseOutCubic), label = "bar$idx")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.width(22.dp).height(60.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.width(22.dp).height(animH).clip(RoundedCornerShape(11.dp)).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Натисніть для перегляду історії", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DatePill(text: String) {
    Row(
        Modifier.clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LastSleepCard(sleep: com.example.fitnesstracker.data.entities.SleepRecord?, sleepH: Float, onSleepClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().bouncyClick(onClick = onSleepClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                Alignment.Center
            ) {
                Icon(Icons.Default.Bedtime, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Останній сон",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (sleep != null) "${"%.1f".format(sleepH)} год · ${"★".repeat(sleep.qualityScore)}" else "Немає даних — торкніться",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActivityRings(outer: Float, outerColor: Color, middle: Float, middleColor: Color, inner: Float, innerColor: Color) {
    val aOuter  by animateFloatAsState(outer,  tween(900, easing = EaseOutCubic), label = "o")
    val aMiddle by animateFloatAsState(middle, tween(900, 100, EaseOutCubic), label = "m")
    val aInner  by animateFloatAsState(inner,  tween(900, 200, EaseOutCubic), label = "i")

    Box(
        modifier = Modifier.size(166.dp).drawBehind {
            val stroke = 13.dp.toPx()
            fun ring(progress: Float, color: Color, inset: Float) {
                val s = Size(size.width - inset * 2, size.height - inset * 2)
                val o = Offset(inset, inset)
                drawArc(color.copy(alpha = 0.16f), -90f, 360f, false, o, s, style = Stroke(stroke))
                drawArc(color, -90f, 360f * progress, false, o, s, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            ring(aOuter, outerColor, stroke / 2f)
            ring(aMiddle, middleColor, stroke / 2f + stroke + 6.dp.toPx())
            ring(aInner, innerColor, stroke / 2f + (stroke + 6.dp.toPx()) * 2)
        },
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(R.drawable.ic_running), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun RingLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AchievementsDialog(allAchievements: List<Achievement>, onDismiss: () -> Unit) {
    val motivationalPhrases = listOf(
        "Вставай, герою дивана. Твоє тіло вже думає, що вийшло на пенсію.",
        "Ти не пропустив тренування. Тренування пропустило твою жалюгідну спробу.",
        "Біль у м'язах? Це вони ржуть над тим, як ти вчора відпочивав.",
        "Давай, чемпіоне. Покажи холодильнику, хто в домі господар.",
        "Ти прийшов качатися чи просто перевірити, чи працює кондиціонер?",
        "Ще один підхід — і тіло повірить, що ти серйозно.",
        "Вставай. Навіть лиходії з фільмів тренуються частіше.",
        "Зроби повторення. Дзеркало вже втомилося чекати.",
        "Мотивація не прийде. Її треба наздогнати.",
        "Тренуйся, поки твій кіт не почав качатися краще за тебе."
    )
    val randomPhrase = remember { motivationalPhrases.random() }
    val unlocked = allAchievements.count { it.unlockedAt != null }
    val total = allAchievements.size.coerceAtLeast(1)
    val progress = unlocked.toFloat() / total

    ExpressiveDialog(
        onDismissRequest = onDismiss,
        title = { Text("Досягнення") },
        confirmButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(50)) { Text("Закрити") }
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Hero — велике число і прогрес
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents, null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    MetricText(
                                        "$unlocked",
                                        style = MaterialTheme.typography.displaySmall,
                                        weight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "/ ${allAchievements.size}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    "розблоковано",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        GlowProgressBar(
                            progress = progress,
                            color = MaterialTheme.colorScheme.primary,
                            height = 8.dp
                        )
                    }
                }

                // Цитата — badge зліва ніколи не «дрібнішає» при переносі тексту
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bolt, null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            randomPhrase,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (allAchievements.isEmpty()) {
                    Text(
                        "Ще немає досягнень.\nПочніть тренуватись!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allAchievements.forEach { a ->
                            val isUnlocked = a.unlockedAt != null
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isUnlocked) MaterialTheme.colorScheme.surfaceContainerHighest
                                        else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        Modifier.size(42.dp).clip(CircleShape).background(
                                            if (isUnlocked) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.EmojiEvents, null,
                                            tint = if (isUnlocked) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            a.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            a.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!isUnlocked) Icon(
                                        Icons.Default.Lock, null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ThinProgressBar(progress: Float, color: Color) {
    GlowProgressBar(progress = progress, color = color, height = 5.dp)
}

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..10  -> "ДОБРОГО РАНКУ"
    in 11..16 -> "ДОБРОГО ДНЯ"
    in 17..21 -> "ДОБРОГО ВЕЧОРА"
    else      -> "ДОБРОЇ НОЧІ"
}

private fun greetingIconForHour(hour: Int): ImageVector = when (hour) {
    in 5..16 -> Icons.Default.WbSunny
    in 17..21 -> Icons.Default.NightsStay
    else -> Icons.Default.Bedtime
}

private fun todayLabel(): String {
    val cal = Calendar.getInstance()
    val days = listOf("Неділя","Понеділок","Вівторок","Середа","Четвер","П'ятниця","Субота")
    val months = listOf("січня","лютого","березня","квітня","травня","червня", "липня","серпня","вересня","жовтня","листопада","грудня")
    return "${days[cal.get(Calendar.DAY_OF_WEEK) - 1]}, ${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]}"
}
