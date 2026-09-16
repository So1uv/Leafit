package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafTonalButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.PickerDates
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields

/** Scrollable calendar content with persistent actions, including on short landscape screens. */
@Composable
fun LeafDatePicker(selectedDay: Long, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val locale = appLocale()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val initial = remember(selectedDay) {
        Instant.ofEpochMilli(PickerDates.toPickerUtc(selectedDay)).atZone(ZoneOffset.UTC).toLocalDate()
    }
    var selectedEpochDay by rememberSaveable(selectedDay) { mutableStateOf(initial.toEpochDay()) }
    var monthKey by rememberSaveable(selectedDay) { mutableStateOf(YearMonth.from(initial).toString()) }
    var choosingYear by rememberSaveable { mutableStateOf(false) }
    val selected = LocalDate.ofEpochDay(selectedEpochDay)
    val month = YearMonth.parse(monthKey)
    val today = LocalDate.now()
    val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    val monthFormat = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
    val firstWeekday = WeekFields.of(locale).firstDayOfWeek
    val minYear = minOf(1900, initial.year)
    val maxYear = maxOf(2100, initial.year)
    val leading = (month.atDay(1).dayOfWeek.value - firstWeekday.value + 7) % 7
    val weekCount = (leading + month.lengthOfMonth() + 6) / 7

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxWidth().safeDrawingPadding().padding(16.dp),
            contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().heightIn(max = maxHeight),
                shape = RoundedCornerShape(32.dp), color = colors.surfaceContainerHigh,
                contentColor = colors.onSurface, tonalElevation = 0.dp, shadowElevation = 0.dp) {
                Column(Modifier.padding(12.dp)) {
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).animateContentSize(spring(.96f, 420f)),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(24.dp), color = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                LeafTitle(selected.dayOfMonth.toString(), style = MaterialTheme.typography.displaySmall, color = colors.onSecondaryContainer)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(stringResource(R.string.common_select_date), style = MaterialTheme.typography.labelMedium)
                                    Text(selected.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)), style = MaterialTheme.typography.titleMedium)
                                    Text(selected.dayOfWeek.getDisplayName(TextStyle.FULL, locale), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            LeafTonalButton(onClick = { choosingYear = !choosingYear }, modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.secondaryContainer,
                                    contentColor = colors.onSecondaryContainer)) {
                                LeafTitle(month.format(monthFormat), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), color = colors.onSecondaryContainer)
                                Icon(if (choosingYear) LeafIcons.KeyboardArrowUp else LeafIcons.ExpandMore, null, Modifier.size(20.dp))
                            }
                            if (!choosingYear) {
                                FilledTonalIconButton(enabled = month > YearMonth.of(minYear, 1),
                                    onClick = { monthKey = month.minusMonths(1).toString() }) {
                                    Icon(LeafIcons.ArrowBack, month.minusMonths(1).format(monthFormat))
                                }
                                FilledTonalIconButton(enabled = month < YearMonth.of(maxYear, 12),
                                    onClick = { monthKey = month.plusMonths(1).toString() }) {
                                    Icon(LeafIcons.ArrowForward, month.plusMonths(1).format(monthFormat))
                                }
                            }
                        }
                        AnimatedContent(targetState = choosingYear to monthKey, transitionSpec = {
                            val forward = targetState.second >= initialState.second
                            if (targetState.first != initialState.first) {
                                (fadeIn(tween(180)) + scaleIn(spring(.93f, 420f), initialScale = .96f))
                                    .togetherWith(fadeOut(tween(100)))
                            } else {
                                (slideInHorizontally(spring(.94f, 420f)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(170)))
                                    .togetherWith(slideOutHorizontally(tween(140)) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(110)))
                            }.using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> spring(.96f, 450f) }))
                        }, label = "calendarPage") { (yearMode, visibleMonth) ->
                        val month = YearMonth.parse(visibleMonth)
                        val leading = (month.atDay(1).dayOfWeek.value - firstWeekday.value + 7) % 7
                        val weekCount = (leading + month.lengthOfMonth() + 6) / 7
                        if (yearMode) {
                            val years = (minYear..maxYear).toList()
                            val grid = rememberLazyGridState(initialFirstVisibleItemIndex = (month.year - minYear - 6).coerceAtLeast(0))
                            LazyVerticalGrid(columns = GridCells.Fixed(3), state = grid,
                                modifier = Modifier.fillMaxWidth().height(288.dp).selectableGroup(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(years, key = { it }) { year ->
                                    FilterChip(selected = year == month.year,
                                        onClick = { monthKey = month.withYear(year).toString(); choosingYear = false },
                                        label = { Text(year.toString(), style = MaterialTheme.typography.titleSmall) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = colors.surfaceContainer,
                                            selectedContainerColor = colors.secondaryContainer, selectedLabelColor = colors.onSecondaryContainer),
                                        border = null, shape = RoundedCornerShape(50))
                                }
                            }
                        } else {
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                                .background(colors.surfaceContainerLow).padding(vertical = 6.dp).selectableGroup()) {
                                Row(Modifier.fillMaxWidth()) {
                                    repeat(7) { offset ->
                                        Box(Modifier.weight(1f).height(32.dp), Alignment.Center) {
                                            Text(firstWeekday.plus(offset.toLong()).getDisplayName(TextStyle.SHORT, locale),
                                                style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                        }
                                    }
                                }
                                repeat(weekCount) { week ->
                                    Row(Modifier.fillMaxWidth()) {
                                        repeat(7) { weekday ->
                                            val number = week * 7 + weekday - leading + 1
                                            if (number !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(48.dp))
                                            else {
                                                val day = month.atDay(number)
                                                val active = day == selected
                                                val description = day.format(dateFormat)
                                                val radius by animateDpAsState(if (active) 14.dp else 24.dp, spring(.85f, 500f), label = "dayShape")
                                                val fill by animateColorAsState(if (active) colors.primaryContainer else colors.surfaceContainerLow,
                                                    spring(stiffness = 550f), label = "calendarDay")
                                                Box(Modifier.weight(1f).height(48.dp)
                                                    .semantics { contentDescription = description }
                                                    .selectable(active, role = Role.RadioButton,
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            if (!active) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                                            selectedEpochDay = day.toEpochDay()
                                                        }), Alignment.Center) {
                                                    Box(Modifier.size(38.dp).background(fill, RoundedCornerShape(radius)), Alignment.Center) {
                                                        Text(number.toString(), style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
                                                            color = if (active) colors.onPrimaryContainer else colors.onSurface)
                                                        if (day == today) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp)
                                                            .size(3.dp).background(colors.primary, RoundedCornerShape(50)))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
                        Spacer(Modifier.width(8.dp))
                        LeafTonalButton(onClick = {
                            onSelect(PickerDates.fromPickerUtc(selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()))
                        }) { Text(stringResource(R.string.common_save)) }
                    }
                }
            }
        }
    }
}
