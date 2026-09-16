package com.example.fitnesstracker.sharing

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.tracking.MapCaptureController
import com.example.fitnesstracker.tracking.WorkoutMap
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.ui.theme.ThemePreferences
import com.example.fitnesstracker.utils.HapticTexture
import com.example.fitnesstracker.utils.Haptics
import com.example.fitnesstracker.utils.LanguagePreferences
import kotlinx.coroutines.*
import java.io.File

class WorkoutShareActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) { super.attachBaseContext(LanguagePreferences.applyTo(newBase)) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val id = intent.getLongExtra("workout_id", 0L)
        setContent { FitnessTrackerTheme(ThemePreferences.get(this)) { WorkoutSharePage(id) { finish() } } }
    }
}

@Composable
fun ShareWorkoutButton(workoutId: Long, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LeafTonalButton(onClick = { context.startActivity(Intent(context, WorkoutShareActivity::class.java).putExtra("workout_id", workoutId)) }, modifier = modifier) {
        Icon(LeafIcons.Upload, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.share_workout))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutSharePage(id: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember(id) { mutableStateOf<ShareWorkout?>(null) }
    var loading by remember(id) { mutableStateOf(true) }
    var error by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    val systemDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(systemDark) }
    var mode by rememberSaveable { mutableStateOf(RouteCardMode.MAP) }
    val capture = remember { MapCaptureController() }
    LaunchedEffect(id) {
        try {
            data = withContext(Dispatchers.IO) { AppDatabase.getInstance(context).workoutDao().getById(id)?.let { ShareWorkout.from(it) } }
            if (data == null) error = R.string.share_missing
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = R.string.share_error }
        finally { loading = false }
    }
    val header = rememberLeafHeaderState()
    LeafScreenScaffold(headerState = header, containerColor = MaterialTheme.colorScheme.surface,
        topBar = { CompactPageTopBar(stringResource(R.string.share_workout), stringResource(R.string.share_preview_hint), LeafIcons.Upload, onBack, header) }) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            val selected = data
            if (selected != null) {
                val image = preview
                if (image != null) {
                    Image(image.asImageBitmap(), stringResource(R.string.share_preview_description),
                        Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(RoundedCornerShape(28.dp)))
                    LeafButton(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                        busy = true; error = null
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    val folder = File(context.cacheDir, "workout_cards").apply { check(mkdirs() || isDirectory) }
                                    val cutoff = System.currentTimeMillis() - 86_400_000L
                                    folder.listFiles()?.filter { it.isFile && it.lastModified() < cutoff }?.forEach { it.delete() }
                                    File.createTempFile("leafit_", ".png", folder).also { target ->
                                        target.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                                    }
                                }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.workout.cards", file)
                                val send = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri)
                                    .apply { clipData = ClipData.newUri(context.contentResolver, "Leafit", uri) }.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(send, context.getString(R.string.share_workout)))
                                Haptics.play(context, HapticTexture.CONFIRM)
                            } catch (e: CancellationException) { throw e }
                            catch (_: Exception) { error = R.string.share_error }
                            finally { busy = false }
                        }
                    }) { Text(stringResource(R.string.share_send)) }
                    LeafTonalButton(enabled = !busy, onClick = { preview = null; error = null }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.share_customize))
                    }
                } else {
                    Text(stringResource(R.string.share_card_options), style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.share_dark_card), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        LeafSwitch(dark, { dark = it }, enabled = !busy)
                    }
                    val hasRoute = !selected.route?.points.isNullOrEmpty()
                    if (hasRoute) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            RouteCardMode.entries.forEach { option ->
                                FilterChip(selected = mode == option, enabled = !busy, onClick = { mode = option; error = null }, border = null,
                                    label = { Text(stringResource(when (option) {
                                        RouteCardMode.MAP -> R.string.share_map
                                        RouteCardMode.ROUTE -> R.string.share_route_only
                                        RouteCardMode.HIDDEN -> R.string.share_hide_route
                                    })) })
                            }
                        }
                        if (mode == RouteCardMode.MAP) WorkoutMap(selected.route!!, MaterialTheme.colorScheme.primary, 0, 0, true,
                            modifier = Modifier.fillMaxWidth().aspectRatio(WorkoutShareCard.MAP_WIDTH / WorkoutShareCard.MAP_HEIGHT).clip(RoundedCornerShape(28.dp)),
                            capture = capture, onLoaded = {})
                        Text(stringResource(R.string.share_route_choice_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LeafButton(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                        busy = true; error = null
                        val chosen = if (hasRoute) mode else RouteCardMode.HIDDEN
                        scope.launch {
                            try {
                                val map = if (chosen == RouteCardMode.MAP) capture.capture() else null
                                if (chosen == RouteCardMode.MAP && map == null) { error = R.string.share_map_not_ready; return@launch }
                                preview = withContext(Dispatchers.Default) { WorkoutShareCard.render(context, selected, chosen, map, dark) }
                            } catch (e: CancellationException) { throw e }
                            catch (_: Exception) { error = R.string.share_error }
                            finally { busy = false }
                        }
                    }) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text(stringResource(R.string.share_make_preview))
                    }
                }
            }
        }
    }
}
