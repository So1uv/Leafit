package com.example.fitnesstracker.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.ui.theme.ThemePreferences
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlinx.coroutines.launch

internal object WidgetPreferences {
    fun prefs(context: Context) = context.getSharedPreferences("widget_appearance", Context.MODE_PRIVATE)
    fun tone(context: Context, id: Int, fallback: Int = 0) = prefs(context).getInt("tone_$id", fallback).coerceIn(0, 2)
    fun details(context: Context, id: Int) = prefs(context).getBoolean("details_$id", true)
    fun water(context: Context, id: Int) = prefs(context).getInt("water_$id", 250).coerceIn(100, 1000)
    fun remove(context: Context, ids: IntArray) {
        prefs(context).edit().apply { ids.forEach { remove("tone_$it"); remove("details_$it"); remove("water_$it") } }.apply()
    }
}

class WidgetConfigurationActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(LanguagePreferences.localizedContext(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(id)
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID || info == null || info.provider.packageName != packageName) { finish(); return }
        val isWater = info.provider.className == WaterWidget::class.java.name
        val defaultTone = if (info.provider.className == SleepWidget::class.java.name) 1 else 0
        setContent {
            FitnessTrackerTheme(themeMode = ThemePreferences.get(this)) {
                var tone by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(WidgetPreferences.tone(this, id, defaultTone)) }
                var details by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(WidgetPreferences.details(this, id)) }
                var volume by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(WidgetPreferences.water(this, id)) }
                var saving by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val c = MaterialTheme.colorScheme
                Surface(Modifier.fillMaxSize(), color = c.surface) {
                    Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(stringResource(R.string.widget_customize), style = MaterialTheme.typography.headlineMedium)
                        Text(info.loadLabel(packageManager).toString(), style = MaterialTheme.typography.titleMedium, color = c.primary)
                        val container = when (tone) { 1 -> c.tertiaryContainer; 2 -> c.surfaceContainerHigh; else -> c.primaryContainer }
                        val ink = when (tone) { 1 -> c.onTertiaryContainer; 2 -> c.onSurface; else -> c.onPrimaryContainer }
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), color = container, contentColor = ink) {
                            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        LeafIconBadge(if (isWater) LeafIcons.WaterFull else LeafIcons.Steps, size = LeafIconSize.Small,
                                            container = c.surface, content = ink)
                                        Text(info.loadLabel(packageManager).toString(), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                    }
                                    Text(if (isWater) "1 250" else "7 840", style = MaterialTheme.typography.displaySmall)
                                    LinearProgressIndicator(progress = { .64f }, modifier = Modifier.fillMaxWidth().height(10.dp),
                                        color = ink, trackColor = ink.copy(alpha = .2f), drawStopIndicator = {})
                                    if (details) Text(stringResource(R.string.widget_config_hint), style = MaterialTheme.typography.bodySmall,
                                        color = ink.copy(alpha = .8f), maxLines = 2)
                                }
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    CircularProgressIndicator(progress = { .64f }, modifier = Modifier.size(76.dp), color = ink,
                                        trackColor = ink.copy(alpha = .2f), strokeWidth = 8.dp, gapSize = 0.dp)
                                    Text("64%", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                        Text(stringResource(R.string.widget_tone), style = MaterialTheme.typography.titleMedium)
                        ExpressiveChoiceRow(listOf(stringResource(R.string.widget_green), stringResource(R.string.widget_lavender), stringResource(R.string.widget_neutral)),
                            listOf(LeafIcons.Eco, LeafIcons.Bedtime, LeafIcons.Tune), tone, { tone = it })
                        LeafCheckboxRow(details, { details = it }, stringResource(R.string.widget_details))
                        Text(stringResource(R.string.b8_widget_sizes_hint), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                        if (isWater) {
                            Text(stringResource(R.string.widget_water_amount), style = MaterialTheme.typography.titleMedium)
                            ExpressiveChoiceRow(listOf(150, 250, 500).map { "$it ${getString(R.string.unit_ml)}" },
                                List(3) { LeafIcons.WaterFull }, listOf(150, 250, 500).indexOf(volume).coerceAtLeast(0),
                                { volume = listOf(150, 250, 500)[it] })
                        }
                        LeafButton(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                            saving = true
                            WidgetPreferences.prefs(this@WidgetConfigurationActivity).edit()
                                .putInt("tone_$id", tone).putBoolean("details_$id", details).putInt("water_$id", volume).apply()
                            scope.launch {
                                runCatching { kotlinx.coroutines.withTimeout(8000) { LeafWidgets.update(applicationContext) } }
                                    .onFailure { LeafWidgets.requestUpdate(applicationContext) }
                                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                                finish()
                            }
                        }) { Text(stringResource(R.string.common_save)) }
                        LeafTextButton(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.common_cancel)) }
                    }
                }
            }
        }
    }
}
