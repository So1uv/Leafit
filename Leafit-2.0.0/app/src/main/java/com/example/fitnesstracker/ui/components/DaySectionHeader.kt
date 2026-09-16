package com.example.fitnesstracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DaySectionHeader(todayTitle: String, dayTitle: String, selectedDay: Long,
    onChooseDate: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false,
    headerState: LeafHeaderState? = null) {
    val colors = MaterialTheme.colorScheme
    val today = selectedDay == DateUtils.todayStart()
    val date = Instant.ofEpochMilli(selectedDay).atZone(ZoneId.systemDefault()).toLocalDate()
    val label = date.format(DateTimeFormatter.ofPattern(
        if (date.year == java.time.LocalDate.now().year) "d MMM" else "dd.MM.yy", appLocale()))
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LeafTitle(if (today) todayTitle else dayTitle,
            modifier = Modifier.weight(1f).leafHeaderMotion(headerState),
            style = MaterialTheme.typography.headlineSmall, maxLines = 2)
        LeafSurface(onClick = onChooseDate, shape = CircleShape, color = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer, modifier = Modifier.heightIn(min = 48.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(LeafIcons.CalendarMonth, stringResource(R.string.common_select_date), Modifier.size(24.dp))
                AnimatedVisibility(!today, enter = fadeIn() + expandHorizontally(spring(.9f, 500f)),
                    exit = fadeOut() + shrinkHorizontally(spring(1f, 550f))) {
                    Text(label, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.labelLarge,
                        maxLines = 1, color = colors.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
fun DayPageTopBar(todayTitle: String, dayTitle: String, selectedDay: Long, onChooseDate: () -> Unit,
    headerState: LeafHeaderState? = null) {
    Box(Modifier.fillMaxWidth().leafHeaderScrim(MaterialTheme.colorScheme.surface, headerState)
        .statusBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp)) {
        DaySectionHeader(todayTitle, dayTitle, selectedDay, onChooseDate,
            modifier = Modifier.height(appHeaderHeight()), compact = true, headerState = headerState)
    }
}
