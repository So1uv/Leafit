package com.example.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.DayNote
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.DateUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DayNoteCard(note: DayNote?, onSave: (String) -> Unit, onHistory: () -> Unit,
    onDelete: (DayNote) -> Unit, onTogglePin: (DayNote) -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    LeafSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(30.dp),
        color = colors.surfaceContainer) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LeafSurface(shape = LeafRosette(), color = colors.tertiaryContainer) {
                    Box(Modifier.size(48.dp), Alignment.Center) { Icon(LeafIcons.EditNote, null, tint = colors.onTertiaryContainer) }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_day_note), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.common_today), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                FilledTonalIconButton(onClick = onHistory) { Icon(LeafIcons.History, stringResource(R.string.home_note_history)) }
            }
            Text(note?.text?.takeIf { it.isNotBlank() } ?: stringResource(R.string.home_day_note_hint),
                style = MaterialTheme.typography.bodyLarge, maxLines = 5, overflow = TextOverflow.Ellipsis,
                color = if (note == null) colors.onSurfaceVariant else colors.onSurface)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (note != null) {
                    IconButton(onClick = { onTogglePin(note) }) {
                        Icon(if (note.isPinned) LeafIcons.Bookmark else LeafIcons.BookmarkBorder,
                            stringResource(if (note.isPinned) R.string.notes_unpin else R.string.notes_pin),
                            tint = if (note.isPinned) colors.tertiary else colors.onSurfaceVariant)
                    }
                    IconButton(onClick = { confirmDelete = true }) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete)) }
                }
                Spacer(Modifier.weight(1f))
                LeafTonalButton(onClick = { editing = true }) {
                    Icon(if (note == null) LeafIcons.Add else LeafIcons.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(if (note == null) R.string.notes_write else R.string.notes_edit_short))
                }
            }
        }
    }
    if (editing) {
        var draft by rememberSaveable { mutableStateOf(note?.text.orEmpty()) }
        LeafEditSheet(onDismissRequest = { editing = false }, title = { Text(stringResource(R.string.home_day_note)) },
            content = { AppTextField(draft, { draft = it.take(20000) }, label = stringResource(R.string.home_day_note),
                modifier = Modifier.fillMaxWidth(), singleLine = false, minLines = 6, maxLines = 16,
                supportingText = stringResource(R.string.notes_character_count, draft.length)) },
            confirmButton = { LeafButton(enabled = draft.isNotBlank(), onClick = { onSave(draft.trim()); editing = false }) {
                Text(stringResource(R.string.common_save))
            } }, dismissButton = { TextButton(onClick = { editing = false }) { Text(stringResource(R.string.common_cancel)) } })
    }
    if (confirmDelete && note != null) NoteDeleteDialog(note, { confirmDelete = false }) {
        onDelete(note); confirmDelete = false
    }
}

