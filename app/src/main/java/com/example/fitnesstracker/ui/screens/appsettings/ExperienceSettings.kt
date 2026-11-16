package com.example.fitnesstracker.ui.screens.appsettings

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import kotlinx.coroutines.launch
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.HapticTexture
import com.example.fitnesstracker.utils.Haptics
import com.example.fitnesstracker.widgets.LeafWidgets
import com.example.fitnesstracker.workout.RestTimerAlarm

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExperienceSettings(section: String = "all") {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var enabled by remember { mutableStateOf(Haptics.enabled(context)) }
    var textures by remember { mutableStateOf(HapticTexture.entries.associateWith { Haptics.textureEnabled(context, it) }) }
    var strength by remember { mutableFloatStateOf(Haptics.intensity(context)) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (section == "all" || section == "haptics") LeafSurface(shape = RoundedCornerShape(LeafCorners.card), color = colors.surfaceContainer) {
            Column(Modifier.padding(LeafSpacing.card), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LeafCardHeader(LeafIcons.MobileVibrate, stringResource(R.string.haptic_title),
                    subtitle = stringResource(R.string.haptic_hint)) {
                    LeafSwitch(enabled, { enabled = it; Haptics.setEnabled(context, it); if (it) Haptics.play(context, HapticTexture.CONFIRM) })
                }
                val warning = when {
                    !Haptics.available(context) -> R.string.b5_haptic_unavailable
                    !Haptics.systemEnabled(context) -> R.string.b5_haptic_system_off
                    else -> null
                }
                if (warning != null) LeafSurface(shape = RoundedCornerShape(LeafCorners.chip), color = colors.errorContainer,
                    contentColor = colors.onErrorContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(LeafIcons.Info, null, Modifier.size(20.dp))
                        Text(stringResource(warning), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.haptic_strength), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(when {
                            strength < .4f -> R.string.b7_haptic_soft
                            strength < .7f -> R.string.b7_haptic_medium
                            else -> R.string.b7_haptic_strong
                        }), style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    }
                    Slider(value = strength, onValueChange = { value ->
                            if (value != strength) { strength = value; Haptics.setIntensity(context, value); Haptics.play(context, HapticTexture.DETENT) }
                        }, enabled = enabled, valueRange = .25f..1f, steps = 2,
                        onValueChangeFinished = { Haptics.play(context, HapticTexture.PRESS) })
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    HapticTexture.entries.forEachIndexed { index, texture ->
                        val label = stringResource(when (texture) {
                            HapticTexture.DETENT -> R.string.b5_haptic_select
                            HapticTexture.PRESS -> R.string.b5_haptic_press
                            HapticTexture.START -> R.string.b5_haptic_start
                            HapticTexture.PAUSE -> R.string.b5_haptic_pause
                            HapticTexture.CONFIRM -> R.string.b5_haptic_confirm
                        })
                        val on = textures[texture] == true
                        LeafSurface(Modifier.fillMaxWidth(), shape = groupedLeafShape(index, HapticTexture.entries.size),
                            color = colors.surfaceContainerHigh) {
                            Row(Modifier.padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                                    color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = .38f))
                                androidx.compose.material3.FilledTonalIconButton(enabled = enabled && on,
                                    onClick = { Haptics.play(context, texture) }, modifier = Modifier.size(40.dp)) {
                                    Icon(LeafIcons.PlayArrow, stringResource(R.string.haptic_preview) + ": " + label, Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(4.dp))
                                LeafSwitch(on, { value ->
                                    textures = textures + (texture to value); Haptics.setTextureEnabled(context, texture, value)
                                    if (value) Haptics.play(context, texture)
                                }, enabled = enabled)
                            }
                        }
                    }
                }
                TextButton(onClick = {
                    enabled = true; strength = .65f; Haptics.setEnabled(context, true); Haptics.setIntensity(context, strength)
                    HapticTexture.entries.forEach { Haptics.setTextureEnabled(context, it, true) }
                    textures = HapticTexture.entries.associateWith { true }; Haptics.play(context, HapticTexture.CONFIRM)
                }) {
                    Icon(LeafIcons.Restore, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.b5_haptic_reset))
                }
            }
        }
        if (section == "all" || section == "widgets") LeafSurface(shape = RoundedCornerShape(LeafCorners.card), color = colors.surfaceContainer) {
            Column(Modifier.padding(LeafSpacing.card), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LeafCardHeader(LeafIcons.Widgets, stringResource(R.string.widget_settings_title),
                    subtitle = stringResource(R.string.widget_settings_hint))
                var pinHelp by remember { mutableStateOf(false) }
                val pinScope = rememberCoroutineScope()
                val pinLifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
                if (pinHelp) LeafInfoSheet(title = stringResource(R.string.b5_widget_pin),
                    details = listOf(stringResource(R.string.b11_widget_help_1), stringResource(R.string.b11_widget_help_2),
                        stringResource(R.string.b11_widget_help_3)),
                    onDismiss = { pinHelp = false }, missing = stringResource(R.string.b11_widget_help_permission), icon = LeafIcons.Widgets)
                val labels = listOf(R.string.widget_today, R.string.widget_sleep, R.string.widget_session,
                    R.string.b5_widget_water, R.string.b5_widget_nutrition, R.string.b5_widget_week)
                val hints = listOf(R.string.widget_today_description, R.string.widget_sleep_description, R.string.widget_session_hint,
                    R.string.b5_widget_water_hint, R.string.b5_widget_nutrition_hint, R.string.b5_widget_week_hint)
                val icons = listOf(LeafIcons.VitalSigns, LeafIcons.Bedtime, LeafIcons.Exercise,
                    LeafIcons.WaterFull, LeafIcons.Nutrition, LeafIcons.CalendarViewWeek)
                val tones = listOf(LeafTone.Primary, LeafTone.Tertiary, LeafTone.Secondary,
                    LeafTone.Primary, LeafTone.Secondary, LeafTone.Primary)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    LeafWidgets.providers.forEachIndexed { index, provider ->
                        LeafSurface(onClick = {
                            val manager = AppWidgetManager.getInstance(context)
                            val before = runCatching { manager.getAppWidgetIds(ComponentName(context, provider)).size }.getOrDefault(0)
                            if (manager.isRequestPinAppWidgetSupported) runCatching {
                                manager.requestPinAppWidget(ComponentName(context, provider), null, null)
                            }
                            pinScope.launch {
                                kotlinx.coroutines.delay(1400)
                                val after = runCatching { manager.getAppWidgetIds(ComponentName(context, provider)).size }.getOrDefault(0)
                                if (after <= before && pinLifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) pinHelp = true
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = groupedLeafShape(index, LeafWidgets.providers.size),
                            color = colors.surfaceContainerHigh) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LeafIconBadge(icons.getOrElse(index) { LeafIcons.Widgets }, size = LeafIconSize.Small,
                                    tone = tones.getOrElse(index) { LeafTone.Primary })
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(stringResource(labels[index]), style = MaterialTheme.typography.titleSmall)
                                    Text(stringResource(hints[index]), style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant, maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Icon(LeafIcons.Add, stringResource(R.string.b5_widget_pin), Modifier.size(22.dp), tint = colors.primary)
                            }
                        }
                    }
                }
            }
        }
        if (section == "all" || section == "timers") LeafSurface(shape = RoundedCornerShape(LeafCorners.card), color = colors.surfaceContainer) {
            Column(Modifier.padding(LeafSpacing.card), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LeafCardHeader(LeafIcons.TimerPlay, stringResource(R.string.timer_settings_title),
                    subtitle = stringResource(R.string.timer_settings_hint), tone = LeafTone.Secondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafTonalButton(onClick = {
                        runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }
                    }) {
                        Icon(LeafIcons.Notifications, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.timer_notification_settings))
                    }
                    if (Build.VERSION.SDK_INT >= 31) LeafTonalButton(onClick = {
                        val intent = if (RestTimerAlarm.exactAvailable(context)) Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            else Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        runCatching { context.startActivity(intent.setData(Uri.parse("package:${context.packageName}"))) }
                    }) {
                        Icon(LeafIcons.Alarm, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.timer_exact_settings))
                    }
                }
            }
        }
    }
}
