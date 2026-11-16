package com.example.fitnesstracker.ui.screens.home

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
internal fun ChallengeVisibilitySettings() {
    val context = LocalContext.current
    val store = remember(context) { LifeStore.get(context) }
    val state by store.state.collectAsLeafState()
    FeatureSection(stringResource(R.string.challenges_title), subtitle = stringResource(R.string.b5_ch_settings_hint),
        icon = LeafIcons.Flag, tone = com.example.fitnesstracker.ui.components.LeafTone.Secondary) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.b5_ch_show), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            LeafSwitch(!state.challengesHidden, { show -> store.update { it.copy(challengesHidden = !show) } })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.b5_ch_compact), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            LeafSwitch(state.challengesCompact, { compact -> store.update { it.copy(challengesCompact = compact) } })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChallengeStoryCard(store: LifeStore) {
    val state by store.state.collectAsLeafState()
    val context = LocalContext.current
    val db = remember(context) { AppDatabase.getInstance(context) }
    val today by currentDayFlow().collectAsLeafState(DateUtils.todayStart())
    val since = remember(today, state.challengeRuns) {
        minOf(Instant.ofEpochMilli(today).atZone(ZoneId.systemDefault()).minusDays(90).toInstant().toEpochMilli(),
            state.challengeRuns.minOfOrNull { it.start } ?: today)
    }
    val workouts by remember(db, since) { db.workoutDao().workoutsThisWeek(since) }.collectAsLeafState(emptyList())
    val steps by remember(db, since) { db.stepDao().getSince(since) }.collectAsLeafState(emptyList())
    val sleeps by remember(db) { db.sleepDao().getAll() }.collectAsLeafState(emptyList())
    val water by remember(db, since) { db.waterDao().getSince(since) }.collectAsLeafState(emptyList())
    val meals by remember(db, since) { db.mealDao().getSince(since) }.collectAsLeafState(emptyList())
    val notes by remember(db) { db.extrasDao().getAllNotes() }.collectAsLeafState(emptyList())
    val now = System.currentTimeMillis()
    val progress = remember(state.challengeRuns, steps, workouts, sleeps, water, meals, notes, today) {
        state.challengeRuns.associate { it.id to ChallengeCatalog.progress(it, steps, workouts, sleeps, water, meals, notes, now) }
    }
    var page by rememberSaveable { mutableStateOf("") }
    var chosen by rememberSaveable { mutableStateOf("first") }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableIntStateOf(-1) }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(state.challengeId) {
        val legacy = ChallengeCatalog.find(state.challengeId)
        if (legacy != null) store.update { old -> old.copy(challengeId = null,
            challengeRuns = old.challengeRuns + ChallengeRun(kind = legacy.id, metric = legacy.metric,
                start = old.challengeStart, goal = legacy.goal, threshold = legacy.threshold)) }
    }
    LaunchedEffect(progress) {
        store.update { old ->
            val completed = old.challengeRuns.filter { (progress[it.id] ?: 0) >= it.goal &&
                old.completedChallenges.none { done -> done.runId == it.id || (done.runId.isBlank() && done.kind == it.kind && done.week == it.start) } }
            if (completed.isEmpty()) old else old.copy(completedChallenges = old.completedChallenges + completed.map {
                FinishedChallenge(it.kind, it.start, System.currentTimeMillis(), it.goal, it.goal, it.title, it.id)
            })
        }
    }
    fun share(title: String, value: Int, goal: Int, detail: String, start: Long) {
        if (sharing) return
        sharing = true
        scope.launch {
            try { ChallengeWrapped.share(context, title, value, goal, detail, start) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { android.widget.Toast.makeText(context, R.string.share_error, android.widget.Toast.LENGTH_LONG).show() }
            finally { sharing = false }
        }
    }
    LeafSurface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureBadge(LeafIcons.Star)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.challenges_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.b5_ch_hub_count, ChallengeCatalog.entries.size, state.challengeRuns.size),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { page = "settings" }) { Icon(LeafIcons.Tune, stringResource(R.string.b5_ch_settings)) }
            }
            if (state.challengeRuns.isEmpty()) Text(stringResource(R.string.design_challenge_intro), style = MaterialTheme.typography.bodyMedium)
            state.challengeRuns.forEach { run ->
                val value = progress[run.id] ?: 0
                val title = run.title.ifBlank { context.getString(ChallengeCatalog.find(run.kind)?.title ?: R.string.b5_ch_own) }
                val ended = now >= ChallengeCatalog.end(run)
                val colors = MaterialTheme.colorScheme
                val tone = when(run.tone) { 1 -> colors.secondaryContainer; 2 -> colors.tertiaryContainer; else -> colors.primaryContainer }
                LeafSurface(shape = RoundedCornerShape(22.dp), color = tone, contentColor = contentColorFor(tone)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.challenge_progress, value, run.goal), style = MaterialTheme.typography.titleMedium)
                            Text("${(value * 100L / run.goal)}%", style = MaterialTheme.typography.labelLarge)
                        }
                        LeafWavyProgress(value.toFloat() / run.goal.coerceAtLeast(1), true, Modifier.fillMaxWidth().height(16.dp),
                            when(run.tone) { 1 -> colors.secondary; 2 -> colors.tertiary; else -> colors.primary },
                            colors.surface.copy(alpha = .65f))
                        if (!state.challengesCompact) {
                            Text(stringResource(ChallengeCatalog.label(run.metric)) + if (run.threshold > 0) " · ${run.threshold}" else "",
                                style = MaterialTheme.typography.bodySmall)
                            Text(DateUtils.formatDate(run.start) + " — " + DateUtils.formatDate(ChallengeCatalog.end(run) - 1),
                                style = MaterialTheme.typography.labelSmall)
                        }
                        if (value >= run.goal) Text(stringResource(R.string.challenge_completed), style = MaterialTheme.typography.labelLarge)
                        else if (ended) Text(stringResource(R.string.b5_ch_expired), style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (run.metric == "manual" && !ended) {
                                IconButton(enabled = run.manual > 0, onClick = { store.update { old -> old.copy(challengeRuns = old.challengeRuns.map { if (it.id == run.id) it.copy(manual = (it.manual - 1).coerceAtLeast(0)) else it },
                                    completedChallenges = old.completedChallenges.filterNot { it.runId == run.id }) } }) {
                                    Icon(LeafIcons.Remove, stringResource(R.string.b5_ch_minus))
                                }
                                LeafTonalButton(enabled = value < run.goal, onClick = { store.update { old -> old.copy(challengeRuns = old.challengeRuns.map { if (it.id == run.id) it.copy(manual = (it.manual + 1).coerceAtMost(it.goal)) else it }) } }) {
                                    Text(stringResource(R.string.b5_ch_checkin))
                                }
                            }
                            TextButton(onClick = { chosen = run.kind; editing = run.id; page = "editor" }) { Text(stringResource(R.string.common_edit)) }
                            IconButton(enabled = !sharing, onClick = { share(title, value, run.goal, context.getString(ChallengeCatalog.label(run.metric)), run.start) }) {
                                Icon(LeafIcons.Upload, stringResource(R.string.challenge_share))
                            }
                            IconButton(onClick = { store.update { old -> old.copy(challengeRuns = old.challengeRuns.filterNot { it.id == run.id }) } }) {
                                Icon(LeafIcons.Close, stringResource(R.string.challenge_cancel))
                            }
                        }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeafTonalButton(onClick = { page = "catalog" }) { Text(stringResource(R.string.b5_ch_catalog)) }
                if (state.completedChallenges.isNotEmpty()) TextButton(onClick = { page = "finished" }) { Text(stringResource(R.string.challenge_finished)) }
            }
        }
    }
    when(page) {
        "settings" -> LeafEditSheet(onDismissRequest = { page = "" }, title = { Text(stringResource(R.string.b5_ch_settings)) }, content = {
            ChallengeVisibilitySettings()
            Text(stringResource(R.string.b5_ch_restore), style = MaterialTheme.typography.bodySmall)
        }, confirmButton = { TextButton(onClick = { page = "" }) { Text(stringResource(R.string.common_close)) } })
        "catalog" -> LeafEditSheet(onDismissRequest = { page = "" }, title = { Text(stringResource(R.string.b5_ch_catalog)) }, content = {
            AppSearchBar(query, { query = it }, stringResource(R.string.b5_ch_search), Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(R.string.b5_all, R.string.b5_walks, R.string.b5_training, R.string.b5_recovery, R.string.b5_nutrition, R.string.b5_habits).forEachIndexed { index, label ->
                    FeatureChoice(category == index - 1, stringResource(label), { category = index - 1 })
                }
                FeatureChoice(favorites, stringResource(R.string.catalog_favorites), { favorites = !favorites })
            }
            Text(stringResource(R.string.b5_ch_rules), style = MaterialTheme.typography.bodySmall)
            val filtered = ChallengeCatalog.entries.filter { (category < 0 || it.category == category) &&
                (!favorites || it.id in state.challengeFavorites) && context.getString(it.title).contains(query, true) }
            if (filtered.isEmpty()) Text(stringResource(R.string.b5_no_results), style = MaterialTheme.typography.bodyMedium)
            filtered.forEach { spec ->
                LeafSurface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(spec.title), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { store.update { it.copy(challengeFavorites = if (spec.id in it.challengeFavorites) it.challengeFavorites - spec.id else it.challengeFavorites + spec.id) } }) {
                                Icon(if (spec.id in state.challengeFavorites) LeafIcons.Star else LeafIcons.StarOutline, stringResource(R.string.catalog_favorite))
                            }
                        }
                        Text(stringResource(ChallengeCatalog.label(spec.metric)), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.b5_ch_goal_period, spec.goal, spec.days) + if (spec.threshold > 0) " · ${spec.threshold}" else "",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LeafTonalButton(enabled = state.challengeRuns.size < 3, onClick = { chosen = spec.id; editing = null; page = "editor" }) {
                            Text(stringResource(R.string.b5_ch_customize))
                        }
                    }
                }
            }
            if (state.challengeRuns.size >= 3) Text(stringResource(R.string.b5_ch_limit), style = MaterialTheme.typography.bodyMedium)
        }, confirmButton = { TextButton(onClick = { page = "" }) { Text(stringResource(R.string.common_close)) } })
        "editor" -> ChallengeEditor(ChallengeCatalog.find(chosen) ?: ChallengeCatalog.entries.last(),
            state.challengeRuns.firstOrNull { it.id == editing }, onClose = { page = "catalog" }, onSave = { run ->
                store.update { old -> old.copy(challengeRuns = old.challengeRuns.filterNot { it.id == run.id } + run) }; page = ""
            })
        "finished" -> LeafEditSheet(onDismissRequest = { page = "" }, title = { Text(stringResource(R.string.challenge_finished)) }, content = {
            state.completedChallenges.sortedByDescending { it.completedAt }.forEach { done ->
                val title = done.title.ifBlank { context.getString(ChallengeCatalog.find(done.kind)?.title ?: R.string.b5_ch_own) }
                FeatureSection(title, subtitle = DateUtils.formatDate(done.completedAt)) {
                    Text(stringResource(R.string.challenge_progress, done.progress, done.goal), style = MaterialTheme.typography.titleLarge)
                    LeafTonalButton(enabled = !sharing, onClick = { share(title, done.progress, done.goal, context.getString(R.string.challenge_completed), done.week) }) {
                        Text(stringResource(R.string.challenge_share))
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { page = "" }) { Text(stringResource(R.string.common_close)) } })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChallengeEditor(spec: ChallengeSpec, current: ChallengeRun?, onClose: () -> Unit, onSave: (ChallengeRun) -> Unit) {
    val context = LocalContext.current
    var title by rememberSaveable(spec.id, current?.id) { mutableStateOf(current?.title?.takeIf { it.isNotBlank() } ?: context.getString(spec.title)) }
    var metric by rememberSaveable(spec.id, current?.id) { mutableStateOf(current?.metric ?: spec.metric) }
    var goal by rememberSaveable(spec.id, current?.id) { mutableStateOf((current?.goal ?: spec.goal).toString()) }
    var threshold by rememberSaveable(spec.id, current?.id) { mutableStateOf((current?.threshold ?: spec.threshold).toString()) }
    var days by rememberSaveable(spec.id, current?.id) { mutableIntStateOf(current?.days ?: spec.days) }
    var tone by rememberSaveable(spec.id, current?.id) { mutableIntStateOf(current?.tone ?: 0) }
    val needsThreshold = metric in listOf("step_days", "sleep_goal", "water_days")
    val target = goal.toIntOrNull()
    val thresholdValue = threshold.toIntOrNull()
    val dayCount = metric in listOf("step_days", "workout_days", "sleep_days", "sleep_goal", "water_days", "meal_days", "note_days")
    val valid = title.isNotBlank() && target != null && target in 1..1000000 && (!dayCount || target <= days) &&
        (!needsThreshold || thresholdValue != null && thresholdValue in 1..100000)
    LeafEditSheet(onDismissRequest = onClose, title = { Text(stringResource(R.string.b5_ch_customize)) }, content = {
        FeatureSection(stringResource(R.string.b5_ch_goal)) {
            AppTextField(title, { title = it.take(80) }, stringResource(R.string.b5_ch_name), Modifier.fillMaxWidth())
            if (current == null && spec.id == "own") FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChallengeCatalog.metrics.forEach { key -> FeatureChoice(metric == key, stringResource(ChallengeCatalog.label(key)), { metric = key }) }
            } else Text(stringResource(ChallengeCatalog.label(metric)), style = MaterialTheme.typography.bodyMedium)
            AppTextField(goal, { goal = it.filter(Char::isDigit).take(7) }, stringResource(R.string.b5_ch_target), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            if (needsThreshold) AppTextField(threshold, { threshold = it.filter(Char::isDigit).take(6) },
                stringResource(when(metric) { "water_days" -> R.string.b5_threshold_water; "sleep_goal" -> R.string.b5_threshold_sleep; else -> R.string.b5_threshold_steps }),
                Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 14, 30).forEach { count -> FeatureChoice(days == count, stringResource(R.string.b5_days, count), { days = count }) }
            }
            Text(stringResource(R.string.b5_ch_rules), style = MaterialTheme.typography.bodySmall)
            if (dayCount && target != null && target > days) Text(stringResource(R.string.b5_ch_days_invalid), color = MaterialTheme.colorScheme.error)
        }
        FeatureSection(stringResource(R.string.b5_appearance)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(R.string.wrapped_mint, R.string.b5_sage, R.string.wrapped_lavender).forEachIndexed { index, label ->
                    FeatureChoice(tone == index, stringResource(label), { tone = index })
                }
            }
        }
    }, confirmButton = { LeafButton(enabled = valid, onClick = {
        onSave(ChallengeRun(id = current?.id ?: java.util.UUID.randomUUID().toString(), kind = spec.id, title = title.trim(), metric = metric,
            start = current?.start ?: DateUtils.todayStart(), days = days, goal = target!!,
            threshold = if (needsThreshold) thresholdValue!! else 0, tone = tone, manual = current?.manual ?: 0))
    }) { Text(stringResource(R.string.common_save)) } }, dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.common_cancel)) } })
}
