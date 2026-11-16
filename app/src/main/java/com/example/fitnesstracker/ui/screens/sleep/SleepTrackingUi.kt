package com.example.fitnesstracker.ui.screens.sleep

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.sleep.SleepAlarm
import com.example.fitnesstracker.sleep.SleepAlarms
import com.example.fitnesstracker.sleep.SleepEvent
import com.example.fitnesstracker.sleep.SleepNight
import com.example.fitnesstracker.sleep.SleepNightStore
import com.example.fitnesstracker.sleep.SleepTrackPrefs
import com.example.fitnesstracker.sleep.SleepTrackSettings
import com.example.fitnesstracker.sleep.SleepTracker
import com.example.fitnesstracker.ui.components.ExpressiveChoiceRow
import com.example.fitnesstracker.ui.components.ExpressiveTimeInput
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafCardHeader
import com.example.fitnesstracker.ui.components.LeafEditSheet
import com.example.fitnesstracker.ui.components.LeafFilledTonalIconButton
import com.example.fitnesstracker.ui.components.LeafFitText
import com.example.fitnesstracker.ui.components.LeafIconBadge
import com.example.fitnesstracker.ui.components.LeafIconSize
import com.example.fitnesstracker.ui.components.LeafIconButton
import com.example.fitnesstracker.ui.components.LeafIcons
import com.example.fitnesstracker.ui.components.LeafRosette
import com.example.fitnesstracker.ui.components.LeafSwitch
import com.example.fitnesstracker.ui.components.LeafTextButton
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.LeafTone
import com.example.fitnesstracker.ui.components.filled
import com.example.fitnesstracker.ui.components.groupedLeafShape
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

