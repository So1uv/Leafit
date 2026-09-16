package com.example.fitnesstracker.tracking

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.ui.theme.ThemePreferences
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LiveWorkoutActivity : ComponentActivity() {
    private var historyId by mutableLongStateOf(0)
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(LanguagePreferences.applyTo(newBase)) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        historyId = intent.getLongExtra("workout_id", 0)
        setContent { FitnessTrackerTheme(ThemePreferences.get(this)) { key(historyId) { LiveWorkoutPage(historyId) { finish() } } } }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent); setIntent(intent); historyId = intent.getLongExtra("workout_id", 0)
    }
}

@Composable
private fun LiveWorkoutPage(historyId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val live by LiveWorkoutStore.state.collectAsState()
    var saved by remember { mutableStateOf<LiveSession?>(null) }
    var initialized by remember { mutableStateOf(false) }
    var seenLive by rememberSaveable { mutableStateOf(false) }
    var reveal by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }
    var mapTimedOut by remember { mutableStateOf(false) }
    var recenter by remember { mutableIntStateOf(0) }
    var hudHeight by remember { mutableIntStateOf(0) }
    var finishDialog by rememberSaveable { mutableStateOf(false) }
    var discardDialog by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }
    var localIssue by remember { mutableIntStateOf(0) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        localIssue = if (LiveWorkoutController.precise(context)) 0 else R.string.live_permission_needed
    }
    LaunchedEffect(historyId) {
        if (historyId > 0) saved = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(context).workoutDao().getById(historyId)?.let { LiveWorkoutStore.fromMetrics(it.metrics) }
        } else LiveWorkoutStore.hydrate(context.applicationContext)
        initialized = true; reveal = true
    }
    LaunchedEffect(live.active) {
        if (historyId == 0L) {
            if (live.active) seenLive = true
            else if (seenLive) onBack()
        }
    }
    val session = if (historyId > 0) saved ?: LiveSession() else live
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val mapReveal by animateFloatAsState(if (reveal) 1f else 0f,
        spring(dampingRatio = 1f, stiffness = 350f), label = "mapReveal")
    LaunchedEffect(mapLoaded) { if (!mapLoaded) { delay(15000); mapTimedOut = true } }
    val colors = MaterialTheme.colorScheme
    val locale = appLocale()
    val readOnly = historyId > 0
    val busy = session.phase == LivePhase.SAVING
    val status = stringResource(when {
        readOnly -> R.string.live_saved_route
        session.phase == LivePhase.SAVING -> R.string.live_saving
        session.status == GpsStatus.SAVE_ERROR -> R.string.live_save_error
        session.status == GpsStatus.STORAGE -> R.string.live_storage_error
        session.status == GpsStatus.PERMISSION -> R.string.live_permission_needed
        session.status == GpsStatus.RECOVERED -> R.string.live_recovered_hint
        session.phase == LivePhase.PAUSED -> R.string.live_paused
        session.status == GpsStatus.GOOD -> R.string.live_gps_active
        session.status == GpsStatus.WEAK -> R.string.live_gps_weak
        else -> R.string.live_gps_waiting
    })
    val variant = com.example.fitnesstracker.workout.WorkoutCatalog.forType(session.type).firstOrNull { it.id == session.variantId }
    val title = session.title.ifBlank { stringResource(variant?.labelRes ?: when (session.type) {
        "walk" -> R.string.workout_type_walk; "cycle" -> R.string.workout_type_cycle; else -> R.string.live_outdoor_run
    }) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val hudMax = (maxHeight * .58f).coerceAtLeast(150.dp)
        if (session.active || readOnly && saved != null) {
            WorkoutMap(session, colors.primary, recenter, hudHeight, readOnly, Modifier.fillMaxSize().graphicsLayer {
                alpha = mapReveal; translationY = (1f - mapReveal) * 60.dp.toPx()
            }) { mapLoaded = true }
        } else Surface(Modifier.fillMaxSize(), color = colors.surface) { }
        if (!mapLoaded) Box(Modifier.fillMaxSize().padding(bottom = 180.dp), Alignment.Center) {
            Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerHigh) {
                Text(stringResource(if (readOnly && initialized && saved == null) R.string.live_route_missing else if (mapTimedOut) R.string.live_map_network else R.string.live_map_loading),
                    Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        AnimatedVisibility(reveal, enter = fadeIn() + slideInVertically(spring(dampingRatio = .9f, stiffness = 400f)) { -it },
            modifier = Modifier.align(Alignment.TopCenter)) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(48.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.surfaceContainerHigh)) {
                    Icon(LeafIcons.ArrowBack, stringResource(R.string.common_back))
                }
                Surface(shape = RoundedCornerShape(20.dp), color = colors.secondaryContainer, modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        LeafTitle(title, style = MaterialTheme.typography.titleSmall)
                        LeafTitle(status, style = MaterialTheme.typography.labelSmall, color = colors.onSecondaryContainer)
                    }
                }
                FilledTonalIconButton(onClick = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); recenter++ }, enabled = session.points.isNotEmpty(),
                    modifier = Modifier.size(48.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colors.surfaceContainerHigh)) {
                    Icon(LeafIcons.MyLocation, stringResource(R.string.live_recenter))
                }
            }
        }
        AnimatedVisibility(reveal && (session.active || readOnly),
            enter = fadeIn() + slideInVertically(spring(dampingRatio = .88f, stiffness = 400f)) { it },
            modifier = Modifier.align(Alignment.BottomCenter)) {
            Box(Modifier.fillMaxWidth().onSizeChanged { hudHeight = it.height }.navigationBarsPadding().padding(12.dp)) {
                Surface(shape = RoundedCornerShape(32.dp), color = colors.surfaceContainer, tonalElevation = 0.dp) {
                    Column(Modifier.fillMaxWidth().heightIn(max = hudMax).verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (session.points.isEmpty()) "—" else String.format(locale, "%.2f", session.distanceMeters / 1000),
                                style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.live_km), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium,
                                color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiveMetric(DateUtils.formatDuration(session.elapsedMs / 1000), stringResource(R.string.workout_time_label), Modifier.weight(1f))
                            LiveMetric(session.accuracy?.let { "±${it.toInt()} ${stringResource(R.string.live_m)}" } ?: "—",
                                stringResource(R.string.live_accuracy), Modifier.weight(1f))
                        }
                        LiveTelemetryGrid(session, readOnly)
                        if (session.points.isEmpty()) Text(stringResource(R.string.live_no_points), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        if (readOnly) com.example.fitnesstracker.sharing.ShareWorkoutButton(historyId, Modifier.fillMaxWidth())
                        if (localIssue != 0) Text(stringResource(localIssue), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        if (!readOnly) {
                            LeafRunningWave(session.phase == LivePhase.ACTIVE, Modifier.fillMaxWidth().height(12.dp))
                            if (session.status == GpsStatus.PERMISSION || localIssue == R.string.live_permission_needed) TextButton(onClick = {
                                permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }) { Text(stringResource(R.string.live_allow_location)) }
                            if (localIssue == R.string.live_location_off || session.status == GpsStatus.WEAK && !locationEnabled(context)) TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                                Text(stringResource(R.string.live_open_settings))
                            }
                            LeafTonalButton(enabled = !busy, onClick = {
                                if (session.phase == LivePhase.ACTIVE) LiveWorkoutController.command(context, "pause")
                                else if (!LiveWorkoutController.precise(context)) localIssue = R.string.live_permission_needed
                                else if (!locationEnabled(context)) localIssue = R.string.live_location_off
                                else { localIssue = 0; LiveWorkoutController.command(context, "resume") }
                            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = CircleShape,
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.primaryContainer)) {
                                AnimatedContent(session.phase == LivePhase.ACTIVE, transitionSpec = {
                                    (fadeIn() + scaleIn(spring(.85f, 450f), initialScale = .8f)).togetherWith(fadeOut() + scaleOut(targetScale = .8f))
                                }, label = "pauseAction") { active ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if (active) LeafIcons.Pause else LeafIcons.PlayArrow, null)
                                        Spacer(Modifier.width(8.dp)); Text(stringResource(if (active) R.string.workout_pause else R.string.workout_resume))
                                    }
                                }
                            }
                            HoldToFinish(!busy) { LiveWorkoutController.command(context, "pause"); finishDialog = true }
                        }
                    }
                }
            }
        }
    }
    if (finishDialog || discardDialog) LeafEditSheet(onDismissRequest = {
        if (!busy) { finishDialog = false; discardDialog = false }
    },
        title = { LeafTitle(stringResource(if (discardDialog) R.string.live_discard_question else R.string.live_finish_confirmation),
            style = MaterialTheme.typography.headlineSmall) },
        content = {
            AnimatedContent(discardDialog, transitionSpec = { fadeIn().togetherWith(fadeOut()) }, label = "finishSheetStep") { discard ->
                Column(Modifier.fillMaxWidth()) {
                    if (!discard) AppTextField(note, { note = it }, stringResource(R.string.workout_note_hint), Modifier.fillMaxWidth())
                    else Icon(LeafIcons.DeleteOutline, null, Modifier.padding(12.dp).size(40.dp), tint = colors.error)
                    if (session.status == GpsStatus.SAVE_ERROR || session.status == GpsStatus.STORAGE)
                        Text(stringResource(if (discard) R.string.live_storage_error else R.string.live_save_error), color = colors.error)
                }
            }
        },
        confirmButton = { LeafButton(enabled = !busy, onClick = {
            if (discardDialog) LiveWorkoutController.command(context, "discard")
            else LiveWorkoutController.command(context, "save", note = note)
        }, colors = ButtonDefaults.buttonColors(containerColor = if (discardDialog) colors.error else colors.primary)) {
            Text(stringResource(if (discardDialog) R.string.common_delete else R.string.common_save))
        } },
        dismissButton = { TextButton(enabled = !busy, onClick = { discardDialog = !discardDialog }) {
            Text(stringResource(if (discardDialog) R.string.common_cancel else R.string.common_delete))
        } })
}

@Composable
private fun LiveMetric(value: String, label: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
