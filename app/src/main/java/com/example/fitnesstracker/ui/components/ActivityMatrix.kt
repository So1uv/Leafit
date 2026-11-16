package com.example.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.currentDayFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun matrixLevel(value: Float): Int = when {
    value <= .001f -> 0
    value < .34f -> 1
    value < .67f -> 2
    else -> 3
}

@Composable
fun ActivityMatrix(intensities: List<Float>, modifier: Modifier = Modifier, title: String? = null) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val locale = appLocale()
    val dayStart by remember { currentDayFlow() }.collectAsState(initial = DateUtils.todayStart())
    val today = remember(dayStart) { Instant.ofEpochMilli(dayStart).atZone(ZoneId.systemDefault()).toLocalDate() }
    val levels = remember(intensities) {
        List(49) { index -> matrixLevel(intensities.getOrElse(index) { 0f }.let { if (it.isFinite()) it.coerceIn(0f, 1f) else 0f }) }
    }
    val activeDays = remember(levels) { levels.count { it > 0 } }
    val streak = remember(levels) {
        val from = if (levels[48] > 0) 48 else 47
        var count = 0
        while (from - count >= 0 && levels[from - count] > 0) count++
        count
    }
    var selected by rememberSaveable(dayStart) { mutableIntStateOf(48) }
    val fullDate = remember(locale) { DateTimeFormatter.ofPattern("EEE, d MMMM", locale) }
    val weekday = remember(locale) { DateTimeFormatter.ofPattern("EE", locale) }
    val labels = listOf(R.string.matrix_no_activity, R.string.matrix_low, R.string.matrix_medium, R.string.matrix_high)
    val fills = listOf(colors.surfaceContainerHighest, colors.secondaryContainer, colors.primaryContainer, colors.primary)
    val inks = listOf(colors.onSurfaceVariant, colors.onSecondaryContainer, colors.onPrimaryContainer, colors.onPrimary)
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    LeafSurface(modifier.fillMaxWidth(), shape = RoundedCornerShape(LeafCorners.card), color = colors.surfaceContainerLow) {
        Column(Modifier.padding(LeafSpacing.card), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LeafCardHeader(LeafIcons.CalendarMonth, title ?: stringResource(R.string.profile_activity_matrix),
                subtitle = stringResource(R.string.matrix_active_days, activeDays)) {
                MatrixPill(stringResource(R.string.b10_matrix_weeks), colors.surfaceContainerHighest, colors.onSurfaceVariant)
            }

            LeafSurface(shape = RoundedCornerShape(24.dp), color = colors.primaryContainer, contentColor = colors.onPrimaryContainer) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedContent(selected, Modifier.weight(1f), transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(180)) + slideInVertically { it / 3 * direction }) togetherWith
                            (fadeOut(tween(120)) + slideOutVertically { -it / 3 * direction })
                    }, label = "matrixDay") { index ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(today.minusDays((48 - index).toLong()).format(fullDate), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(labels[levels[index]]), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                        repeat(3) { step ->
                            val on = levels[selected] > step
                            val bar by animateColorAsState(if (on) colors.primary else colors.onPrimaryContainer.copy(alpha = .16f),
                                label = "matrixBar")
                            Box(Modifier.width(8.dp).height((12 + step * 8).dp).clip(CircleShape).background(bar))
                        }
                    }
                }
            }

            Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { day ->
                        Text(today.minusDays((48 - day).toLong()).format(weekday), Modifier.weight(1f), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
                    }
                }
                repeat(7) { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(7) { day ->
                            val index = week * 7 + day
                            val active = index == selected
                            val level = levels[index]
                            val date = today.minusDays((48 - index).toLong())
                            val description = date.format(fullDate) + ": " + stringResource(labels[level])
                            val bg by animateColorAsState(fills[level], spring(stiffness = Spring.StiffnessMedium), label = "matrixCell")
                            val corner by animateIntAsState(if (active) 50 else 30,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "matrixShape")
                            val scale by animateFloatAsState(if (active) 1f else .92f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "matrixScale")
                            val shape = RoundedCornerShape(corner)
                            Box(Modifier.weight(1f).aspectRatio(1f)
                                .graphicsLayer {
                                    val t = (appear.value * 1.8f - index / 48f * .8f).coerceIn(0f, 1f)
                                    alpha = t
                                    scaleX = scale * (.7f + .3f * t)
                                    scaleY = scale * (.7f + .3f * t)
                                }
                                .clip(shape).background(bg)
                                .then(if (active) Modifier.border(3.dp, if (level == 3) colors.tertiary else colors.primary, shape)
                                    else if (index == 48) Modifier.border(2.dp, colors.outline, shape) else Modifier)
                                .semantics { contentDescription = description }
                                .selectable(active, role = Role.RadioButton,
                                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                                    onClick = {
                                        if (!active) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selected = index
                                    }),
                                contentAlignment = Alignment.Center) {
                                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge, color = inks[level],
                                    maxLines = 1)
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.matrix_legend_low), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    fills.forEachIndexed { index, fill ->
                        Box(Modifier.width(22.dp).height(12.dp).clip(RoundedCornerShape(
                            topStart = if (index == 0) 6.dp else 2.dp, bottomStart = if (index == 0) 6.dp else 2.dp,
                            topEnd = if (index == fills.lastIndex) 6.dp else 2.dp,
                            bottomEnd = if (index == fills.lastIndex) 6.dp else 2.dp)).background(fill))
                    }
                }
                Text(stringResource(R.string.matrix_legend_high), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                if (streak > 1) MatrixPill(stringResource(R.string.b10_matrix_streak, streak), colors.tertiaryContainer,
                    colors.onTertiaryContainer)
            }
        }
    }
}

@Composable
private fun MatrixPill(text: String, container: Color, content: Color) {
    LeafSurface(shape = CircleShape, color = container, contentColor = content) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