private fun clock(minutes: Int): String = (minutes / 60).toString().padStart(2, '0') + ":" + (minutes % 60).toString().padStart(2, '0')

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SleepTrackerCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val live by SleepTracker.state.collectAsState()
    var settings by remember { mutableStateOf(SleepTrackPrefs.load(context)) }
    var showSettings by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { SleepTracker.recover(context) }
    fun update(next: SleepTrackSettings) { settings = next; SleepTrackPrefs.save(context, next) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { SleepTracker.start(context) }
    fun begin() {
        val missing = buildList {
            if (settings.sound && ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (missing.isEmpty()) SleepTracker.start(context) else launcher.launch(missing.toTypedArray())
    }
    if (showSettings) SleepTrackSettingsSheet(settings, ::update) { showSettings = false }

    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp),
        color = if (live.active) colors.tertiaryContainer else colors.surfaceContainer,
        contentColor = if (live.active) colors.onTertiaryContainer else colors.onSurface) {
        AnimatedContent(when { live.ringing -> 2; live.active -> 1; else -> 0 },
            Modifier.animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) }, label = "sleepTracker") { mode ->
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (mode) {
                    2 -> {
                        val pulse by rememberInfiniteTransition(label = "alarmPulse").animateFloat(.92f, 1.08f,
                            infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "alarmScale")
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LeafIconBadge(LeafIcons.Alarm, Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse },
                                size = LeafIconSize.Hero, tone = LeafTone.Solid, shape = LeafRosette(8))
                            Text(live.label.ifBlank { stringResource(R.string.b11_sleep_alarm) }, style = MaterialTheme.typography.headlineSmall)
                            Text(DateUtils.formatTime(System.currentTimeMillis()), style = MaterialTheme.typography.displayMedium)
                        }
                        if (live.math) {
                            var round by remember { mutableIntStateOf(0) }
                            val task = remember(round) {
                                val a = (12..48).random()
                                val b = (7..29).random()
                                Triple(a, b, listOf(a + b, a + b + listOf(-10, 10, -2, 2).random(), a + b + listOf(-1, 1, 5, -5).random())
                                    .distinct().shuffled())
                            }
                            Text(stringResource(R.string.b12_sleep_math_task, task.first, task.second), Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                task.third.forEach { answer ->
                                    LeafTonalButton(onClick = {
                                        if (answer == task.first + task.second) SleepTracker.send(context, SleepTracker.ACTION_STOP) else round++
                                    }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                        Text(answer.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (live.canSnooze) LeafTonalButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_SNOOZE) },
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Icon(LeafIcons.Snooze, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                LeafFitText(stringResource(R.string.b11_sleep_snooze, settings.snooze))
                            }
                            if (!live.math) LeafButton(onClick = {
                                SleepTracker.send(context, if (live.active) SleepTracker.ACTION_STOP else SleepTracker.ACTION_DISMISS)
                            }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Icon(LeafIcons.WbSunny, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                LeafFitText(stringResource(if (live.active) R.string.b11_sleep_wake else R.string.b13_alarm_dismiss))
                            }
                        }
                        if (live.active && !live.math) LeafTextButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_DISMISS) },
                            modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.b13_alarm_dismiss)) }
                    }
                    1 -> {
                        val now by produceState(System.currentTimeMillis(), live.start) {
                            while (true) { value = System.currentTimeMillis(); delay(1000) }
                        }
                        val levels = remember { mutableStateListOf<Float>() }
                        LaunchedEffect(live.start) {
                            while (true) {
                                levels.add(SleepTracker.state.value.level)
                                if (levels.size > 40) levels.removeAt(0)
                                delay(1000)
                            }
                        }
                        LeafCardHeader(LeafIcons.MoonStars, stringResource(R.string.b11_sleep_tracking_on),
                            subtitle = stringResource(R.string.b11_sleep_since, DateUtils.formatTime(live.start)), tone = LeafTone.Solid)
                        LeafFitText(DateUtils.formatDuration(((now - live.start) / 1000L).coerceAtLeast(0L)),
                            Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
                        if (live.sound) {
                            val bar = colors.tertiary
                            Canvas(Modifier.fillMaxWidth().height(56.dp)) {
                                val slot = size.width / 40f
                                val width = slot * .56f
                                levels.forEachIndexed { index, level ->
                                    val height = (size.height * (.08f + .92f * level)).coerceAtLeast(width)
                                    val x = (40 - levels.size + index) * slot + (slot - width) / 2f
                                    drawRoundRect(bar.copy(alpha = .35f + .65f * level), Offset(x, (size.height - height) / 2f),
                                        Size(width, height), CornerRadius(width / 2f))
                                }
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (live.sound) TrackPill(LeafIcons.GraphicEq, stringResource(R.string.b11_sleep_events, live.events))
                            if (live.sound) TrackPill(LeafIcons.Hearing, stringResource(R.string.b11_sleep_snores, live.snores))
                            if (live.lullaby) TrackPill(LeafIcons.Waves, stringResource(R.string.b13_sleep_lullaby_on))
                            if (live.alarmAt > 0L) TrackPill(LeafIcons.Alarm, DateUtils.formatTime(live.alarmAt))
                            if (!live.sound && settings.sound) TrackPill(LeafIcons.Mic, stringResource(R.string.b11_sleep_no_mic))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            LeafFilledTonalIconButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_DISCARD) },
                                modifier = Modifier.size(56.dp)) {
                                Icon(LeafIcons.DeleteOutline, stringResource(R.string.b11_sleep_discard))
                            }
                            LeafButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_STOP) },
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                                Icon(LeafIcons.WbSunny, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                                LeafFitText(stringResource(R.string.b12_sleep_finish_short))
                            }
                        }
                    }
                    else -> {
                        LeafCardHeader(LeafIcons.MoonStars, stringResource(R.string.b11_sleep_tracker),
                            subtitle = stringResource(R.string.b11_sleep_tracker_hint), tone = LeafTone.Tertiary) {
                            LeafFilledTonalIconButton(onClick = { showSettings = true }) {
                                Icon(LeafIcons.Tune, stringResource(R.string.b11_sleep_settings))
                            }
                        }
                        val revision by SleepAlarms.revision.collectAsState()
                        val upcoming = remember(revision) { SleepAlarms.upcoming(context) }
                        if (upcoming != null || settings.lullaby > 0) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (upcoming != null) TrackPill(LeafIcons.Alarm, DateUtils.formatTime(upcoming.second))
                            if (settings.lullaby > 0) TrackPill(LeafIcons.Waves, stringResource(R.string.b13_sleep_lullaby))
                        }
                        LeafButton(onClick = { begin() }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Icon(LeafIcons.Bedtime, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.b11_sleep_start))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackPill(icon: ImageVector, text: String) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = CircleShape, color = colors.surface.copy(alpha = .6f), contentColor = colors.onSurface) {
        Row(Modifier.padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(LeafIcons.filled(icon), null, Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
private fun SleepTrackSettingsSheet(settings: SleepTrackSettings, onChange: (SleepTrackSettings) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val ringtone = remember(settings.alarmUri) {
        runCatching {
            val uri = settings.alarmUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            RingtoneManager.getRingtone(context, uri)?.getTitle(context)
        }.getOrNull()
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val uri = androidx.core.content.IntentCompat.getParcelableExtra(data, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            onChange(settings.copy(alarmUri = uri?.toString().orEmpty()))
        }
    }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.b11_sleep_settings)) },
        confirmButton = { LeafButton(onClick = onDismiss) { Text(stringResource(R.string.b7_info_ok)) } }) {
        ExpressiveChoiceRow(listOf(stringResource(R.string.b11_sleep_sound), stringResource(R.string.b11_sleep_alarm),
            stringResource(R.string.b12_sleep_tab_tracking)), listOf(LeafIcons.GraphicEq, LeafIcons.Alarm, LeafIcons.Tune), tab, { tab = it })
        when (tab) {
            0 -> {
                TrackGroup {
                    TrackSwitch(LeafIcons.Mic, stringResource(R.string.b11_sleep_sound), stringResource(R.string.b11_sleep_sound_hint),
                        settings.sound, 0, if (settings.sound) 3 else 1) { onChange(settings.copy(sound = it)) }
                    if (settings.sound) {
                        TrackSwitch(LeafIcons.GraphicEq, stringResource(R.string.b11_sleep_recordings),
                            stringResource(R.string.b11_sleep_recordings_hint), settings.recordings, 1, 3) { onChange(settings.copy(recordings = it)) }
                        TrackSwitch(LeafIcons.MobileVibrate, stringResource(R.string.b11_sleep_antisnore),
                            stringResource(R.string.b11_sleep_antisnore_hint), settings.antiSnore, 2, 3) { onChange(settings.copy(antiSnore = it)) }
                    }
                }
                if (settings.sound) {
                    TrackChoice(stringResource(R.string.b11_sleep_sensitivity), listOf(stringResource(R.string.b11_sleep_low),
                        stringResource(R.string.b11_sleep_medium), stringResource(R.string.b11_sleep_high)), settings.sensitivity) {
                        onChange(settings.copy(sensitivity = it))
                    }
                    TrackNumbers(stringResource(R.string.b12_sleep_start_delay), listOf(0, 10, 20, 30), settings.startDelay) {
                        onChange(settings.copy(startDelay = it))
                    }
                    if (settings.recordings) {
                        TrackChoice(stringResource(R.string.b12_sleep_record_filter), listOf(stringResource(R.string.b12_sleep_filter_all),
                            stringResource(R.string.b12_sleep_filter_voice)), settings.recordFilter) { onChange(settings.copy(recordFilter = it)) }
                        TrackNumbers(stringResource(R.string.b11_sleep_max_clips), listOf(5, 10, 20, 40), settings.maxClips) {
                            onChange(settings.copy(maxClips = it))
                        }
                        TrackNumbers(stringResource(R.string.b12_sleep_clip_length), listOf(10, 20, 30, 60), settings.clipSeconds) {
                            onChange(settings.copy(clipSeconds = it))
                        }
                    }
                }
            }
            1 -> {
                run {
                    LeafTonalButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_TEST) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(LeafIcons.PlayArrow, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.b13_alarm_test))
                    }
                    TrackNumbers(stringResource(R.string.b11_sleep_smart), listOf(0, 15, 30, 45), settings.smartWindow) {
                        onChange(settings.copy(smartWindow = it))
                    }
                    LeafSurface(onClick = {
                        runCatching {
                            picker.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    settings.alarmUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }))
                        }
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.surfaceContainer) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LeafIconBadge(LeafIcons.VolumeUp, size = LeafIconSize.Small, tone = LeafTone.Tertiary)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.b12_sleep_ringtone), style = MaterialTheme.typography.titleSmall)
                                Text(ringtone ?: "—", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(LeafIcons.ChevronRight, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
                        }
                    }
                    TrackNumbers(stringResource(R.string.b12_sleep_volume), listOf(40, 70, 100), settings.alarmVolume) {
                        onChange(settings.copy(alarmVolume = it))
                    }
                    TrackGroup {
                        TrackSwitch(LeafIcons.VolumeUp, stringResource(R.string.b11_sleep_gradual), stringResource(R.string.b11_sleep_gradual_hint),
                            settings.gradual, 0, 4) { onChange(settings.copy(gradual = it)) }
                        TrackSwitch(LeafIcons.MobileVibrate, stringResource(R.string.b11_sleep_vibrate), null,
                            settings.vibrate, 1, 4) { onChange(settings.copy(vibrate = it)) }
                        TrackSwitch(LeafIcons.MobileVibrate, stringResource(R.string.b12_sleep_strong_vibration), null,
                            settings.strongVibration, 2, 4) { onChange(settings.copy(strongVibration = it)) }
                        TrackSwitch(LeafIcons.QuestionMark, stringResource(R.string.b12_sleep_math), stringResource(R.string.b12_sleep_math_hint),
                            settings.mathDismiss, 3, 4) { onChange(settings.copy(mathDismiss = it)) }
                    }
                    if (settings.gradual) TrackNumbers(stringResource(R.string.b12_sleep_ramp), listOf(30, 60, 180), settings.rampSeconds) {
                        onChange(settings.copy(rampSeconds = it))
                    }
                    TrackNumbers(stringResource(R.string.b11_sleep_snooze_length), listOf(5, 10, 15), settings.snooze) {
                        onChange(settings.copy(snooze = it))
                    }
                    TrackNumbers(stringResource(R.string.b12_sleep_snooze_limit), listOf(0, 1, 3, 5), settings.snoozeLimit, zero = "∞") {
                        onChange(settings.copy(snoozeLimit = it))
                    }
                }
            }
            else -> {
                TrackNumbers(stringResource(R.string.b11_sleep_max_hours), listOf(10, 12, 14), settings.maxHours) {
                    onChange(settings.copy(maxHours = it))
                }
                TrackNumbers(stringResource(R.string.b12_sleep_keep_nights), listOf(14, 30, 90), settings.keepNights) {
                    onChange(settings.copy(keepNights = it))
                }
                TrackChoice(stringResource(R.string.b13_sleep_lullaby), listOf(stringResource(R.string.b13_lullaby_off),
                    stringResource(R.string.b13_lullaby_white), stringResource(R.string.b13_lullaby_pink),
                    stringResource(R.string.b13_lullaby_brown)), settings.lullaby) { onChange(settings.copy(lullaby = it)) }
                if (settings.lullaby > 0) {
                    TrackNumbers(stringResource(R.string.b13_lullaby_minutes), listOf(15, 30, 45, 60), settings.lullabyMinutes) {
                        onChange(settings.copy(lullabyMinutes = it))
                    }
                    TrackNumbers(stringResource(R.string.b13_lullaby_volume), listOf(20, 40, 70), settings.lullabyVolume) {
                        onChange(settings.copy(lullabyVolume = it))
                    }
                }
                LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(LeafIcons.filled(LeafIcons.Shield), null, Modifier.size(22.dp))
                            Text(stringResource(R.string.b11_sleep_battery_hint), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        }
                        LeafTonalButton(onClick = {
                            runCatching { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.b11_sleep_battery)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
}

@Composable
private fun TrackSwitch(icon: ImageVector, title: String, subtitle: String?, checked: Boolean, index: Int, count: Int,
    onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(onClick = { onChange(!checked) }, modifier = Modifier.fillMaxWidth(), shape = groupedLeafShape(index, count),
        color = colors.surfaceContainer) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(icon, size = LeafIconSize.Small, tone = if (checked) LeafTone.Primary else LeafTone.Neutral)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            LeafSwitch(checked, onChange)
        }
    }
}

