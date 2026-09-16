package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

enum class LeafWeekMetric { STEPS, MOVEMENT, SLEEP }

@Composable
fun LeafWeekHistory(metric: LeafWeekMetric, currentValues: List<Float>, unit: String,
                    accent: Color, decimals: Int = 0) {
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val firstMonday = monday.minusWeeks(12)
    val since = firstMonday.atStartOfDay(zone).toInstant().toEpochMilli()
    val historyFlow = remember(db, metric, since, zone) {
        fun day(time: Long) = Instant.ofEpochMilli(time).atZone(zone).toLocalDate()
        when (metric) {
            LeafWeekMetric.STEPS -> db.stepDao().getSince(since).map { rows ->
                rows.associate { day(it.dayStart) to it.steps.toFloat() }
            }
            LeafWeekMetric.MOVEMENT -> db.workoutDao().workoutsThisWeek(since).map { rows ->
                rows.groupBy { day(it.startTime) }.mapValues { (_, records) -> records.sumOf { it.durationSeconds }.toFloat() / 60f }
            }
            LeafWeekMetric.SLEEP -> db.sleepDao().getAll().map { rows ->
                rows.filter { it.bedTime >= since }.groupBy { day(it.bedTime) }.mapValues { (_, records) ->
                    records.sumOf { (it.wakeTime - it.bedTime).coerceAtLeast(0L) }.toFloat() / 3_600_000f
                }
            }
        }
    }
    val history by historyFlow.collectAsState(initial = emptyMap())
    val pager = rememberPagerState(initialPage = 12, pageCount = { 13 })
    val scope = rememberCoroutineScope()
    val format = DateTimeFormatter.ofPattern("d MMM", appLocale())
    val visibleMonday = firstMonday.plusWeeks(pager.currentPage.toLong())
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = pager.currentPage > 0, onClick = {
                    scope.launch { pager.animateScrollToPage(pager.currentPage - 1, animationSpec = spring(.95f, 360f)) }
                }) { Icon(LeafIcons.ChevronRight, stringResource(R.string.week_previous), Modifier.graphicsLayer { rotationZ = 180f }) }
                LeafTitle("${visibleMonday.format(format)} — ${visibleMonday.plusDays(6).format(format)}",
                    Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                IconButton(enabled = pager.currentPage < 12, onClick = {
                    scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = spring(.95f, 360f)) }
                }) { Icon(LeafIcons.ChevronRight, stringResource(R.string.week_next)) }
            }
        }
        HorizontalPager(state = pager, key = { it }, pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth().animateContentSize(spring(.95f, 400f))) { page ->
            val start = firstMonday.plusWeeks(page.toLong())
            val values = if (page == 12) currentValues else List(7) { history[start.plusDays(it.toLong())] ?: 0f }
            ExpressiveWeekChart(values, unit, accent, decimals = decimals, weekStart = start)
        }
    }
}
