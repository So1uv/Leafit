package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafCard as Card
import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.currentDayFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityMatrix(intensities: List<Float>, modifier: Modifier = Modifier, title: String? = null) {
    val colors = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val dayStart by remember { currentDayFlow() }.collectAsState(initial = com.example.fitnesstracker.utils.DateUtils.todayStart())
    val today = Instant.ofEpochMilli(dayStart).atZone(ZoneId.systemDefault()).toLocalDate()
    val values = List(49) { intensities.getOrElse(it) { 0f }.let { v -> if (v.isFinite()) v.coerceIn(0f, 1f) else 0f } }
    var selected by rememberSaveable(dayStart) { mutableIntStateOf(48) }
    val shortDate = DateTimeFormatter.ofPattern("d MMM", appLocale())
    val fullDate = DateTimeFormatter.ofPattern("EEE, d MMMM", appLocale())
    fun level(v: Float): Int = when { v <= .001f -> R.string.matrix_no_activity; v < .34f -> R.string.matrix_low; v < .67f -> R.string.matrix_medium; else -> R.string.matrix_high }
    fun ink(v: Float): Color = if (v >= .55f) colors.onPrimary else colors.onSurface
    fun fill(v: Float): Color = if (v <= .001f) colors.surfaceContainerHighest else lerp(colors.secondaryContainer, colors.primary, v)
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(18.dp), color = colors.tertiaryContainer) {
                    Box(Modifier.size(48.dp), Alignment.Center) { Icon(LeafIcons.CalendarViewWeek, null, tint = colors.onTertiaryContainer) }
                }
                Column(Modifier.weight(1f)) {
                    if (title != null) Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.matrix_active_days, values.count { it > .001f }),
                        style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                }
            }
            Text(stringResource(R.string.matrix_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(today.minusDays(48).format(shortDate), style = MaterialTheme.typography.labelMedium)
                Text(today.format(shortDate), style = MaterialTheme.typography.labelMedium)
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val gridWidth = maxWidth.coerceAtLeast(336.dp)
                Row(Modifier.horizontalScroll(rememberScrollState()).width(gridWidth).selectableGroup()) {
                    repeat(7) { week ->
                        Column(Modifier.weight(1f)) {
                            repeat(7) { day ->
                                val index = week * 7 + day
                                val active = index == selected
                                val date = today.minusDays((48 - index).toLong())
                                val value = values[index]
                                val description = date.format(fullDate) + ": " + stringResource(level(value))
                                val bg by animateColorAsState(fill(value), spring(stiffness = 500f), label = "activityDay")
                                val corner by animateDpAsState(if (active) 16.dp else 10.dp, spring(.88f, 500f), label = "activityShape")
                                Box(Modifier.fillMaxWidth().height(48.dp)
                                    .semantics { contentDescription = description }
                                    .selectable(active, role = Role.RadioButton,
                                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                                        onClick = { if (!active) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); selected = index }),
                                    contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(corner))
                                        .background(bg), Alignment.Center) {
                                        if (active) Surface(shape = CircleShape, color = colors.surfaceContainerLowest) {
                                            Text(date.dayOfMonth.toString(), Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                                        } else if (value <= .001f) Box(Modifier.size(4.dp).clip(CircleShape).background(colors.onSurfaceVariant.copy(alpha = .45f)))
                                        else if (index == 48) Box(Modifier.size(5.dp).clip(CircleShape).background(ink(value)))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.matrix_legend_low), style = MaterialTheme.typography.labelSmall)
                listOf(0f, .2f, .5f, .8f, 1f).forEach { value -> Box(Modifier.size(14.dp).clip(RoundedCornerShape(5.dp)).background(fill(value))) }
                Text(stringResource(R.string.matrix_legend_high), style = MaterialTheme.typography.labelSmall)
            }
            Surface(shape = RoundedCornerShape(22.dp), color = colors.surfaceContainerHighest) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeafTitle(today.minusDays((48 - selected).toLong()).format(fullDate), style = MaterialTheme.typography.titleSmall)
                    LeafTitle(stringResource(level(values[selected])), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}
