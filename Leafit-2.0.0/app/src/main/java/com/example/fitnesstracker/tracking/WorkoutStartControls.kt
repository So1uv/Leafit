package com.example.fitnesstracker.tracking

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafSwitch
import com.example.fitnesstracker.ui.components.LeafButton
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.LeafIcons
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

internal fun locationEnabled(context: Context) = LocationManagerCompat.isLocationEnabled(context.getSystemService(LocationManager::class.java))
internal fun openLive(context: Context, workoutId: Long = 0) {
    context.startActivity(Intent(context, LiveWorkoutActivity::class.java).putExtra("workout_id", workoutId))
}

@Composable
fun WorkoutStartControls(type: String, routeAllowed: Boolean = LiveWorkoutController.outdoor(type),
    variantId: String = "", sessionTitle: String = "", onStartWithoutGps: () -> Unit) {
    val context = LocalContext.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val live by LiveWorkoutStore.state.collectAsState()
    var ready by remember { mutableStateOf(false) }
    var enabled by rememberSaveable(type, routeAllowed) { mutableStateOf(false) }
    var issue by rememberSaveable { mutableIntStateOf(0) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        enabled = LiveWorkoutController.precise(context)
        issue = if (enabled) 0 else R.string.live_permission_needed
        if (enabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(Unit) { LiveWorkoutStore.hydrate(context.applicationContext); ready = true }
    val colors = MaterialTheme.colorScheme
    val fill by animateColorAsState(if (enabled) colors.secondaryContainer else colors.surfaceContainerHigh,
        spring(stiffness = 500f), label = "gpsSwitch")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (live.active) {
            Surface(onClick = { openLive(context) }, shape = RoundedCornerShape(28.dp), color = colors.secondaryContainer) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(LeafIcons.Map, null)
                    Text(stringResource(if (live.status == GpsStatus.RECOVERED) R.string.live_recover else R.string.live_return),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(LeafIcons.ArrowForward, null)
                }
            }
        } else {
            AnimatedVisibility(routeAllowed && LiveWorkoutController.outdoor(type)) {
                Surface(shape = RoundedCornerShape(28.dp), color = fill) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(LeafIcons.Map, null, tint = colors.onSecondaryContainer)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.live_toggle), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.live_toggle_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        LeafSwitch(checked = enabled, onCheckedChange = { value ->
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            if (!value) { enabled = false; issue = 0 }
                            else if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) issue = R.string.live_services_needed
                            else if (LiveWorkoutController.precise(context)) {
                                enabled = true; issue = 0
                                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                                    notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }, thumbContent = { Icon(if (enabled) LeafIcons.LocationOn else LeafIcons.Map, null, Modifier.size(16.dp)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primaryContainer, checkedThumbColor = colors.primary,
                                checkedIconColor = colors.onPrimary, uncheckedTrackColor = colors.surfaceContainerHighest,
                                uncheckedBorderColor = Color.Transparent))
                    }
                }
            }
            if (issue != 0) {
                Text(stringResource(issue), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                if (issue == R.string.live_permission_needed || issue == R.string.live_location_off) TextButton(onClick = {
                    val intent = if (issue == R.string.live_location_off) Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    context.startActivity(intent)
                }) { Text(stringResource(R.string.live_open_settings)) }
            }
            LeafButton(enabled = ready, onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                if (!enabled || !routeAllowed || !LiveWorkoutController.outdoor(type)) onStartWithoutGps()
                else if (!LiveWorkoutController.precise(context)) { enabled = false; issue = R.string.live_permission_needed }
                else if (!locationEnabled(context)) issue = R.string.live_location_off
                else if (LiveWorkoutController.command(context, "start", type, variantId = variantId, sessionTitle = sessionTitle)) openLive(context)
                else issue = R.string.live_permission_needed
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), shape = CircleShape) {
                Icon(LeafIcons.PlayArrow, null, Modifier.size(22.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.workout_start), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
fun SavedRouteButton(workoutId: Long, metrics: String) {
    val context = LocalContext.current
    if (metrics.contains("\"leaf_route_v1\"")) TextButton(onClick = { openLive(context, workoutId) }) {
        Icon(LeafIcons.Map, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.live_view_route))
    }
}
