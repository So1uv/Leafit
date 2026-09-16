package com.example.fitnesstracker.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LeafIcons
import com.example.fitnesstracker.ui.components.appLocale
import com.example.fitnesstracker.ui.theme.LeafSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun LiveTelemetryGrid(session: LiveSession, readOnly: Boolean, modifier: Modifier = Modifier) {
    val locale = appLocale()
    val speed = if (readOnly) averageSpeedKmh(session.elapsedMs, session.distanceMeters)
        else if (session.phase == LivePhase.ACTIVE && session.status == GpsStatus.GOOD)
            session.currentSpeedMps?.toDouble()?.times(3.6) else null
    val values = listOf(speed?.takeIf { it.isFinite() && it >= 0 }?.let { String.format(locale, "%.1f", it) } ?: "—",
        formatPace(averagePaceSeconds(session.elapsedMs, session.distanceMeters), locale),
        formatPace(session.fastestPaceSeconds?.toDouble(), locale),
        formatPace(session.slowestPaceSeconds?.toDouble(), locale))
    val labels = listOf(stringResource(if (readOnly) R.string.live_avg_speed_short else R.string.live_current_speed_short),
        stringResource(R.string.live_avg_pace_short), stringResource(R.string.live_fast_pace_short),
        stringResource(R.string.live_slow_pace_short))
    val descriptions = listOf(stringResource(if (readOnly) R.string.live_avg_speed else R.string.live_current_speed),
        stringResource(R.string.live_avg_pace), stringResource(R.string.live_fast_pace), stringResource(R.string.live_slow_pace))
    val units = listOf(stringResource(R.string.unit_kmh), stringResource(R.string.unit_min_km),
        stringResource(R.string.unit_min_km), stringResource(R.string.unit_min_km))
    val icons = listOf(LeafIcons.Speed, LeafIcons.Timer, LeafIcons.DirectionsRun, LeafIcons.DirectionsWalk)
    val colors = MaterialTheme.colorScheme
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 280.dp && fontScale < 1.4f) 4 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..3).toList().chunked(columns).forEach { indices ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    indices.forEach { index ->
                        LeafSurface(modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(18.dp),
                            color = if (index < 2) colors.surfaceContainerHigh else colors.tertiaryContainer.copy(alpha = .55f)) {
                            Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(icons[index], descriptions[index], Modifier.size(18.dp),
                                    tint = if (index < 2) colors.primary else colors.onTertiaryContainer)
                                Text(values[index], style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(units[index], style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                Text(labels[index], style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                    color = colors.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedWorkoutTelemetry(metrics: String, durationSeconds: Long, distanceMeters: Float) {
    val session by produceState<LiveSession?>(null, metrics) {
        value = withContext(Dispatchers.Default) { LiveWorkoutStore.fromMetrics(metrics) }
    }
    LiveTelemetryGrid((session ?: LiveSession()).copy(elapsedMs = durationSeconds * 1000, distanceMeters = distanceMeters), readOnly = true)
}
