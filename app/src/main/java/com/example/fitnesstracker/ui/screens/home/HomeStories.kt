package com.example.fitnesstracker.ui.screens.home

import com.example.fitnesstracker.ui.components.LeafIconButton as IconButton
import com.example.fitnesstracker.ui.components.LeafTextButton as TextButton

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.*
import com.example.fitnesstracker.ui.components.*
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.*
import java.io.File
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgressStoryCard(store: LifeStore, embedded: Boolean = false) {
    val state by store.state.collectAsLeafState()
    var editor by rememberSaveable { mutableStateOf(false) }
    var gallery by rememberSaveable { mutableStateOf(false) }
    var compare by rememberSaveable { mutableStateOf(false) }
    var chest by rememberSaveable { mutableStateOf("") }
    var waist by rememberSaveable { mutableStateOf("") }
    var hips by rememberSaveable { mutableStateOf("") }
    var bicep by rememberSaveable { mutableStateOf("") }
    var customName by rememberSaveable { mutableStateOf("") }
    var customValue by rememberSaveable { mutableStateOf("") }
    var custom by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.listSaver<Map<String, Float>, Any>(
        save = { values -> values.flatMap { listOf(it.key, it.value) } },
        restore = { values -> values.chunked(2).associate { (it[0] as String) to (it[1] as Float) } }
    )) { mutableStateOf(emptyMap<String, Float>()) }
    var date by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var pickDate by rememberSaveable { mutableStateOf(false) }
    var selectedPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            importing = true
            val previous = selectedPhoto
            val imported = withContext(Dispatchers.IO) { store.importPhoto(uri) }
            if (editor && imported != null) {
                selectedPhoto = imported
                withContext(Dispatchers.IO) { store.discardPhoto(previous) }
            } else if (imported != null) withContext(Dispatchers.IO) { store.discardPhoto(imported) }
            photoError = imported == null
            importing = false
        }
    }
    val photos = remember(state.progress) {
        state.progress.filter { it.photo != null && File(it.photo).isFile }.sortedBy { it.date }
    }
    fun measure(value: String) = value.replace(',', '.').toFloatOrNull()?.takeIf { it.isFinite() && it in 1f..300f }
    val fields = listOf(chest, waist, hips, bicep)
    val valid = fields.all { it.isBlank() || measure(it) != null } &&
        (selectedPhoto != null || fields.any { measure(it) != null } || custom.isNotEmpty())
    LeafSurface(shape = RoundedCornerShape(28.dp), color = if (embedded) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(LeafIcons.MonitorWeight, null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.progress_story), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { date = System.currentTimeMillis(); editor = true }) {
                    Icon(LeafIcons.Add, stringResource(R.string.progress_new))
                }
            }
            if (state.progress.isEmpty()) Text(stringResource(R.string.design_progress_empty),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else {
                val latest = state.progress.maxByOrNull { it.date }
                latest?.photo?.let { photo ->
                    AsyncImage(File(photo), null, Modifier.fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop)
                }
                Text(stringResource(R.string.progress_story_count, state.progress.size), style = MaterialTheme.typography.titleMedium)
                latest?.let { check ->
                    Text(DateUtils.formatDate(check.date), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val measurements = listOfNotNull(
                        check.chest?.let { stringResource(R.string.progress_chest) to it },
                        check.waist?.let { stringResource(R.string.progress_waist) to it },
                        check.hips?.let { stringResource(R.string.progress_hips) to it },
                        check.bicep?.let { stringResource(R.string.progress_bicep) to it }
                    ) + check.measurements.toList()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        measurements.take(4).forEach { (label, value) ->
                            LeafSurface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Column(Modifier.widthIn(min = 120.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.progress.isNotEmpty()) TextButton(onClick = { gallery = true }) { Text(stringResource(R.string.progress_history)) }
                if (photos.size >= 2) TextButton(onClick = { compare = true }) { Text(stringResource(R.string.progress_compare)) }
            }
        }
    }
    fun cancelEditor() {
        store.discardPhoto(selectedPhoto)
        selectedPhoto = null; editor = false; photoError = false
    }
    if (pickDate) LeafDatePicker(date, onSelect = { date = it; pickDate = false }, onDismiss = { pickDate = false })
    if (editor && !pickDate) LeafEditSheet(onDismissRequest = { cancelEditor() }, title = { Text(stringResource(R.string.progress_new)) },
        content = {
            TextButton(onClick = { pickDate = true }) { Text(DateUtils.formatDate(date)) }
            FeatureSection(stringResource(R.string.design_progress_measurements)) {
            AppTextField(chest, { chest = it }, stringResource(R.string.progress_chest), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal,
                isError = chest.isNotBlank() && measure(chest) == null)
            AppTextField(waist, { waist = it }, stringResource(R.string.progress_waist), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal,
                isError = waist.isNotBlank() && measure(waist) == null)
            AppTextField(hips, { hips = it }, stringResource(R.string.progress_hips), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal,
                isError = hips.isNotBlank() && measure(hips) == null)
            AppTextField(bicep, { bicep = it }, stringResource(R.string.progress_bicep), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal,
                isError = bicep.isNotBlank() && measure(bicep) == null)
            }
            FeatureSection(stringResource(R.string.design_progress_custom)) {
            custom.forEach { (name, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$name · $value", Modifier.weight(1f))
                    IconButton(onClick = { custom = custom - name }) { Icon(LeafIcons.Close, stringResource(R.string.common_delete)) }
                }
            }
            AppTextField(customName, { customName = it.take(80) }, stringResource(R.string.progress_custom_name), Modifier.fillMaxWidth())
            AppTextField(customValue, { customValue = it }, stringResource(R.string.progress_custom_value), Modifier.fillMaxWidth(), keyboardType = KeyboardType.Decimal)
            TextButton(enabled = customName.isNotBlank() && measure(customValue) != null, onClick = {
                custom = custom + (customName.trim() to measure(customValue)!!)
                customName = ""; customValue = ""
            }) { Text(stringResource(R.string.progress_custom_add)) }
            }
            LeafTonalButton(modifier = Modifier.fillMaxWidth(), enabled = !importing, onClick = { picker.launch("image/*") }) {
                Icon(LeafIcons.Add, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.progress_photo))
            }
            if (importing) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { LeafLoadingIndicator() }
            if (photoError) Text(stringResource(R.string.share_error), color = MaterialTheme.colorScheme.error)
            selectedPhoto?.let { AsyncImage(File(it), null, Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Fit) }
            if (!valid) Text(stringResource(R.string.progress_invalid), style = MaterialTheme.typography.bodySmall)
        },
        confirmButton = { LeafButton(enabled = valid && !importing, onClick = {
            store.update { it.copy(progress = it.progress + ProgressCheck(date = date, chest = measure(chest),
                waist = measure(waist), hips = measure(hips), bicep = measure(bicep), photo = selectedPhoto, measurements = custom)) }
            editor = false; chest = ""; waist = ""; hips = ""; bicep = ""; selectedPhoto = null; custom = emptyMap()
        }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = { cancelEditor() }) { Text(stringResource(R.string.common_cancel)) } })
    if (gallery) LeafEditSheet(onDismissRequest = { gallery = false }, title = { Text(stringResource(R.string.progress_history)) },
        content = {
            var visible by rememberSaveable { mutableIntStateOf(20) }
            state.progress.sortedByDescending { it.date }.take(visible).forEach { p ->
                key(p.id) {
                    LeafSurface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(DateUtils.formatDate(p.date), style = MaterialTheme.typography.titleMedium)
                            p.photo?.takeIf { File(it).isFile }?.let { AsyncImage(File(it), null,
                                Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Fit) }
                            Text((listOfNotNull(
                                p.chest?.let { stringResource(R.string.progress_chest) + " " + it },
                                p.waist?.let { stringResource(R.string.progress_waist) + " " + it },
                                p.hips?.let { stringResource(R.string.progress_hips) + " " + it },
                                p.bicep?.let { stringResource(R.string.progress_bicep) + " " + it }
                            ) + p.measurements.map { "${it.key} · ${it.value}" }).joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { store.removeProgress(p.id) }) { Text(stringResource(R.string.common_delete)) }
                        }
                    }
                }
            }
            if (visible < state.progress.size) TextButton(onClick = { visible += 20 }) { Text(stringResource(R.string.common_show_more)) }
        }, confirmButton = { TextButton(onClick = { gallery = false }) { Text(stringResource(R.string.common_close)) } })
    if (compare && photos.size >= 2) {
        var firstId by rememberSaveable { mutableStateOf(photos.first().id) }
        var secondId by rememberSaveable { mutableStateOf(photos.last().id) }
        val first = photos.firstOrNull { it.id == firstId } ?: photos.first()
        val second = photos.firstOrNull { it.id == secondId } ?: photos.last()
        var fraction by rememberSaveable { mutableFloatStateOf(.5f) }
        var sideBySide by rememberSaveable { mutableStateOf(false) }
        LeafEditSheet(onDismissRequest = { compare = false }, title = { Text(stringResource(R.string.progress_compare)) },
            content = {
                PhotoDateSelector(stringResource(R.string.progress_before), first, photos.filter { it.id != second.id }) { firstId = it.id }
                PhotoDateSelector(stringResource(R.string.progress_after), second, photos.filter { it.id != first.id }) { secondId = it.id }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!sideBySide, { sideBySide = false }, label = { Text(stringResource(R.string.progress_slider)) })
                    FilterChip(sideBySide, { sideBySide = true }, label = { Text(stringResource(R.string.progress_side_by_side)) })
                }
                if (sideBySide) Row(Modifier.fillMaxWidth().height(300.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(File(first.photo!!), null, Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Fit)
                    AsyncImage(File(second.photo!!), null, Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Fit)
                } else {
                    Box(Modifier.fillMaxWidth().height(320.dp)) {
                        AsyncImage(File(first.photo!!), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        AsyncImage(File(second.photo!!), null, Modifier.fillMaxSize().drawWithContent {
                            val contentScope = this
                            clipRect(right = size.width * fraction) { contentScope.drawContent() }
                        }, contentScale = ContentScale.Fit)
                        Canvas(Modifier.fillMaxSize()) {
                            drawLine(androidx.compose.ui.graphics.Color.White,
                                androidx.compose.ui.geometry.Offset(size.width * fraction, 0f),
                                androidx.compose.ui.geometry.Offset(size.width * fraction, size.height), 3.dp.toPx())
                        }
                    }
                    Slider(fraction, { fraction = it })
                }
            }, confirmButton = { TextButton(onClick = { compare = false }) { Text(stringResource(R.string.common_close)) } })
    }
}

@Composable
private fun PhotoDateSelector(label: String, selected: ProgressCheck, choices: List<ProgressCheck>, onSelect: (ProgressCheck) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) { Text(label + " · " + DateUtils.formatDate(selected.date)) }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { entry -> DropdownMenuItem(text = { Text(DateUtils.formatDate(entry.date)) },
                onClick = { onSelect(entry); expanded = false }) }
        }
    }
}