@Composable
private fun NoteDeleteDialog(note: DayNote, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(LeafIcons.DeleteOutline, null) },
        title = { Text(stringResource(R.string.notes_delete_title)) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(DateUtils.formatDate(note.dayStart), style = MaterialTheme.typography.labelLarge)
            Text(note.text, maxLines = 3, overflow = TextOverflow.Ellipsis)
        } },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteHistoryDialog(notes: List<DayNote>, onDismiss: () -> Unit,
    onDelete: (DayNote) -> Unit, onTogglePin: (DayNote) -> Unit, onSave: (DayNote) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var pinnedOnly by rememberSaveable { mutableStateOf(false) }
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    var openedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<DayNote?>(null) }
    val colors = MaterialTheme.colorScheme
    val locale = appLocale()
    val visible = remember(notes, query, pinnedOnly, newestFirst) {
        notes.filter { (!pinnedOnly || it.isPinned) && it.text.contains(query.trim(), ignoreCase = true) }
            .sortedBy { if (newestFirst) -it.dayStart else it.dayStart }
    }
    val groups = remember(visible, locale) {
        val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)
        visible.groupBy { Instant.ofEpochMilli(it.dayStart).atZone(ZoneId.systemDefault()).format(formatter) }
    }
    val opened = notes.firstOrNull { it.dayStart == openedDay }
    val clipboard = LocalClipboardManager.current
    pendingDelete?.let { note -> NoteDeleteDialog(note, { pendingDelete = null }) {
        onDelete(note); pendingDelete = null
        if (openedDay == note.dayStart) { openedDay = null; editing = false }
    } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), containerColor = colors.surfaceContainerLow,
        tonalElevation = 0.dp) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.92f).imePadding()) {
            Row(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (opened != null) FilledTonalIconButton(onClick = { openedDay = null; editing = false }) {
                    Icon(LeafIcons.ArrowBack, stringResource(R.string.common_back))
                } else LeafSurface(shape = LeafRosette(), color = colors.tertiaryContainer) {
                    Box(Modifier.size(48.dp), Alignment.Center) { Icon(LeafIcons.EditNote, null, tint = colors.onTertiaryContainer) }
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_note_history), style = MaterialTheme.typography.titleLarge)
                    Text(if (opened != null) DateUtils.formatDate(opened.dayStart) else stringResource(R.string.home_notes_count, notes.size),
                        style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) { Icon(LeafIcons.Close, stringResource(R.string.common_close)) }
            }
            AnimatedContent(targetState = openedDay, modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(100)) }, label = "notePage") { selectedDay ->
                val opened = notes.firstOrNull { it.dayStart == selectedDay }
                if (opened != null) {
                    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
                            item(key = opened.dayStart) {
                                if (editing) AppTextField(draft, { draft = it.take(20000) },
                                    label = stringResource(R.string.home_day_note), modifier = Modifier.fillMaxWidth(),
                                    singleLine = false, minLines = 8, maxLines = Int.MAX_VALUE)
                                else SelectionContainer { Text(opened.text, style = MaterialTheme.typography.bodyLarge) }
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (editing) {
                                TextButton(onClick = { editing = false }) { Text(stringResource(R.string.common_cancel)) }
                                Spacer(Modifier.weight(1f))
                                LeafButton(enabled = draft.isNotBlank(), onClick = {
                                    onSave(opened.copy(text = draft.trim())); editing = false
                                }) { Text(stringResource(R.string.common_save)) }
                            } else {
                                IconButton(onClick = { clipboard.setText(AnnotatedString(opened.text)) }) { Icon(LeafIcons.ContentCopy, stringResource(R.string.notes_copy)) }
                                IconButton(onClick = { onTogglePin(opened) }) {
                                    Icon(if (opened.isPinned) LeafIcons.Bookmark else LeafIcons.BookmarkBorder,
                                        stringResource(if (opened.isPinned) R.string.notes_unpin else R.string.notes_pin))
                                }
                                IconButton(onClick = { pendingDelete = opened }) { Icon(LeafIcons.DeleteOutline, stringResource(R.string.common_delete)) }
                                Spacer(Modifier.weight(1f))
                                LeafTonalButton(onClick = { draft = opened.text; editing = true }) { Text(stringResource(R.string.notes_edit_short)) }
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        AppSearchBar(query, { query = it }, stringResource(R.string.notes_search), Modifier.padding(horizontal = 20.dp))
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            ExpressiveChoiceRow(listOf(stringResource(R.string.notes_all), stringResource(R.string.notes_pinned)),
                                listOf(LeafIcons.EditNote, LeafIcons.Bookmark), if (pinnedOnly) 1 else 0,
                                { pinnedOnly = it == 1 }, modifier = Modifier.weight(1f))
                            IconButton(onClick = { newestFirst = !newestFirst }) {
                                Icon(LeafIcons.SwapVert, stringResource(if (newestFirst) R.string.notes_newest_first else R.string.notes_oldest_first))
                            }
                        }
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (visible.isEmpty()) item {
                                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(LeafIcons.EditNote, null, Modifier.size(36.dp), tint = colors.tertiary)
                                    Text(stringResource(if (notes.isEmpty()) R.string.home_note_history_empty else R.string.notes_no_results))
                                }
                            }
                            groups.forEach { (month, records) ->
                                item(key = "month_$month") { Text(month, Modifier.padding(top = 16.dp, bottom = 8.dp),
                                    style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant) }
                                itemsIndexed(records, key = { _, note -> note.dayStart }) { index, note ->
                                    val date = Instant.ofEpochMilli(note.dayStart).atZone(ZoneId.systemDefault())
                                    LeafSurface(onClick = { openedDay = note.dayStart; editing = false },
                                        shape = RoundedCornerShape(topStart = if (index == 0) 24.dp else 6.dp,
                                            topEnd = if (index == 0) 24.dp else 6.dp,
                                            bottomStart = if (index == records.lastIndex) 24.dp else 6.dp,
                                            bottomEnd = if (index == records.lastIndex) 24.dp else 6.dp),
                                        color = if (note.isPinned) colors.tertiaryContainer else colors.surfaceContainerHigh) {
                                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                            LeafSurface(shape = RoundedCornerShape(16.dp), color = colors.surface) {
                                                Column(Modifier.width(48.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge)
                                                    Text(date.format(DateTimeFormatter.ofPattern("EEE", locale)), style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            Text(note.text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 3, overflow = TextOverflow.Ellipsis)
                                            Icon(if (note.isPinned) LeafIcons.Bookmark else LeafIcons.ChevronRight, null, Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
