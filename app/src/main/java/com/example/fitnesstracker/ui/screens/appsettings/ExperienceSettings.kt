package com.example.fitnesstracker.ui.screens.appsettings

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
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.HapticTexture
import com.example.fitnesstracker.utils.Haptics
import com.example.fitnesstracker.widgets.LeafWidgets
import com.example.fitnesstracker.workout.RestTimerAlarm

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExperienceSettings() {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var enabled by remember { mutableStateOf(Haptics.enabled(context)) }
    var strength by remember { mutableFloatStateOf(Haptics.intensity(context)) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.experience_title), style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 12.dp))
        LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(LeafIcons.Tune, null, tint = colors.primary, modifier = Modifier.padding(end = 12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.haptic_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.haptic_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    LeafSwitch(enabled, { enabled = it; Haptics.setEnabled(context, it); if (it) Haptics.play(context, HapticTexture.PRESS) })
                }
                Text(stringResource(R.string.haptic_strength), style = MaterialTheme.typography.labelLarge)
                Slider(value = strength, onValueChange = { strength = it }, enabled = enabled, valueRange = .25f..1f, steps = 2,
                    onValueChangeFinished = { Haptics.setIntensity(context, strength); Haptics.play(context, HapticTexture.DETENT) })
                LeafTonalButton(onClick = { Haptics.play(context, HapticTexture.CONFIRM) }, enabled = enabled) {
                    Text(stringResource(R.string.haptic_preview))
                }
            }
        }
        LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.widget_settings_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.widget_settings_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LeafWidgets.providers.forEachIndexed { index, provider ->
                        LeafTonalButton(onClick = {
                            val manager = AppWidgetManager.getInstance(context)
                            if (manager.isRequestPinAppWidgetSupported) {
                                val accepted = runCatching { manager.requestPinAppWidget(ComponentName(context, provider), null, null) }.getOrDefault(false)
                                if (!accepted) Toast.makeText(context, R.string.widget_pin_hint, Toast.LENGTH_LONG).show()
                            } else Toast.makeText(context, R.string.widget_pin_hint, Toast.LENGTH_LONG).show()
                        }) { Text(stringResource(listOf(R.string.widget_today, R.string.widget_sleep, R.string.widget_session)[index])) }
                    }
                }
            }
        }
        LeafSurface(shape = RoundedCornerShape(28.dp), color = colors.surfaceContainer) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.timer_settings_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.timer_settings_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                LeafTonalButton(onClick = {
                    runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }
                }) { Text(stringResource(R.string.timer_notification_settings)) }
                if (Build.VERSION.SDK_INT >= 31) LeafTonalButton(onClick = {
                    val intent = if (RestTimerAlarm.exactAvailable(context)) Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        else Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    runCatching { context.startActivity(intent.setData(Uri.parse("package:${context.packageName}"))) }
                }) { Text(stringResource(R.string.timer_exact_settings)) }
            }
        }
    }
}
