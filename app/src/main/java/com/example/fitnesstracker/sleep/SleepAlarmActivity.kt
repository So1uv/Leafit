package com.example.fitnesstracker.sleep

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LeafButton
import com.example.fitnesstracker.ui.components.LeafFitText
import com.example.fitnesstracker.ui.components.LeafIcons
import com.example.fitnesstracker.ui.components.LeafRosette
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.filled
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.ui.theme.ThemePreferences
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlinx.coroutines.delay

class SleepAlarmActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(LanguagePreferences.applyTo(newBase)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { FitnessTrackerTheme(ThemePreferences.get(this)) { AlarmScreen { finish() } } }
    }
}

@Composable
private fun AlarmScreen(onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = MaterialTheme.colorScheme
    val live by SleepTracker.state.collectAsState()
    val settings = remember { SleepTrackPrefs.load(context) }
    var seen by remember { mutableStateOf(false) }
    LaunchedEffect(live.ringing) {
        if (live.ringing) seen = true
        else {
            if (!seen) delay(1500)
            if (!SleepTracker.state.value.ringing) onClose()
        }
    }
    BackHandler { }
    val now by produceState(System.currentTimeMillis()) {
        while (true) { value = System.currentTimeMillis(); delay(1000) }
    }
    val transition = rememberInfiniteTransition(label = "alarm")
    val spin by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "alarmSpin")
    val pulse by transition.animateFloat(.94f, 1.06f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "alarmPulse")
    LeafSurface(Modifier.fillMaxSize(), color = colors.tertiaryContainer, contentColor = colors.onTertiaryContainer) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(.6f))
            Box(Modifier.size(168.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }, contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = spin }.clip(LeafRosette(8)).background(colors.tertiary))
                Icon(LeafIcons.filled(LeafIcons.Alarm), null, Modifier.size(72.dp), tint = colors.onTertiary)
            }
            Spacer(Modifier.height(28.dp))
            LeafFitText(DateUtils.formatTime(now), Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center)
            Text(live.label.ifBlank { stringResource(R.string.b11_sleep_alarm) }, style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center)
            Text(DateUtils.formatDate(now), style = MaterialTheme.typography.titleMedium, color = colors.onTertiaryContainer.copy(alpha = .75f))
            Spacer(Modifier.weight(1f))
            var solved by remember { mutableStateOf(false) }
            if (live.math && !solved) {
                var round by remember { mutableIntStateOf(0) }
                val task = remember(round) {
                    val a = (12..48).random()
                    val b = (7..29).random()
                    Triple(a, b, listOf(a + b, a + b + listOf(-10, 10, -2, 2).random(), a + b + listOf(-1, 1, 5, -5).random())
                        .distinct().shuffled())
                }
                LeafSurface(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                    color = colors.surface.copy(alpha = .6f), contentColor = colors.onSurface) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.b12_sleep_math_task, task.first, task.second), style = MaterialTheme.typography.displaySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            task.third.forEach { answer ->
                                LeafTonalButton(onClick = { if (answer == task.first + task.second) solved = true else round++ },
                                    modifier = Modifier.weight(1f).heightIn(min = 64.dp)) {
                                    Text(answer.toString(), style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (live.canSnooze) LeafSurface(onClick = { SleepTracker.send(context, SleepTracker.ACTION_SNOOZE) },
                    modifier = Modifier.fillMaxWidth().height(72.dp), shape = CircleShape, color = colors.surface.copy(alpha = .7f),
                    contentColor = colors.onSurface) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(LeafIcons.filled(LeafIcons.Snooze), null, Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.b11_sleep_snooze, settings.snooze), style = MaterialTheme.typography.titleLarge)
                    }
                }
                if (!live.math || solved) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (live.active) LeafTonalButton(onClick = { SleepTracker.send(context, SleepTracker.ACTION_DISMISS) },
                        modifier = Modifier.weight(1f).heightIn(min = 72.dp)) {
                        LeafFitText(stringResource(R.string.b13_alarm_dismiss), style = MaterialTheme.typography.titleMedium)
                    }
                    LeafButton(onClick = {
                        SleepTracker.send(context, if (live.active) SleepTracker.ACTION_STOP else SleepTracker.ACTION_DISMISS)
                    }, modifier = Modifier.weight(1f).heightIn(min = 72.dp)) {
                        Icon(LeafIcons.filled(LeafIcons.WbSunny), null, Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        LeafFitText(stringResource(if (live.active) R.string.b11_sleep_wake else R.string.b13_alarm_dismiss),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
