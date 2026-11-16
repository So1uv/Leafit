package com.example.fitnesstracker.sharing

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt
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
    val colors = MaterialTheme.colorScheme
    var data by remember(id) { mutableStateOf<ShareWorkout?>(null) }
    var insightPool by remember(id) { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember(id) { mutableStateOf(true) }
    var error by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    val preferences = remember(context) { context.getSharedPreferences("wrapped_design", Context.MODE_PRIVATE) }
    fun <T : Enum<T>> savedEnum(key: String, values: Array<T>, fallback: T): T =
        values.firstOrNull { it.name == preferences.getString(key, null) } ?: fallback
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var heroMetric by rememberSaveable { mutableStateOf(savedEnum("hero", WrappedHero.entries.toTypedArray(), WrappedHero.AUTO)) }
    var dark by rememberSaveable { mutableStateOf(preferences.getBoolean("dark", systemDark)) }
    var mode by rememberSaveable { mutableStateOf(savedEnum("route", RouteCardMode.entries.toTypedArray(), RouteCardMode.ROUTE)) }
    var style by rememberSaveable { mutableStateOf(savedEnum("style", WrappedStyle.entries.toTypedArray(), WrappedStyle.MINT)) }
    var format by rememberSaveable { mutableStateOf(savedEnum("format", WrappedFormat.entries.toTypedArray(), WrappedFormat.AUTO)) }
    var sticker by rememberSaveable { mutableStateOf(preferences.getBoolean("sticker", false)) }
    var showStats by rememberSaveable { mutableStateOf(preferences.getBoolean("stats", true)) }
    var showDate by rememberSaveable { mutableStateOf(preferences.getBoolean("date", true)) }
    var showInsights by rememberSaveable { mutableStateOf(preferences.getBoolean("insights", true)) }
    var layoutStyle by rememberSaveable { mutableStateOf(savedEnum("layout", WrappedLayout.entries.toTypedArray(), WrappedLayout.POSTER)) }
    var showExercises by rememberSaveable { mutableStateOf(preferences.getBoolean("exercises", true)) }
    var showEnergy by rememberSaveable { mutableStateOf(preferences.getBoolean("energy", true)) }
    var customTitle by rememberSaveable { mutableStateOf("") }
    var caption by rememberSaveable { mutableStateOf("") }
    var livePreview by remember { mutableStateOf<Bitmap?>(null) }
    var rendering by remember { mutableStateOf(false) }
    val capture = remember { MapCaptureController() }
    LaunchedEffect(id) {
        try {
            val (loaded, pool) = withContext(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(context).workoutDao()
                val workout = dao.getById(id)
                val share = workout?.let { ShareWorkout.from(it) }
                share to (workout?.let { wrappedInsights(context, it, dao.getAll().first()) } ?: emptyList())
            }
            data = loaded; insightPool = pool
            if (loaded == null) error = R.string.share_missing
            loaded?.let {
                if ((heroMetric == WrappedHero.DISTANCE && it.workout.distanceMeters <= 0) ||
                    (heroMetric == WrappedHero.VOLUME && (it.plan?.volumeKg ?: 0.0) <= 0.0) ||
                    (heroMetric == WrappedHero.STEPS && it.workout.steps <= 0)) heroMetric = WrappedHero.AUTO
            }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = R.string.share_error }
        finally { loading = false }
    }
    val insights = if (showInsights) insightPool else emptyList()
    LaunchedEffect(data, dark, mode, style, showStats, showDate, layoutStyle, showExercises, showEnergy, customTitle, caption,
        heroMetric, insights, format, sticker) {
        val selected = data ?: return@LaunchedEffect
        delay(160)
        rendering = true
        try {
            livePreview = withContext(Dispatchers.Default) {
                val card = WorkoutShareCard.render(context, selected, if (mode == RouteCardMode.MAP) RouteCardMode.ROUTE else mode,
                    null, dark, style, showStats, showDate, caption, layoutStyle, showExercises, showEnergy, customTitle,
                    outputWidth = 540, heroMetric = heroMetric, insights = insights, transparent = sticker)
                WorkoutShareCard.frame(card, format, if (sticker) null else WorkoutShareCard.toneColor(style, dark))
            }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error = R.string.share_error }
        finally { rendering = false }
    }

    suspend fun finalImage(selected: ShareWorkout): Bitmap? {
        val hasRoute = !selected.route?.points.isNullOrEmpty()
        val chosen = if (hasRoute && layoutStyle != WrappedLayout.FOCUS) mode else RouteCardMode.HIDDEN
        val map = if (chosen == RouteCardMode.MAP) capture.capture() else null
        if (chosen == RouteCardMode.MAP && map == null) { error = R.string.share_map_not_ready; return null }
        return withContext(Dispatchers.Default) {
            val card = WorkoutShareCard.render(context, selected, chosen, map, dark, style, showStats, showDate, caption, layoutStyle,
                showExercises, showEnergy, customTitle, heroMetric = heroMetric, insights = insights, transparent = sticker)
            WorkoutShareCard.frame(card, format, if (sticker) null else WorkoutShareCard.toneColor(style, dark))
        }
    }

    fun export(save: Boolean) {
        val selected = data ?: return
        busy = true; error = null
        scope.launch {
            try {
                val image = finalImage(selected) ?: return@launch
                if (save) {
                    val ok = withContext(Dispatchers.IO) { saveToGallery(context, image) }
                    android.widget.Toast.makeText(context, if (ok) R.string.b9_wrap_saved else R.string.share_error,
                        android.widget.Toast.LENGTH_SHORT).show()
                } else {
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
                }
                Haptics.play(context, HapticTexture.CONFIRM)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { error = R.string.share_error }
            finally { busy = false }
        }
    }

    val header = rememberLeafHeaderState()
    LeafScreenScaffold(headerState = header, containerColor = colors.surface,
        topBar = { CompactPageTopBar(stringResource(R.string.share_workout), stringResource(R.string.share_preview_hint), LeafIcons.Upload, onBack, header) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val stageTone = androidx.compose.ui.graphics.Color(WorkoutShareCard.toneColor(style, dark))
                Surface(shape = RoundedCornerShape(32.dp), color = if (sticker) colors.surfaceContainerHigh else stageTone) {
                    Box(Modifier.fillMaxWidth().heightIn(min = 240.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                        val image = livePreview
                        if (image != null) Image(image.asImageBitmap(), stringResource(R.string.share_preview_description),
                            Modifier.heightIn(max = 440.dp).aspectRatio(image.width.toFloat() / image.height)
                                .clip(RoundedCornerShape(if (format == WrappedFormat.AUTO) 24.dp else 12.dp)))
                        else if (loading || data != null) LoadingIndicatorShape()
                        if (rendering && image != null) Box(Modifier.align(Alignment.TopEnd)) {
                            LeafLoadingIndicator(Modifier.size(24.dp), colors.primary)
                        }
                    }
                }
                error?.let { Text(stringResource(it), color = colors.error, style = MaterialTheme.typography.bodyMedium) }
                val selected = data ?: return@Column
                ExpressiveChoiceRow(listOf(stringResource(R.string.b9_wrap_tab_style), stringResource(R.string.b9_wrap_tab_data),
                    stringResource(R.string.b9_wrap_tab_words), stringResource(R.string.b9_wrap_tab_format)),
                    listOf(LeafIcons.Palette, LeafIcons.Tune, LeafIcons.EditNote, LeafIcons.Widgets), tab, { tab = it })
                when (tab) {
                    0 -> {
                        FeatureSection(stringResource(R.string.design_wrapped_style), icon = LeafIcons.Palette) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                WrappedStyle.entries.forEach { option ->
                                    val active = style == option
                                    val swatch = androidx.compose.ui.graphics.Color(WorkoutShareCard.toneColor(option, dark))
                                    val ink = androidx.compose.ui.graphics.Color(WorkoutShareCard.accentColor(option, dark))
                                    com.example.fitnesstracker.ui.theme.LeafSurface(onClick = { style = option }, enabled = !busy,
                                        modifier = Modifier.size(56.dp), shape = if (active) LeafRosette() else androidx.compose.foundation.shape.CircleShape,
                                        color = swatch, border = if (active) null else androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (active) Icon(LeafIcons.Check, null, Modifier.size(24.dp), tint = ink)
                                            else Box(Modifier.size(18.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(ink))
                                        }
                                    }
                                }
                            }
                            Text(stringResource(when (style) {
                                WrappedStyle.MINT -> R.string.wrapped_mint; WrappedStyle.DAWN -> R.string.wrapped_dawn
                                WrappedStyle.LAVENDER -> R.string.wrapped_lavender; WrappedStyle.SKY -> R.string.b5_wrapped_sky
                                WrappedStyle.SAGE -> R.string.b5_sage
                            }), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                            WrappedToggle(stringResource(R.string.share_dark_card), dark, !busy) { dark = it }
                        }
                        FeatureSection(stringResource(R.string.wrapped_layout), icon = LeafIcons.Category) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                WrappedLayout.entries.forEachIndexed { index, option ->
                                    val active = layoutStyle == option
                                    com.example.fitnesstracker.ui.theme.LeafSurface(onClick = {
                                        layoutStyle = option; showStats = option != WrappedLayout.FOCUS
                                        showEnergy = option != WrappedLayout.FOCUS; showExercises = option != WrappedLayout.FOCUS
                                    }, enabled = !busy, shape = groupedLeafShape(index, WrappedLayout.entries.size),
                                        color = if (active) colors.primaryContainer else colors.surfaceContainerHigh) {
                                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                            RadioButton(selected = active, onClick = null)
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(stringResource(when (option) {
                                                    WrappedLayout.POSTER -> R.string.wrapped_poster
                                                    WrappedLayout.JOURNAL -> R.string.wrapped_journal
                                                    WrappedLayout.FOCUS -> R.string.wrapped_focus
                                                }), style = MaterialTheme.typography.titleMedium)
                                                Text(stringResource(when (option) {
                                                    WrappedLayout.POSTER -> R.string.design_wrapped_poster_hint
                                                    WrappedLayout.JOURNAL -> R.string.design_wrapped_journal_hint
                                                    WrappedLayout.FOCUS -> R.string.design_wrapped_focus_hint
                                                }), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        FeatureSection(stringResource(R.string.b5_wrapped_hero), icon = LeafIcons.Star) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                WrappedHero.entries.filter { option -> when (option) {
                                    WrappedHero.DISTANCE -> selected.workout.distanceMeters > 0
                                    WrappedHero.VOLUME -> (selected.plan?.volumeKg ?: 0.0) > 0.0
                                    WrappedHero.STEPS -> selected.workout.steps > 0
                                    else -> true
                                } }.forEach { option ->
                                    FeatureChoice(heroMetric == option, stringResource(when (option) {
                                        WrappedHero.AUTO -> R.string.b5_wrapped_auto
                                        WrappedHero.DURATION -> R.string.share_duration
                                        WrappedHero.DISTANCE -> R.string.b5_metric_kilometers
                                        WrappedHero.VOLUME -> R.string.workout_result_volume
                                        WrappedHero.STEPS -> R.string.b5_metric_steps
                                    }), { heroMetric = option }, enabled = !busy)
                                }
                            }
                        }
                        FeatureSection(stringResource(R.string.b9_wrap_insights), icon = LeafIcons.Trophy) {
                            WrappedToggle(stringResource(R.string.b9_wrap_insights), showInsights, !busy && insightPool.isNotEmpty()) { showInsights = it }
                            if (insightPool.isEmpty()) Text(stringResource(R.string.b9_wrap_insights_empty), style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant)
                            else FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                insightPool.forEach { label ->
                                    Surface(shape = RoundedCornerShape(16.dp), color = colors.secondaryContainer) {
                                        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                        FeatureSection(stringResource(R.string.design_wrapped_content), icon = LeafIcons.Checklist) {
                            WrappedToggle(stringResource(R.string.wrapped_show_date), showDate, !busy) { showDate = it }
                            val hasSets = (selected.plan?.completedCount ?: 0) > 0
                            if (selected.workout.distanceMeters > 0 || hasSets || heroMetric !in listOf(WrappedHero.AUTO, WrappedHero.DURATION))
                                WrappedToggle(stringResource(R.string.wrapped_show_stats), showStats, !busy) { showStats = it }
                            if (hasSets && layoutStyle != WrappedLayout.FOCUS)
                                WrappedToggle(stringResource(R.string.wrapped_show_exercises), showExercises, !busy) { showExercises = it }
                            if (selected.workout.caloriesBurned > 0 || selected.workout.steps > 0)
                                WrappedToggle(stringResource(R.string.wrapped_show_energy), showEnergy, !busy) { showEnergy = it }
                        }
                        val hasRoute = !selected.route?.points.isNullOrEmpty()
                        if (hasRoute && layoutStyle != WrappedLayout.FOCUS) FeatureSection(stringResource(R.string.design_wrapped_route), icon = LeafIcons.Route) {
                            ExpressiveChoiceRow(RouteCardMode.entries.map { option -> stringResource(when (option) {
                                RouteCardMode.MAP -> R.string.share_map
                                RouteCardMode.ROUTE -> R.string.share_route_only
                                RouteCardMode.HIDDEN -> R.string.share_hide_route
                            }) }, listOf(LeafIcons.Map, LeafIcons.Route, LeafIcons.Close), mode.ordinal, { mode = RouteCardMode.entries[it]; error = null })
                            Text(stringResource(if (mode == RouteCardMode.MAP) R.string.design_wrapped_map_preview else R.string.share_route_choice_hint),
                                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            if (mode == RouteCardMode.MAP) WorkoutMap(selected.route!!, colors.primary, 0, 0, true,
                                modifier = Modifier.fillMaxWidth().aspectRatio(968f / 460f).clip(RoundedCornerShape(24.dp)),
                                capture = capture, onLoaded = {})
                        }
                    }
                    2 -> FeatureSection(stringResource(R.string.design_wrapped_words), icon = LeafIcons.EditNote) {
                        AppTextField(customTitle, { customTitle = it.take(120) }, stringResource(R.string.wrapped_title), Modifier.fillMaxWidth())
                        AppTextField(caption, { caption = it.take(70) }, stringResource(R.string.wrapped_caption),
                            Modifier.fillMaxWidth(), singleLine = false, minLines = 2, maxLines = 3,
                            supportingText = "${caption.length}/70")
                        Text(stringResource(R.string.b9_wrap_caption_ideas), style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(R.string.b9_wrap_idea_1, R.string.b9_wrap_idea_2, R.string.b9_wrap_idea_3, R.string.b9_wrap_idea_4).forEach { res ->
                                val idea = stringResource(res)
                                FeatureChoice(caption == idea, idea, { caption = idea }, enabled = !busy)
                            }
                        }
                    }
                    else -> {
                        FeatureSection(stringResource(R.string.b9_wrap_tab_format), icon = LeafIcons.Widgets) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WrappedFormat.entries.forEach { option ->
                                    val active = format == option
                                    com.example.fitnesstracker.ui.theme.LeafSurface(onClick = { format = option }, enabled = !busy,
                                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(if (active) 28.dp else 16.dp),
                                        color = if (active) colors.primaryContainer else colors.surfaceContainerHigh) {
                                        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            val ratio = when (option) { WrappedFormat.STORY -> 9f / 16f; WrappedFormat.POST -> 4f / 5f
                                                WrappedFormat.SQUARE -> 1f; WrappedFormat.AUTO -> 3f / 4f }
                                            Box(Modifier.height(40.dp).aspectRatio(ratio).clip(RoundedCornerShape(6.dp))
                                                .background(if (active) colors.primary else colors.outline))
                                            LeafFitText(stringResource(when (option) {
                                                WrappedFormat.AUTO -> R.string.b9_wrap_format_auto; WrappedFormat.STORY -> R.string.b9_wrap_format_story
                                                WrappedFormat.POST -> R.string.b9_wrap_format_post; WrappedFormat.SQUARE -> R.string.b9_wrap_format_square
                                            }), style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                        FeatureSection(stringResource(R.string.b9_wrap_sticker), icon = LeafIcons.Sell) {
                            WrappedToggle(stringResource(R.string.b9_wrap_sticker), sticker, !busy) { sticker = it }
                            Text(stringResource(R.string.b9_wrap_sticker_hint), style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant)
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(enabled = !busy, onClick = {
                        preferences.edit().putString("style", style.name).putString("layout", layoutStyle.name)
                            .putString("route", mode.name).putString("hero", heroMetric.name).putBoolean("dark", dark)
                            .putBoolean("stats", showStats).putBoolean("date", showDate).putBoolean("exercises", showExercises)
                            .putBoolean("energy", showEnergy).putBoolean("insights", showInsights)
                            .putString("format", format.name).putBoolean("sticker", sticker).apply()
                        android.widget.Toast.makeText(context, R.string.b5_wrapped_saved, android.widget.Toast.LENGTH_SHORT).show()
                    }) { Text(stringResource(R.string.b5_wrapped_save)) }
                    TextButton(enabled = !busy, onClick = {
                        style = WrappedStyle.MINT; layoutStyle = WrappedLayout.POSTER; mode = RouteCardMode.ROUTE
                        heroMetric = WrappedHero.AUTO; dark = systemDark; showStats = true; showDate = true; showInsights = true
                        showExercises = true; showEnergy = true; customTitle = ""; caption = ""
                        format = WrappedFormat.AUTO; sticker = false
                    }) { Text(stringResource(R.string.b5_wrapped_reset)) }
                }
            }
            if (data != null) Surface(color = colors.surfaceContainer) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (android.os.Build.VERSION.SDK_INT >= 29) LeafTonalButton(enabled = !busy, onClick = { export(save = true) },
                        modifier = Modifier.height(56.dp)) {
                        Icon(LeafIcons.FileDownload, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.b9_wrap_save))
                    }
                    LeafButton(enabled = !busy, onClick = { export(save = false) }, modifier = Modifier.weight(1f).height(56.dp)) {
                        if (busy) LeafLoadingIndicator(Modifier.size(24.dp), colors.onPrimary)
                        else {
                            Icon(LeafIcons.Upload, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.b9_wrap_share))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicatorShape() {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "wrapLoad")
    val angle by transition.animateFloat(0f, 360f, androidx.compose.animation.core.infiniteRepeatable(
        androidx.compose.animation.core.tween(2400, easing = androidx.compose.animation.core.LinearEasing)), label = "angle")
    Box(Modifier.size(56.dp).graphicsLayer { rotationZ = angle }.clip(LeafRosette())
        .background(MaterialTheme.colorScheme.primary))
}

private fun wrappedInsights(context: Context, workout: com.example.fitnesstracker.data.entities.Workout,
    all: List<com.example.fitnesstracker.data.entities.Workout>): List<String> {
    val ctx = LanguagePreferences.localizedContext(context)
    val zone = java.time.ZoneId.systemDefault()
    fun day(ms: Long) = java.time.Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
    val sameType = all.filter { it.type == workout.type }
    val previous = sameType.filter { it.id != workout.id && it.startTime < workout.startTime }.maxByOrNull { it.startTime }
    return buildList {
        if (previous != null) {
            val useDistance = workout.distanceMeters > 0 && previous.distanceMeters > 0
            val now = if (useDistance) workout.distanceMeters.toDouble() else workout.durationSeconds.toDouble()
            val before = if (useDistance) previous.distanceMeters.toDouble() else previous.durationSeconds.toDouble()
            if (before > 0) {
                val delta = ((now - before) / before * 100).roundToInt()
                if (delta != 0) add(ctx.getString(R.string.b9_wrap_vs_last, (if (delta > 0) "+" else "−") + kotlin.math.abs(delta) + "%"))
            }
        }
        if (sameType.size >= 3) {
            if (workout.durationSeconds > 0 && sameType.maxOf { it.durationSeconds } == workout.durationSeconds)
                add(ctx.getString(R.string.b9_wrap_record_duration))
            if (workout.distanceMeters > 0 && sameType.maxOf { it.distanceMeters } == workout.distanceMeters)
                add(ctx.getString(R.string.b9_wrap_record_distance))
        }
        val date = day(workout.startTime)
        val monday = date.minusDays((date.dayOfWeek.value - 1).toLong())
        val inWeek = all.count { val d = day(it.startTime); !d.isBefore(monday) && it.startTime <= workout.startTime }
        if (inWeek >= 2) add(ctx.getString(R.string.b9_wrap_week_count, inWeek))
        val days = all.map { day(it.startTime) }.toSet()
        var streak = 0
        var cursor = date
        while (cursor in days) { streak++; cursor = cursor.minusDays(1) }
        if (streak >= 2) add(ctx.getString(R.string.b9_wrap_streak, streak))
    }
}

private fun saveToGallery(context: Context, image: Bitmap): Boolean {
    if (android.os.Build.VERSION.SDK_INT < 29) return false
    val resolver = context.contentResolver
    val values = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "leafit_${System.currentTimeMillis()}.png")
        put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Leafit")
        put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        resolver.openOutputStream(uri)?.use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) } ?: return false
        values.clear(); values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        true
    } catch (_: Exception) { resolver.delete(uri, null, null); false }
}

@Composable
private fun WrappedToggle(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        LeafSwitch(checked, onChange, enabled = enabled)
    }
}
