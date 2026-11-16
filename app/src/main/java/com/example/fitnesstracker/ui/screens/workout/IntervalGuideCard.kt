package com.example.fitnesstracker.ui.screens.workout

import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.viewmodel.WorkoutViewModel
import com.example.fitnesstracker.workout.WorkoutPlan

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IntervalGuideCard(vm: WorkoutViewModel, plan: WorkoutPlan, running: Boolean, paused: Boolean) {
    val guide by vm.interval.collectAsLeafState()
    var configure by rememberSaveable { mutableStateOf(false) }
    var work by rememberSaveable { mutableStateOf("45") }
    var rest by rememberSaveable { mutableStateOf("15") }
    var rounds by rememberSaveable { mutableStateOf("3") }
    LeafSurface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeatureBadge(LeafIcons.Timer)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.interval_title), Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium)
            }
            if (guide == null) {
                Text(stringResource(R.string.interval_hint), style = MaterialTheme.typography.bodyMedium)
                LeafTonalButton(enabled = running && !paused && plan.exercises.isNotEmpty(),
                    onClick = { configure = true }) { Text(stringResource(R.string.interval_configure)) }
            } else {
                AnimatedContent(guide?.phase, transitionSpec = { fadeIn(spring()) togetherWith fadeOut() },
                    label = "intervalPhase") { phase ->
                    Column {
                        Text(stringResource(when (phase) {
                            "work" -> R.string.interval_work
                            "rest" -> R.string.interval_rest
                            else -> R.string.interval_complete
                        }), style = MaterialTheme.typography.titleMedium)
                        if (phase != "done") Text(plan.exercises.getOrNull(guide?.exerciseIndex ?: 0)?.name.orEmpty(),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
                Text((guide?.remaining ?: 0).toString() + " " + stringResource(R.string.journal_seconds),
                    style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.interval_round, guide?.round ?: 1, guide?.rounds ?: 1),
                    style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (guide?.phase != "done") LeafTonalButton(enabled = !paused,
                        onClick = vm::skipIntervalPhase) { Text(stringResource(R.string.timer_skip)) }
                    TextButton(onClick = vm::stopInterval) { Text(stringResource(R.string.interval_stop)) }
                }
            }
        }
    }
    if (configure) LeafEditSheet(onDismissRequest = { configure = false },
        title = { Text(stringResource(R.string.interval_configure)) },
        content = {
            FeatureSection(stringResource(R.string.interval_title), icon = com.example.fitnesstracker.ui.components.LeafIcons.TimerPlay) {
            AppTextField(work, { work = it.filter(Char::isDigit).take(4) }, stringResource(R.string.interval_work_seconds),
                Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            AppTextField(rest, { rest = it.filter(Char::isDigit).take(4) }, stringResource(R.string.interval_rest_seconds),
                Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            AppTextField(rounds, { rounds = it.filter(Char::isDigit).take(2) }, stringResource(R.string.interval_rounds),
                Modifier.fillMaxWidth(), keyboardType = KeyboardType.Number)
            }
            Text(stringResource(R.string.interval_record_hint), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.interval_sequence, plan.exercises.joinToString(" → ") { it.name }),
                style = MaterialTheme.typography.bodyMedium)
        }, confirmButton = { LeafButton(enabled = (work.toIntOrNull() ?: 0) in 5..3600 &&
            (rest.toIntOrNull() ?: -1) in 0..1800 && (rounds.toIntOrNull() ?: 0) in 1..20,
            onClick = {
                vm.startInterval(work.toInt(), rest.toInt(), rounds.toInt())
                configure = false
            }) { Text(stringResource(R.string.interval_start)) } },
        dismissButton = { TextButton(onClick = { configure = false }) { Text(stringResource(R.string.common_cancel)) } })
}
