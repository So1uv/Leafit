package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SleepTagPicker(selectedTags: Set<String>, onChange: (Set<String>) -> Unit) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("sleep_tags", android.content.Context.MODE_PRIVATE) }
    var custom by remember { mutableStateOf(prefs.getStringSet("custom", emptySet()).orEmpty().toSet()) }
    val defaults = listOf(stringResource(R.string.sleep_tag_good), stringResource(R.string.sleep_tag_wakeups),
        stringResource(R.string.sleep_tag_snoring), stringResource(R.string.sleep_tag_nightmares), stringResource(R.string.sleep_tag_insomnia))
    var choosing by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeafSurface(shape = RoundedCornerShape(14.dp), color = colors.tertiaryContainer) {
                Box(Modifier.size(38.dp), Alignment.Center) { Icon(LeafIcons.Label, null, tint = colors.onTertiaryContainer, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.sleep_tags), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.sleep_tag_selected_count, selectedTags.size),
                    style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = { choosing = true }) {
                Icon(LeafIcons.Tune, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.sleep_tags_choose))
            }
        }
        if (selectedTags.isEmpty()) LeafSurface(onClick = { choosing = true }, shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerHigh) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(LeafIcons.Add, null, tint = colors.tertiary)
                Text(stringResource(R.string.sleep_tags_empty_hint), style = MaterialTheme.typography.bodyMedium)
            }
        } else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedTags.forEach { tag ->
                InputChip(selected = true, onClick = { choosing = true }, shape = CircleShape, border = null,
                    colors = InputChipDefaults.inputChipColors(selectedContainerColor = colors.tertiaryContainer,
                        selectedLabelColor = colors.onTertiaryContainer, selectedLeadingIconColor = colors.onTertiaryContainer),
                    label = { Text(tag, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(LeafIcons.Label, null, Modifier.size(16.dp)) })
            }
        }
    }
    if (choosing) {
        var draft by remember { mutableStateOf(selectedTags) }
        var query by rememberSaveable { mutableStateOf("") }
        var newTag by rememberSaveable { mutableStateOf("") }
        var creating by rememberSaveable { mutableStateOf(false) }
        val all = (defaults + custom.sorted() + draft).distinct()
        val shown = all.filter { it.contains(query.trim(), ignoreCase = true) }
        LeafEditSheet(onDismissRequest = { choosing = false },
            title = { Text(stringResource(R.string.sleep_tags)) },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppSearchBar(query, { query = it }, stringResource(R.string.sleep_tags_search), Modifier.fillMaxWidth())
                    shown.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { tag ->
                                val checked = tag in draft
                                LeafSurface(onClick = { draft = if (checked) draft - tag else draft + tag },
                                    modifier = Modifier.weight(1f).semantics { selected = checked; role = Role.Checkbox },
                                    shape = RoundedCornerShape(if (checked) 20.dp else 28.dp),
                                    color = if (checked) colors.tertiaryContainer else colors.surfaceContainerHigh,
                                    contentColor = if (checked) colors.onTertiaryContainer else colors.onSurface) {
                                    Column(Modifier.heightIn(min = 92.dp).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(if (checked) LeafIcons.CheckCircle else LeafIcons.Label, null,
                                            Modifier.size(22.dp), tint = colors.tertiary)
                                        Text(tag, style = MaterialTheme.typography.labelLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (shown.isEmpty()) Text(stringResource(R.string.notes_no_results), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { creating = !creating }) {
                        Icon(LeafIcons.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.sleep_tag_custom_hint))
                    }
                    if (creating) {
                        AppTextField(newTag, { newTag = it.replace(',', ' ').take(40) },
                            label = stringResource(R.string.sleep_tag_custom_hint), modifier = Modifier.fillMaxWidth())
                        LeafTonalButton(enabled = newTag.trim().isNotEmpty(), onClick = {
                            val value = newTag.trim()
                            custom = custom + value; draft = draft + value
                            prefs.edit().putStringSet("custom", custom).apply()
                            newTag = ""; creating = false; query = ""
                        }) { Text(stringResource(R.string.common_add)) }
                    }
                    if (custom.isNotEmpty()) {
                        Text(stringResource(R.string.sleep_tags_custom), style = MaterialTheme.typography.titleSmall)
                        custom.sorted().forEach { tag ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tag, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = {
                                    custom = custom - tag; draft = draft - tag
                                    prefs.edit().putStringSet("custom", custom).apply()
                                }) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete), Modifier.size(20.dp)) }
                            }
                        }
                    }
                }
            },
            confirmButton = { LeafButton(onClick = { onChange(draft); choosing = false }) {
                Text(stringResource(R.string.sleep_tags_apply, draft.size))
            } },
            dismissButton = { TextButton(onClick = { draft = emptySet() }) { Text(stringResource(R.string.sleep_tags_clear)) } })
    }
}
