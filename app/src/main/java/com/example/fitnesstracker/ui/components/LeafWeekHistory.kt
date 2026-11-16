package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.currentDayFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class LeafWeekMetric { STEPS, MOVEMENT, SLEEP }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeafWeekHistory(metric: LeafWeekMetric, currentValues: List<Float>, unit: String, accent: Color, decimals: Int = 0) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val day by currentDayFlow().collectAsLeafState(DateUtils.todayStart())
    val zone = remember { ZoneId.systemDefault() }
    val today = remember(day) { Instant.ofEpochMilli(day).atZone(zone).toLocalDate() }
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val firstMonday = monday.minusWeeks(2600)
    val pager = rememberPagerState(initialPage = 2600, pageCount = { 2601 })
    val scope = rememberCoroutineScope()
    val format = DateTimeFormatter.ofPattern("d MMM yyyy", appLocale())
    val visibleMonday = firstMonday.plusWeeks(pager.currentPage.toLong())
    val since = visibleMonday.minusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = visibleMonday.plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val historyFlow = remember(db, metric, since, end) {
        fun date(time: Long) = Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
        when(metric) {
            LeafWeekMetric.STEPS -> db.stepDao().getBetween(since, end).map { rows -> rows.associate { date(it.dayStart) to it.steps.toFloat() } }
            LeafWeekMetric.MOVEMENT -> db.workoutDao().getForDay(since, end).map { rows -> rows.groupBy { date(it.startTime) }
                .mapValues { (_, rows) -> rows.sumOf { it.durationSeconds }.toFloat() / 60f } }
            LeafWeekMetric.SLEEP -> db.sleepDao().getBetween(since, end).map { rows -> rows.groupBy { date(it.wakeTime) }
                .mapValues { (_, rows) -> rows.sumOf { (it.wakeTime - it.bedTime).coerceAtLeast(0) }.toFloat() / 3600000f } }
        }
    }
    val queryKey = Triple(metric, since, end)
    val loaded by produceState(queryKey to emptyMap<LocalDate, Float>(), historyFlow) {
        value = queryKey to emptyMap()
        historyFlow.collect { value = queryKey to it }
    }
    val history = if (loaded.first == queryKey) loaded.second else emptyMap()
    run {
        val values = if (pager.currentPage == 2600 && metric != LeafWeekMetric.SLEEP) List(7) { currentValues.getOrElse(it) { 0f } }
            else List(7) { history[visibleMonday.plusDays(it.toLong())] ?: 0f }
        val previous = List(7) { history[visibleMonday.minusWeeks(1).plusDays(it.toLong())] ?: 0f }
        var choosing by remember { mutableStateOf(false) }
        var details by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(enabled = pager.currentPage > 0, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1, animationSpec = spring(.95f, 360f)) } }) {
                        Icon(LeafIcons.ChevronRight, stringResource(R.string.week_previous), Modifier.graphicsLayer { rotationZ = 180f })
                    }
                    TextButton(onClick = { choosing = true }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(4.dp)) {
                        Text("${visibleMonday.format(format)} — ${visibleMonday.plusDays(6).format(format)}", style = MaterialTheme.typography.labelMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    IconButton(enabled = pager.currentPage < 2600, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = spring(.95f, 360f)) } }) {
                        Icon(LeafIcons.ChevronRight, stringResource(R.string.week_next))
                    }
                }
            }
            HorizontalPager(state = pager, key = { it }, pageSpacing = 16.dp, modifier = Modifier.fillMaxWidth()) { page ->
                val start = firstMonday.plusWeeks(page.toLong())
                val pageValues = if (page == pager.currentPage) values else List(7) { history[start.plusDays(it.toLong())] ?: 0f }
                ExpressiveWeekChart(pageValues, unit, accent, decimals = decimals, weekStart = start)
            }
            val observedDays = values.count { it > 0 }.coerceAtLeast(1)
            val total = if (metric == LeafWeekMetric.SLEEP) values.sum() / observedDays else values.sum()
            val previousDays = previous.count { it > 0 }.coerceAtLeast(1)
            val old = if (metric == LeafWeekMetric.SLEEP) previous.sum() / previousDays else previous.sum()
            val shownDays = if (visibleMonday == monday) today.dayOfWeek.value else 7
            val comparable = if (metric == LeafWeekMetric.SLEEP) null else previous.take(shownDays).sum().takeIf { it > 0 }
            Text(stringResource(if (metric == LeafWeekMetric.SLEEP) R.string.b5_week_average else R.string.b5_week_total,
                weekValue(total, decimals), unit), style = MaterialTheme.typography.labelLarge)
            if (comparable != null) Text(stringResource(R.string.b5_week_compare,
                (if (total >= comparable) "+" else "") + weekValue((total / comparable - 1f) * 100f, 0)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else if (metric == LeafWeekMetric.SLEEP && old > 0 && values.any { it > 0 }) Text(stringResource(R.string.b5_week_sleep_compare,
                (if (total >= old) "+" else "") + weekValue(total - old, 1)), style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { details = true }) { Text(stringResource(R.string.b5_week_records)) }
                if (pager.currentPage != 2600) TextButton(onClick = { scope.launch { pager.scrollToPage(2600) } }) { Text(stringResource(R.string.common_today)) }
            }
        }
        if (choosing) LeafDatePicker(visibleMonday.atStartOfDay(zone).toInstant().toEpochMilli(), { timestamp ->
            val selected = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
            val selectedMonday = selected.minusDays((selected.dayOfWeek.value - 1).toLong())
            val page = ChronoUnit.WEEKS.between(firstMonday, selectedMonday).toInt().coerceIn(0, 2600)
            choosing = false; scope.launch { pager.scrollToPage(page) }
        }, { choosing = false })
        if (details) WeekRecordsSheet(visibleMonday) { details = false }
    }
}