@Composable
private fun TrackChoice(title: String, labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ExpressiveChoiceRow(labels, emptyList(), selected, onSelect)
    }
}

@Composable
private fun TrackNumbers(title: String, options: List<Int>, value: Int, zero: String = "—", onSelect: (Int) -> Unit) {
    TrackChoice(title, options.map { if (it == 0) zero else it.toString() }, options.indexOf(value).coerceAtLeast(0)) { onSelect(options[it]) }
}

@Composable
internal fun SleepAlarmsCard() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val revision by SleepAlarms.revision.collectAsState()
    val alarms = remember(revision) { SleepAlarms.list(context) }
    val upcoming = remember(revision) { SleepAlarms.upcoming(context) }
    var editing by remember { mutableStateOf<SleepAlarm?>(null) }
    var exact by remember { mutableStateOf(SleepAlarms.canScheduleExact(context)) }
    val locale = com.example.fitnesstracker.ui.components.appLocale()
    val dayNames = remember(locale) {
        List(7) { java.time.DayOfWeek.of(it + 1).getDisplayName(java.time.format.TextStyle.SHORT, locale) }
    }
    editing?.let { alarm ->
        SleepAlarmSheet(alarm, dayNames, onDismiss = { editing = null },
            onDelete = { SleepAlarms.remove(context, alarm.id); editing = null },
            onSave = { SleepAlarms.upsert(context, it); exact = SleepAlarms.canScheduleExact(context); editing = null })
    }
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LeafCardHeader(LeafIcons.Alarm, stringResource(R.string.b13_alarms), subtitle = upcoming?.let {
                val minutes = ((it.second - System.currentTimeMillis()) / 60000L).coerceAtLeast(0L)
                stringResource(R.string.b13_alarm_next, (minutes / 60).toInt(), (minutes % 60).toInt())
            } ?: stringResource(R.string.b13_alarms_empty), tone = LeafTone.Tertiary) {
                LeafFilledTonalIconButton(onClick = { editing = SleepAlarm(0, 7 * 60) }) {
                    Icon(LeafIcons.Add, stringResource(R.string.b13_alarm_add))
                }
            }
            if (!exact && alarms.any { it.enabled }) LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                color = colors.errorContainer, contentColor = colors.onErrorContainer) {
                Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.b13_alarm_exact), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    LeafTextButton(onClick = {
                        runCatching {
                            context.startActivity(Intent(if (Build.VERSION.SDK_INT >= 31) Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                else Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + context.packageName)))
                        }
                    }) { Text(stringResource(R.string.b13_alarm_exact_action)) }
                }
            }
            if (alarms.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                alarms.forEachIndexed { index, alarm ->
                    LeafSurface(onClick = { editing = alarm }, modifier = Modifier.fillMaxWidth(), shape = groupedLeafShape(index, alarms.size),
                        color = if (alarm.enabled) colors.tertiaryContainer else colors.surfaceContainerHigh,
                        contentColor = if (alarm.enabled) colors.onTertiaryContainer else colors.onSurfaceVariant) {
                        Row(Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(clock(alarm.minutes), style = MaterialTheme.typography.displaySmall)
                                Text(listOfNotNull(alarm.label.takeIf { it.isNotBlank() }, alarmDays(alarm.days, dayNames)).joinToString(" · "),
                                    style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (alarm.smart) Icon(LeafIcons.filled(LeafIcons.MoonStars), stringResource(R.string.b13_alarm_smart), Modifier.size(20.dp))
                            LeafSwitch(alarm.enabled, { SleepAlarms.upsert(context, alarm.copy(enabled = it)); exact = SleepAlarms.canScheduleExact(context) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun alarmDays(days: Int, names: List<String>): String = when (days) {
    0 -> stringResource(R.string.b13_alarm_once)
    127 -> stringResource(R.string.b13_alarm_daily)
    else -> names.filterIndexed { index, _ -> days and (1 shl index) != 0 }.joinToString(" ")
}

@Composable
private fun SleepAlarmSheet(initial: SleepAlarm, dayNames: List<String>, onDismiss: () -> Unit, onDelete: () -> Unit,
    onSave: (SleepAlarm) -> Unit) {
    val colors = MaterialTheme.colorScheme
    var alarm by remember { mutableStateOf(initial.copy(enabled = true)) }
    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.b13_alarm_edit)) },
        confirmButton = {
            LeafButton(onClick = { onSave(alarm) }) {
                Icon(LeafIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            if (initial.id > 0) LeafTextButton(onClick = onDelete) { Text(stringResource(R.string.common_delete), color = colors.error) }
        }) {
        ExpressiveTimeInput(stringResource(R.string.b13_alarm_when), LeafIcons.Alarm, alarm.minutes,
            onChange = { alarm = alarm.copy(minutes = it) })
        val rings = remember(alarm.minutes, alarm.days) { alarm.next(System.currentTimeMillis()) }
        val wait = ((rings - System.currentTimeMillis()) / 60000L).coerceAtLeast(0L)
        LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(LeafIcons.filled(LeafIcons.Schedule), null, Modifier.size(20.dp))
                Text(stringResource(R.string.b13_alarm_rings, DateUtils.formatDate(rings), DateUtils.formatTime(rings),
                    (wait / 60).toInt(), (wait % 60).toInt()), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(stringResource(R.string.b13_alarm_repeat) + " · " + alarmDays(alarm.days, dayNames), style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dayNames.forEachIndexed { index, name ->
                val active = alarm.days and (1 shl index) != 0
                LeafSurface(onClick = { alarm = alarm.copy(days = alarm.days xor (1 shl index)) },
                    modifier = Modifier.weight(1f).height(52.dp), shape = if (active) CircleShape else RoundedCornerShape(14.dp),
                    color = if (active) colors.primary else colors.surfaceContainerHigh,
                    contentColor = if (active) colors.onPrimary else colors.onSurfaceVariant) {
                    Box(contentAlignment = Alignment.Center) {
                        LeafFitText(name.take(2), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        com.example.fitnesstracker.ui.components.AppTextField(alarm.label, { alarm = alarm.copy(label = it.take(40)) },
            stringResource(R.string.b13_alarm_label), Modifier.fillMaxWidth())
        TrackGroup {
            TrackSwitch(LeafIcons.MoonStars, stringResource(R.string.b13_alarm_smart), stringResource(R.string.b13_alarm_smart_hint),
                alarm.smart, 0, 1) { alarm = alarm.copy(smart = it) }
        }
    }
}

@Composable
internal fun SleepNightReport(note: String) {
    val start = SleepNightStore.startOf(note) ?: return
    val context = LocalContext.current
    val night by produceState<SleepNight?>(null, start) {
        value = withContext(Dispatchers.IO) { SleepNightStore.load(context, start) }
    }
    val report = night ?: return
    NightReportBody(report)
}

@Composable
private fun NightReportBody(report: SleepNight) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val clips = remember(report) { report.events.filter { it.clip != null }.sortedBy { it.at } }
    var playing by remember { mutableStateOf<String?>(null) }
    var current by remember(report.start) { mutableIntStateOf(0) }
    var progress by remember { mutableFloatStateOf(0f) }
    var chain by rememberSaveable { mutableStateOf(true) }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val player = remember { MediaPlayer() }
    val chainNow by rememberUpdatedState(chain)
    DisposableEffect(Unit) { onDispose { runCatching { player.release() } } }
    LaunchedEffect(playing) {
        progress = 0f
        while (playing != null) {
            progress = runCatching { player.currentPosition.toFloat() / player.duration.coerceAtLeast(1) }.getOrDefault(0f)
            delay(80)
        }
    }
    fun stop() {
        runCatching { player.reset() }
        playing = null
    }
    fun play(index: Int) {
        val event = clips.getOrNull(index) ?: return
        val clip = event.clip ?: return
        stop()
        current = index
        runCatching {
            player.setDataSource(File(SleepNightStore.clipDir(context), clip).absolutePath)
            player.setOnCompletionListener {
                playing = null
                if (chainNow && current + 1 < clips.size) play(current + 1)
            }
            player.prepare()
            player.start()
            playing = clip
        }
    }
    fun toggle(clip: String) {
        if (playing == clip) stop() else play(clips.indexOfFirst { it.clip == clip })
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            ReportStat(report.events.size.toString(), stringResource(R.string.b11_sleep_stat_events), LeafIcons.GraphicEq, 0, Modifier.weight(1f))
            ReportStat(report.clips.toString(), stringResource(R.string.b14_stat_clips), LeafIcons.Mic, 1, Modifier.weight(1f))
            ReportStat(report.quietShare.toString(), stringResource(R.string.b14_stat_quiet), LeafIcons.MoonStars, 2, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            ReportStat((report.snoreSeconds / 60).toString(), stringResource(R.string.b11_sleep_stat_snore), LeafIcons.Hearing, 3, Modifier.weight(1f))
            ReportStat((report.talkSeconds / 60).toString(), stringResource(R.string.b14_stat_talk), LeafIcons.Mic, 4, Modifier.weight(1f))
            ReportStat(report.loudest.toString(), stringResource(R.string.b13_stat_loudest), LeafIcons.VolumeUp, 5, Modifier.weight(1f))
        }
    }
    if (clips.isNotEmpty()) {
        val event = clips[current.coerceIn(0, clips.lastIndex)]
        val active = playing != null
        LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeafIconBadge(LeafIcons.GraphicEq, size = LeafIconSize.Small, tone = LeafTone.Solid, shape = LeafRosette(8))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.b14_player_title, current.coerceIn(0, clips.lastIndex) + 1, clips.size),
                            style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(eventLabel(event.type)) + " · " + stringResource(R.string.b11_sleep_event_detail,
                            DateUtils.formatTime(event.at), event.seconds, event.peak), style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                com.example.fitnesstracker.ui.components.LeafWavyProgress(if (active) progress else 0f, active,
                    Modifier.fillMaxWidth().height(20.dp), colors.tertiary, colors.onTertiaryContainer.copy(alpha = .18f))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafFilledTonalIconButton(enabled = current > 0, onClick = { play(current - 1) }, modifier = Modifier.size(52.dp)) {
                        Icon(LeafIcons.ArrowBack, stringResource(R.string.b14_player_prev))
                    }
                    LeafButton(onClick = { if (active) stop() else play(current.coerceIn(0, clips.lastIndex)) },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                        Icon(LeafIcons.filled(if (active) LeafIcons.Stop else LeafIcons.PlayArrow), null, Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        LeafFitText(stringResource(if (active) R.string.b14_player_stop else R.string.b14_player_play))
                    }
                    LeafFilledTonalIconButton(enabled = current < clips.lastIndex, onClick = { play(current + 1) }, modifier = Modifier.size(52.dp)) {
                        Icon(LeafIcons.ArrowForward, stringResource(R.string.b14_player_next))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.b14_player_chain), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    LeafSwitch(chain, { chain = it })
                }
            }
        }
    } else LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(LeafIcons.Mic, size = LeafIconSize.Small, tone = LeafTone.Neutral)
            Text(stringResource(R.string.b14_player_empty), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant)
        }
    }
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            var picked by remember(report.start) { mutableStateOf<Float?>(null) }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LeafIconBadge(LeafIcons.GraphicEq, size = LeafIconSize.Small, tone = LeafTone.Tertiary)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.b14_chart_title), style = MaterialTheme.typography.titleSmall)
                    val share = picked
                    Text(if (share != null && report.noise.isNotEmpty()) {
                        val at = report.start + ((report.end - report.start) * share).toLong()
                        val level = report.noise[(share * (report.noise.size - 1)).toInt().coerceIn(0, report.noise.lastIndex)]
                        stringResource(R.string.b14_chart_point, DateUtils.formatTime(at), level)
                    } else stringResource(R.string.b14_chart_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (report.noise.any { it > 0 }) {
                Row(Modifier.fillMaxWidth().height(148.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.fillMaxHeight().padding(top = 14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.b14_chart_loud), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Text(stringResource(R.string.b14_chart_quiet), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                    NightAreaChart(report, colors.tertiary, picked, Modifier.weight(1f).fillMaxHeight()) { share ->
                        picked = share
                        val at = report.start + ((report.end - report.start) * share).toLong()
                        val window = ((report.end - report.start) / 24L).coerceAtLeast(30000L)
                        val near = clips.withIndex().minByOrNull { kotlin.math.abs(it.value.at - at) }
                        if (near != null && kotlin.math.abs(near.value.at - at) <= window) play(near.index)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(start = 44.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(report.start, (report.start + report.end) / 2, report.end).forEach {
                        Text(DateUtils.formatTime(it), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PhaseChip(stringResource(R.string.b12_sleep_filter_snore), colors.primary)
                    PhaseChip(stringResource(R.string.b12_sleep_filter_talk), colors.secondary)
                    PhaseChip(stringResource(R.string.b11_sleep_type_noise), colors.tertiary)
                }
            } else Text(stringResource(R.string.b13_chart_empty), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
    if (report.events.isNotEmpty()) {
        Text(stringResource(R.string.b14_events_title, report.events.size), style = MaterialTheme.typography.titleSmall)
        ExpressiveChoiceRow(listOf(stringResource(R.string.b12_sleep_filter_all), stringResource(R.string.b12_sleep_filter_snore),
            stringResource(R.string.b12_sleep_filter_talk), stringResource(R.string.b11_sleep_type_noise)),
            listOf(LeafIcons.GraphicEq, LeafIcons.Hearing, LeafIcons.Mic, LeafIcons.VolumeUp), filter, { filter = it })
        val shown = remember(report, filter) {
            report.events.filter {
                when (filter) { 1 -> it.type == SleepEvent.SNORE; 2 -> it.type == SleepEvent.TALK; 3 -> it.type == SleepEvent.NOISE; else -> true }
            }.sortedBy { it.at }.take(40)
        }
        if (shown.isEmpty()) Text(stringResource(R.string.b12_sleep_no_events), style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant)
        else Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            shown.forEachIndexed { index, event ->
                val clip = event.clip
                val active = clip != null && playing == clip
                LeafSurface(Modifier.fillMaxWidth(), shape = groupedLeafShape(index, shown.size),
                    color = if (active) colors.tertiaryContainer else colors.surfaceContainerLow) {
                    Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LeafIconBadge(when (event.type) {
                            SleepEvent.SNORE -> LeafIcons.Hearing
                            SleepEvent.TALK -> LeafIcons.Mic
                            else -> LeafIcons.VolumeUp
                        }, size = LeafIconSize.Small, tone = if (event.type == SleepEvent.SNORE) LeafTone.Tertiary else LeafTone.Secondary)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(eventLabel(event.type)), style = MaterialTheme.typography.titleSmall, maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.b11_sleep_event_detail, DateUtils.formatTime(event.at), event.seconds, event.peak),
                                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
                        }
                        if (clip != null) LeafFilledTonalIconButton(onClick = { toggle(clip) }) {
                            Icon(if (active) LeafIcons.Stop else LeafIcons.PlayArrow, stringResource(R.string.b11_sleep_play))
                        } else Spacer(Modifier.size(48.dp))
                    }
                }
            }
        }
    }
    Text(stringResource(R.string.b11_sleep_report_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
}

private fun eventLabel(type: Int): Int = when (type) {
    SleepEvent.SNORE -> R.string.b11_sleep_type_snore
    SleepEvent.TALK -> R.string.b11_sleep_type_talk
    else -> R.string.b11_sleep_type_noise
}

@Composable
private fun PhaseChip(text: String, color: Color) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = CircleShape, color = colors.surfaceContainerHigh) {
        Row(Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(Modifier.size(10.dp)) { drawCircle(color) }
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun NightAreaChart(report: SleepNight, color: Color, picked: Float?, modifier: Modifier = Modifier, onPick: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val guide = colors.outlineVariant
    val surface = colors.surfaceContainerLow
    val snore = colors.primary
    val talk = colors.secondary
    val marker = colors.onSurface
    val pick by rememberUpdatedState(onPick)
    val values = remember(report) {
        val source = report.noise
        val target = 72
        if (source.size <= target) source.map { it.toFloat() } else List(target) { index ->
            val from = index * source.size / target
            val to = ((index + 1) * source.size / target).coerceAtLeast(from + 1).coerceAtMost(source.size)
            source.subList(from, to).max().toFloat()
        }
    }
    val reveal = remember(report.start) { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(report.start) { reveal.animateTo(1f, tween(900)) }
    Canvas(modifier.pointerInput(report.start) {
        detectTapGestures { offset -> pick((offset.x / size.width).coerceIn(0f, 1f)) }
    }.pointerInput(report.start) {
        detectHorizontalDragGestures { change, _ -> pick((change.position.x / size.width).coerceIn(0f, 1f)) }
    }) {
        val top = 22.dp.toPx()
        val bottom = size.height - 4.dp.toPx()
        val low = values.filter { it > 0f }.minOrNull() ?: 0f
        val high = (values.maxOrNull() ?: 1f).coerceAtLeast(low + 6f)
        val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx()))
        repeat(3) { step ->
            val y = top + (bottom - top) * step / 2f
            drawLine(guide, Offset(0f, y), Offset(size.width, y), 1.dp.toPx(), pathEffect = dash)
        }
        val span = (report.end - report.start).coerceAtLeast(1L)
        if (values.size < 2) {
            val share = (((values.firstOrNull() ?: 0f) - low + 3f) / (high - low + 3f)).coerceIn(.08f, 1f)
            drawRoundRect(color.copy(alpha = .5f), Offset(0f, bottom - (bottom - top) * share), Size(size.width, (bottom - top) * share),
                CornerRadius(12.dp.toPx()))
        } else {
            val step = size.width / (values.size - 1)
            val points = values.mapIndexed { index, value ->
                val share = ((value.coerceAtLeast(low) - low) / (high - low)).coerceIn(0f, 1f)
                Offset(index * step, bottom - (bottom - top) * (.06f + .94f * share))
            }
            val line = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val a = points[i - 1]
                    val b = points[i]
                    val mid = (a.x + b.x) / 2f
                    cubicTo(mid, a.y, mid, b.y, b.x, b.y)
                }
            }
            val area = androidx.compose.ui.graphics.Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            clipRect(right = size.width * reveal.value) {
                drawPath(area, androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color.copy(alpha = .38f), color.copy(alpha = .02f)), top, size.height))
                drawPath(line, color, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
        report.events.take(160).forEach { event ->
            val x = ((event.at - report.start).toFloat() / span).coerceIn(0f, 1f) * size.width
            val tone = when (event.type) { SleepEvent.SNORE -> snore; SleepEvent.TALK -> talk; else -> color }
            drawLine(tone.copy(alpha = .35f), Offset(x, 10.dp.toPx()), Offset(x, bottom), 2.dp.toPx())
            drawCircle(surface, 7.dp.toPx(), Offset(x, 10.dp.toPx()))
            drawCircle(tone, if (event.clip != null) 5.dp.toPx() else 3.dp.toPx(), Offset(x, 10.dp.toPx()))
        }
        if (picked != null) {
            val x = picked * size.width
            drawLine(marker, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
private fun ReportStat(value: String, label: String, icon: ImageVector, index: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val corner = 24.dp
    val small = 6.dp
    LeafSurface(modifier, shape = RoundedCornerShape(
        topStart = if (index == 0) corner else small, topEnd = if (index == 2) corner else small,
        bottomStart = if (index == 3) corner else small, bottomEnd = if (index == 5) corner else small),
        color = colors.surfaceContainerLow) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(LeafIcons.filled(icon), null, Modifier.size(18.dp), tint = colors.tertiary)
            LeafFitText(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 2, minLines = 2,
                overflow = TextOverflow.Ellipsis)
        }
    }
}
