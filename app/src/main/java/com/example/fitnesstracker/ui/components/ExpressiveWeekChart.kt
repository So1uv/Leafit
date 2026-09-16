package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow

fun weekValue(value: Float, decimals: Int = 0): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = decimals
    minimumFractionDigits = 0
}.format(value.toDouble())

@Composable
fun CurrentWeekCaption() {
    val monday = LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())
    val format = DateTimeFormatter.ofPattern("d MMM", appLocale())
    Text("${monday.format(format)} — ${monday.plusDays(6).format(format)}",
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun ExpressiveWeekChart(values: List<Float>, unit: String, accent: Color,
                        modifier: Modifier = Modifier, decimals: Int = 0,
                        weekStart: LocalDate = LocalDate.now().minusDays((LocalDate.now().dayOfWeek.value - 1).toLong())) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val safe = List(7) { values.getOrElse(it) { 0f }.let { v -> if (v.isFinite()) v.coerceAtLeast(0f) else 0f } }
    val todayIndex = java.time.temporal.ChronoUnit.DAYS.between(weekStart, LocalDate.now()).toInt()
    var selected by rememberSaveable(weekStart.toString()) { mutableIntStateOf(if (todayIndex in 0..6) todayIndex else 0) }
    val labels = stringResource(R.string.home_weekdays).split(",")
    val monday = weekStart
    val numberFormat = NumberFormat.getNumberInstance(appLocale()).apply { maximumFractionDigits = decimals }
    fun reading(value: Float) = numberFormat.format(value.toDouble())
    val dateFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM", appLocale())
    val maxValue = safe.maxOrNull() ?: 0f
    val magnitude = 10.0.pow(kotlin.math.floor(log10(maxValue.coerceAtLeast(1f).toDouble()))).toFloat()
    val ceiling = (ceil(maxValue / magnitude) * magnitude).coerceAtLeast(1f)
    val selectedDate = monday.plusDays(selected.toLong()).format(dateFormat)
    val fontScale = LocalDensity.current.fontScale
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = colors.surfaceContainerLow, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LeafTitle(selectedDate, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LeafTitle(reading(safe[selected]), style = MaterialTheme.typography.headlineLarge,
                        color = accent)
                    Text(unit, style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
        }
        Text(stringResource(R.string.chart_scale, reading(ceiling), unit),
            style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Keep labels and touch targets usable with a narrow viewport or enlarged system text.
            val plotWidth = maxOf(maxWidth, (44.dp + 8.dp * (fontScale - 1f).coerceAtLeast(0f)) * 7)
            val gridColor = colors.onSurfaceVariant.copy(alpha = .12f)
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Row(Modifier.width(plotWidth).selectableGroup().drawBehind {
                    val plotHeight = 148.dp.toPx()
                    listOf(0f, .5f, 1f).forEach { fraction ->
                        val y = plotHeight * fraction
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    }
                }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    safe.forEachIndexed { index, value ->
                        val active = selected == index
                        val fill by animateFloatAsState(value / ceiling,
                            spring(dampingRatio = 1f, stiffness = 220f), label = "weekBar$index")
                        val barColor by animateColorAsState(if (active) accent else androidx.compose.ui.graphics.lerp(colors.surfaceContainerHighest, accent, .23f),
                            spring(stiffness = 500f), label = "weekBarTone$index")
                        val interactions = remember { MutableInteractionSource() }
                        val description = "${monday.plusDays(index.toLong()).format(dateFormat)}: ${reading(value)} $unit"
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                            .semantics { contentDescription = description }
                            .selectable(active, role = Role.Tab, interactionSource = interactions,
                                indication = null, onClick = {
                                    if (!active) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    selected = index
                                })
                            .padding(horizontal = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(148.dp), contentAlignment = Alignment.BottomCenter) {
                                if (value > 0f) Box(Modifier.fillMaxWidth().fillMaxHeight(fill.coerceIn(0f, 1f))
                                    .clip(CircleShape)
                                    .background(barColor))
                                else Box(Modifier.padding(bottom = 2.dp).size(width = 12.dp, height = 3.dp)
                                    .background(colors.outline, CircleShape))
                            }
                            Spacer(Modifier.height(8.dp))
                            Surface(shape = CircleShape, color = if (active) colors.secondaryContainer else Color.Transparent) {
                                Text(labels.getOrElse(index) { "—" }, Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (active) colors.onSecondaryContainer else colors.onSurfaceVariant)
                            }
                            Box(Modifier.padding(top = 4.dp, bottom = 6.dp).size(4.dp)
                                .background(if (index == todayIndex) accent else Color.Transparent, CircleShape))
                        }
                    }
                }
            }
        }

    }
}
