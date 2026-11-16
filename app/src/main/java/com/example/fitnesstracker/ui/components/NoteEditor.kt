package com.example.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.theme.LeafSurface
import com.example.fitnesstracker.utils.HapticTexture
import com.example.fitnesstracker.utils.Haptics
import java.util.Calendar

internal data class NoteTask(val text: String, val done: Boolean, val line: Int = -1)
internal data class NoteMoment(val time: String, val text: String)
internal data class NoteTag(val label: String, val text: String)

internal data class NoteDoc(
    val mood: Int = -1,
    val text: String = "",
    val moments: List<NoteMoment> = emptyList(),
    val bullets: List<String> = emptyList(),
    val tasks: List<NoteTask> = emptyList(),
    val tags: List<NoteTag> = emptyList()
) {
    val hasBlocks get() = moments.isNotEmpty() || bullets.isNotEmpty() || tasks.isNotEmpty() || tags.isNotEmpty()

    fun serialize(keepEmpty: Boolean = false): String {
        val parts = buildList {
            val free = if (keepEmpty) text else text.trim('\n', ' ')
            if (free.isNotEmpty()) add(free)
            moments.filter { keepEmpty || it.text.isNotBlank() }.forEach { add("${it.time} — ${it.text}") }
            bullets.filter { keepEmpty || it.isNotBlank() }.forEach { add(NoteFormat.BULLET + it) }
            tasks.filter { keepEmpty || it.text.isNotBlank() }.forEach { add((if (it.done) NoteFormat.DONE else NoteFormat.TODO) + it.text) }
            tags.filter { keepEmpty || it.text.isNotBlank() }.forEach { add("${it.label}: ${it.text}") }
        }
        val body = parts.joinToString("\n")
        return when {
            mood < 0 -> body
            body.isEmpty() -> NoteFormat.moods[mood]
            else -> NoteFormat.moods[mood] + " " + body
        }
    }

    fun headline(): String = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim()
        ?: tags.firstOrNull()?.let { "${it.label}: ${it.text}" }
        ?: moments.firstOrNull()?.let { "${it.time} — ${it.text}" }
        ?: bullets.firstOrNull()
        ?: tasks.firstOrNull()?.text
        ?: ""
}

internal object NoteFormat {
    val moods = listOf("😞", "😕", "😐", "🙂", "😄")
    const val BULLET = "• "
    const val TODO = "☐ "
    const val DONE = "☑ "
    private val momentRegex = Regex("^(\\d{1,2}[:.]\\d{2}(?:[\\s\\u00A0\\u202F]?[AaPp][Mm])?)\\s—\\s?(.*)$")
    private val clockRegex = Regex("(\\d{1,2})[:.](\\d{2})")

    fun mood(text: String): Int = moods.indexOfFirst { text.startsWith("$it ") || text == it }

    fun body(text: String): String {
        val m = mood(text)
        return if (m < 0) text else text.removePrefix(moods[m]).trimStart()
    }

    fun words(text: String) = body(text).split(Regex("\\s+")).count { it.any(Char::isLetterOrDigit) }

    fun headline(text: String) = parse(text).headline()

    fun parse(raw: String): NoteDoc {
        val body = body(raw)
        val offset = raw.split('\n').size - body.split('\n').size
        val free = ArrayList<String>()
        val moments = ArrayList<NoteMoment>()
        val bullets = ArrayList<String>()
        val tasks = ArrayList<NoteTask>()
        val tags = ArrayList<NoteTag>()
        body.split('\n').forEachIndexed { index, line ->
            val moment = momentRegex.find(line)
            val colon = line.indexOf(": ")
            when {
                line.startsWith(TODO) -> tasks += NoteTask(line.removePrefix(TODO), false, index + offset)
                line.startsWith(DONE) -> tasks += NoteTask(line.removePrefix(DONE), true, index + offset)
                line.startsWith(BULLET) -> bullets += line.removePrefix(BULLET)
                moment != null -> moments += NoteMoment(moment.groupValues[1], moment.groupValues[2])
                colon in 1..24 && line.substring(0, colon).none { it in ".,!?;" } ->
                    tags += NoteTag(line.substring(0, colon), line.substring(colon + 2))
                else -> free += line
            }
        }
        return NoteDoc(mood(raw), free.joinToString("\n"), moments, bullets, tasks, tags)
    }

    fun toggleTask(text: String, lineIndex: Int): String {
        val lines = text.split('\n').toMutableList()
        val line = lines.getOrNull(lineIndex) ?: return text
        lines[lineIndex] = when {
            line.startsWith(TODO) -> DONE + line.removePrefix(TODO)
            line.startsWith(DONE) -> TODO + line.removePrefix(DONE)
            else -> line
        }
        return lines.joinToString("\n")
    }

    fun clock(time: String): Pair<Int, Int> {
        val match = clockRegex.find(time)
            ?: return Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) to it.get(Calendar.MINUTE) }
        var hour = match.groupValues[1].toIntOrNull() ?: 0
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        val lower = time.lowercase()
        if (lower.contains('p') && hour < 12) hour += 12
        if (lower.contains('a') && hour == 12) hour = 0
        return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
    }

    fun time(hour: Int, minute: Int, is24: Boolean): String {
        val mm = minute.toString().padStart(2, '0')
        if (is24) return hour.toString().padStart(2, '0') + ":" + mm
        val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
        return "$h:$mm " + if (hour < 12) "AM" else "PM"
    }

    fun now(is24: Boolean): String = Calendar.getInstance().let { time(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE), is24) }
}

private val NoteDocSaver = Saver<NoteDoc, String>(save = { it.serialize(true) }, restore = { NoteFormat.parse(it) })

private val moodIcons: List<ImageVector> get() = listOf(LeafIcons.SentimentVeryDissatisfied, LeafIcons.SentimentDissatisfied,
    LeafIcons.SentimentNeutral, LeafIcons.SentimentSatisfied, LeafIcons.SentimentVerySatisfied)

fun noteMoodIcon(text: String?): ImageVector? = text?.let { NoteFormat.mood(it) }?.takeIf { it >= 0 }?.let { moodIcons[it] }

private data class NoteTagDef(val label: String, val icon: ImageVector, val tone: LeafTone)

@Composable
private fun noteTagDefs(): List<NoteTagDef> = listOf(
    NoteTagDef(stringResource(R.string.b8_note_tag_workout), LeafIcons.Exercise, LeafTone.Primary),
    NoteTagDef(stringResource(R.string.b8_note_tag_food), LeafIcons.Restaurant, LeafTone.Secondary),
    NoteTagDef(stringResource(R.string.b8_note_tag_sleep), LeafIcons.Bedtime, LeafTone.Tertiary),
    NoteTagDef(stringResource(R.string.b8_note_tag_feeling), LeafIcons.VitalSigns, LeafTone.Primary),
    NoteTagDef(stringResource(R.string.b8_note_tag_gratitude), LeafIcons.Favorite, LeafTone.Tertiary))

private fun List<NoteTagDef>.match(label: String): NoteTagDef =
    firstOrNull { it.label.equals(label.trim(), ignoreCase = true) } ?: NoteTagDef(label, LeafIcons.Label, LeafTone.Secondary)

private fun <T> List<T>.replaced(index: Int, value: T): List<T> = mapIndexed { i, item -> if (i == index) value else item }
private fun <T> List<T>.without(index: Int): List<T> = filterIndexed { i, _ -> i != index }
private fun clean(text: String) = text.replace('\n', ' ').take(300)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeafNoteEditor(initial: String, title: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var doc by rememberSaveable(stateSaver = NoteDocSaver) { mutableStateOf(NoteFormat.parse(initial)) }
    var field by remember { mutableStateOf(TextFieldValue(doc.text, TextRange(doc.text.length))) }
    var open by rememberSaveable { mutableStateOf("") }
    var focusKey by remember { mutableStateOf<String?>(null) }
    var timeEdit by remember { mutableStateOf<Int?>(null) }
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val is24 = remember { android.text.format.DateFormat.is24HourFormat(context) }
    val focus = remember { FocusRequester() }
    val tagDefs = noteTagDefs()
    val opened = remember(open) { open.split(',').filter { it.isNotEmpty() }.toSet() }
    LaunchedEffect(Unit) { if (!doc.hasBlocks) runCatching { focus.requestFocus() } }
    fun show(key: String) {
        if (key !in opened) open = (opened + key).joinToString(",")
        focusKey = key
    }
    fun hide(key: String) { open = (opened - key).joinToString(",") }
    val body = remember(doc) { doc.copy(mood = -1).serialize() }

    LeafEditSheet(onDismissRequest = onDismiss, title = { Text(title) },
        confirmButton = {
            LeafButton(enabled = body.isNotBlank(), onClick = { onSave(doc.serialize().trim()) }) {
                Icon(LeafIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            if (body.isNotEmpty() || doc.mood >= 0) LeafTextButton(onClick = {
                doc = NoteDoc(); field = TextFieldValue(""); open = ""
            }) { Text(stringResource(R.string.b8_note_clear)) }
        }) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            NoteSectionLabel(stringResource(R.string.b8_note_mood))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moodIcons.forEachIndexed { index, icon ->
                    val selected = doc.mood == index
                    val bg by animateColorAsState(if (selected) colors.primary else colors.surfaceContainerHigh, label = "mood")
                    val fg by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, label = "moodInk")
                    val scale by animateFloatAsState(if (selected) 1.1f else 1f,
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "moodScale")
                    LeafSurface(onClick = {
                        Haptics.play(context, HapticTexture.DETENT)
                        doc = doc.copy(mood = if (selected) -1 else index)
                    }, modifier = Modifier.weight(1f).height(56.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                        shape = if (selected) LeafRosette() else RoundedCornerShape(20.dp), color = bg) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (selected) LeafIcons.filled(icon) else icon, NoteFormat.moods[index], Modifier.size(28.dp), tint = fg)
                        }
                    }
                }
            }

            LeafSurface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainerHigh) {
                Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(LeafIcons.EditNote, null, Modifier.size(18.dp), tint = colors.primary)
                        Text(stringResource(R.string.b10_note_thoughts), style = MaterialTheme.typography.labelLarge, color = colors.primary)
                    }
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(value = field, onValueChange = {
                        field = it.copy(text = it.text.take(20000)); doc = doc.copy(text = field.text)
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp, max = 280.dp).focusRequester(focus),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        decorationBox = { inner ->
                            Box {
                                if (field.text.isEmpty()) Text(stringResource(R.string.b8_note_placeholder),
                                    style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant.copy(alpha = .7f))
                                inner()
                            }
                        })
                    Text(stringResource(R.string.b8_note_stats, NoteFormat.words(field.text), field.text.length),
                        Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
            }

            NoteBlockSlot(doc.moments.isNotEmpty() || "moments" in opened) {
                NoteBlock(LeafIcons.Schedule, stringResource(R.string.b10_note_moments), LeafTone.Tertiary,
                    doc.moments.size.takeIf { it > 0 }?.toString(), if (doc.moments.isEmpty()) ({ hide("moments") }) else null) {
                    doc.moments.forEachIndexed { index, moment ->
                        NoteItemRow(moment.text, { doc = doc.copy(moments = doc.moments.replaced(index, moment.copy(text = clean(it)))) },
                            onDelete = { doc = doc.copy(moments = doc.moments.without(index)) }) {
                            LeafSurface(onClick = { timeEdit = index }, modifier = Modifier.padding(start = 8.dp, end = 12.dp),
                                shape = RoundedCornerShape(12.dp), color = colors.tertiaryContainer,
                                contentColor = colors.onTertiaryContainer) {
                                Text(moment.time, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge, maxLines = 1)
                            }
                        }
                    }
                    NoteAddRow(stringResource(R.string.b10_note_add_moment), "moments", focusKey, { focusKey = null }) {
                        doc = doc.copy(moments = doc.moments + NoteMoment(NoteFormat.now(is24), it))
                    }
                }
            }

            NoteBlockSlot(doc.bullets.isNotEmpty() || "list" in opened) {
                NoteBlock(LeafIcons.FormatListBulleted, stringResource(R.string.b8_note_list), LeafTone.Secondary,
                    doc.bullets.size.takeIf { it > 0 }?.toString(), if (doc.bullets.isEmpty()) ({ hide("list") }) else null) {
                    doc.bullets.forEachIndexed { index, item ->
                        NoteItemRow(item, { doc = doc.copy(bullets = doc.bullets.replaced(index, clean(it))) },
                            onDelete = { doc = doc.copy(bullets = doc.bullets.without(index)) }) {
                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(colors.secondary))
                            }
                        }
                    }
                    NoteAddRow(stringResource(R.string.b10_note_add_item), "list", focusKey, { focusKey = null }) {
                        doc = doc.copy(bullets = doc.bullets + it)
                    }
                }
            }

            NoteBlockSlot(doc.tasks.isNotEmpty() || "tasks" in opened) {
                val done = doc.tasks.count { it.done }
                NoteBlock(if (doc.tasks.isNotEmpty() && done == doc.tasks.size) LeafIcons.TaskAlt else LeafIcons.Checklist,
                    stringResource(R.string.b10_note_tasks), LeafTone.Primary,
                    doc.tasks.size.takeIf { it > 0 }?.let { "$done/$it" }, if (doc.tasks.isEmpty()) ({ hide("tasks") }) else null) {
                    if (doc.tasks.isNotEmpty()) {
                        val progress by animateFloatAsState(done / doc.tasks.size.toFloat(), spring(stiffness = Spring.StiffnessLow),
                            label = "tasks")
                        LeafWavyProgress(progress, true, Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp).height(16.dp),
                            colors.primary, colors.primaryContainer)
                    }
                    doc.tasks.forEachIndexed { index, task ->
                        NoteItemRow(task.text, { doc = doc.copy(tasks = doc.tasks.replaced(index, task.copy(text = clean(it)))) },
                            onDelete = { doc = doc.copy(tasks = doc.tasks.without(index)) }, struck = task.done) {
                            Checkbox(checked = task.done, onCheckedChange = { checked ->
                                Haptics.play(context, if (checked) HapticTexture.CONFIRM else HapticTexture.DETENT)
                                doc = doc.copy(tasks = doc.tasks.replaced(index, task.copy(done = checked)))
                            })
                        }
                    }
                    NoteAddRow(stringResource(R.string.b9_note_add_task), "tasks", focusKey, { focusKey = null }) {
                        doc = doc.copy(tasks = doc.tasks + NoteTask(it, false))
                    }
                }
            }

            NoteBlockSlot(doc.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    doc.tags.forEachIndexed { index, tag ->
                        NoteTagField(tagDefs.match(tag.label), tag.text, groupedLeafShape(index, doc.tags.size), "tag:${tag.label}",
                            focusKey, { focusKey = null },
                            onText = { doc = doc.copy(tags = doc.tags.replaced(index, tag.copy(text = clean(it)))) },
                            onDelete = { doc = doc.copy(tags = doc.tags.without(index)) })
                    }
                }
            }

            NoteSectionLabel(stringResource(R.string.b10_note_add_block))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteChip(LeafIcons.Schedule, stringResource(R.string.b8_note_time),
                    doc.moments.isNotEmpty() || "moments" in opened) { show("moments") }
                NoteChip(LeafIcons.FormatListBulleted, stringResource(R.string.b8_note_list),
                    doc.bullets.isNotEmpty() || "list" in opened) { show("list") }
                NoteChip(LeafIcons.CheckBox, stringResource(R.string.b8_note_task),
                    doc.tasks.isNotEmpty() || "tasks" in opened) { show("tasks") }
                tagDefs.forEach { def ->
                    val present = doc.tags.any { it.label.equals(def.label, ignoreCase = true) }
                    NoteChip(def.icon, def.label, present) {
                        if (!present) doc = doc.copy(tags = doc.tags + NoteTag(def.label, ""))
                        focusKey = "tag:${def.label}"
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    val editIndex = timeEdit
    val editMoment = editIndex?.let { doc.moments.getOrNull(it) }
    if (editIndex != null && editMoment != null) NoteTimeDialog(editMoment.time, is24, { timeEdit = null }) {
        doc = doc.copy(moments = doc.moments.replaced(editIndex, editMoment.copy(time = it))); timeEdit = null
    }
}

@Composable
private fun NoteSectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun NoteBlockSlot(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(visible, enter = fadeIn() + expandVertically(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)),
        exit = fadeOut() + shrinkVertically()) { content() }
}

@Composable
private fun NoteBlock(icon: ImageVector, title: String, tone: LeafTone, badge: String?, onClose: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val toneColors = leafToneColors(tone)
    LeafSurface(shape = RoundedCornerShape(24.dp), color = colors.surfaceContainer) {
        Column(Modifier.fillMaxWidth().animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)).padding(8.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LeafIconBadge(icon, size = LeafIconSize.Small, tone = tone)
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (badge != null) LeafSurface(shape = CircleShape, color = toneColors.container, contentColor = toneColors.content) {
                    Text(badge, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                }
                if (onClose != null) LeafIconButton(onClick = onClose) {
                    Icon(LeafIcons.Close, stringResource(R.string.common_close), Modifier.size(18.dp), tint = colors.onSurfaceVariant)
                } else Spacer(Modifier.width(4.dp))
            }
            content()
        }
    }
}

@Composable
private fun NoteItemRow(text: String, onText: (String) -> Unit, onDelete: () -> Unit, struck: Boolean = false,
    leading: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val ink by animateColorAsState(if (struck) colors.onSurfaceVariant else colors.onSurface, label = "itemInk")
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        leading()
        BasicTextField(value = text, onValueChange = onText, modifier = Modifier.weight(1f).padding(vertical = 6.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = ink,
                textDecoration = if (struck) TextDecoration.LineThrough else null),
            cursorBrush = SolidColor(colors.primary), maxLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done))
        LeafIconButton(onClick = onDelete) {
            Icon(LeafIcons.Close, stringResource(R.string.common_delete), Modifier.size(18.dp), tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoteAddRow(placeholder: String, key: String, focusKey: String?, onFocused: () -> Unit, onAdd: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var draft by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(focusKey) {
        if (focusKey == key) { runCatching { focus.requestFocus() }; onFocused() }
    }
    fun add() {
        val item = draft.trim()
        if (item.isEmpty()) return
        onAdd(item)
        draft = ""
        Haptics.play(context, HapticTexture.DETENT)
    }
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(LeafIcons.Add, null, Modifier.size(22.dp), tint = colors.primary)
        }
        BasicTextField(value = draft, onValueChange = { draft = clean(it) }, singleLine = true,
            modifier = Modifier.weight(1f).focusRequester(focus),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { add() }),
            decorationBox = { inner ->
                Box {
                    if (draft.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    inner()
                }
            })
        if (draft.isNotBlank()) LeafFilledTonalIconButton(onClick = { add() }) { Icon(LeafIcons.Check, placeholder) }
    }
}

@Composable
private fun NoteTagField(def: NoteTagDef, text: String, shape: RoundedCornerShape, key: String, focusKey: String?,
    onFocused: () -> Unit, onText: (String) -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    LaunchedEffect(focusKey) {
        if (focusKey == key) { runCatching { focus.requestFocus() }; onFocused() }
    }
    LeafSurface(shape = shape, color = colors.surfaceContainer) {
        Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(def.icon, size = LeafIconSize.Small, tone = def.tone)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(def.label, style = MaterialTheme.typography.labelLarge, color = colors.primary, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                BasicTextField(value = text, onValueChange = onText, modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary), maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    decorationBox = { inner ->
                        Box {
                            if (text.isEmpty()) Text(stringResource(R.string.b10_note_tag_hint), style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant.copy(alpha = .7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            inner()
                        }
                    })
            }
            LeafIconButton(onClick = onDelete) {
                Icon(LeafIcons.Close, stringResource(R.string.common_delete), Modifier.size(18.dp), tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NoteChip(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (active) colors.primaryContainer else colors.surfaceContainerHigh, label = "chip")
    val fg by animateColorAsState(if (active) colors.onPrimaryContainer else colors.onSurfaceVariant, label = "chipInk")
    LeafSurface(onClick = { Haptics.play(context, HapticTexture.DETENT); onClick() },
        shape = RoundedCornerShape(if (active) 20.dp else 12.dp), color = bg, contentColor = fg) {
        Row(Modifier.height(40.dp).padding(start = 12.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(if (active) LeafIcons.filled(icon) else icon, null, Modifier.size(18.dp), tint = fg)
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, color = fg)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTimeDialog(initial: String, is24: Boolean, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val start = remember(initial) { NoteFormat.clock(initial) }
    val state = rememberTimePickerState(start.first, start.second, is24)
    AlertDialog(onDismissRequest = onDismiss,
        confirmButton = {
            LeafTextButton(onClick = { onPick(NoteFormat.time(state.hour, state.minute, is24)) }) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = { LeafTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        title = { Text(stringResource(R.string.b8_note_time)) },
        text = { TimePicker(state) })
}

@Composable
fun NotePreview(text: String, maxLines: Int, onToggleTask: ((Int) -> Unit)?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val doc = remember(text) { NoteFormat.parse(text) }
    val full = maxLines == Int.MAX_VALUE
    val tagDefs = noteTagDefs()
    val allParagraphs = doc.text.split('\n').filter { it.isNotBlank() }
    var budget = maxLines
    val paragraphs = allParagraphs.take(budget.coerceAtLeast(0)); budget -= paragraphs.size
    val moments = doc.moments.take(budget.coerceAtLeast(0)); budget -= moments.size
    val bullets = doc.bullets.take(budget.coerceAtLeast(0)); budget -= bullets.size
    val tasks = doc.tasks.take(budget.coerceAtLeast(0)); budget -= tasks.size
    val tags = doc.tags.take(budget.coerceAtLeast(0))
    val hidden = allParagraphs.size + doc.moments.size + doc.bullets.size + doc.tasks.size + doc.tags.size -
        (paragraphs.size + moments.size + bullets.size + tasks.size + tags.size)
    val itemLines = if (full) Int.MAX_VALUE else 2
    Column(modifier, verticalArrangement = Arrangement.spacedBy(if (full) 12.dp else 10.dp)) {
        if (full && doc.mood >= 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LeafIconBadge(moodIcons[doc.mood], size = LeafIconSize.Large, tone = LeafTone.Solid, shape = LeafRosette())
            Text(stringResource(R.string.b8_note_mood), style = MaterialTheme.typography.titleMedium)
        }
        if (paragraphs.isNotEmpty()) NotePreviewGroup(full, LeafIcons.EditNote, stringResource(R.string.b10_note_thoughts), LeafTone.Primary) {
            Text(paragraphs.joinToString("\n"), style = MaterialTheme.typography.bodyLarge,
                maxLines = if (full) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis)
        }
        if (moments.isNotEmpty()) NotePreviewGroup(full, LeafIcons.Schedule, stringResource(R.string.b10_note_moments), LeafTone.Tertiary) {
            moments.forEach { moment ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeafSurface(shape = RoundedCornerShape(12.dp), color = colors.tertiaryContainer,
                        contentColor = colors.onTertiaryContainer) {
                        Text(moment.time, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                    Text(moment.text, Modifier.weight(1f).padding(top = 2.dp), style = MaterialTheme.typography.bodyLarge,
                        maxLines = itemLines, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (bullets.isNotEmpty()) NotePreviewGroup(full, LeafIcons.FormatListBulleted, stringResource(R.string.b8_note_list), LeafTone.Secondary) {
            bullets.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.padding(top = 8.dp, start = 4.dp).size(8.dp).clip(CircleShape).background(colors.secondary))
                    Text(item, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = itemLines,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (tasks.isNotEmpty()) NotePreviewGroup(full, null, "", LeafTone.Primary) {
            TaskProgress(doc.tasks.count { it.done }, doc.tasks.size)
            tasks.forEach { task ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).let { m ->
                    if (onToggleTask != null) m.clickable { onToggleTask(task.line) } else m
                }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.done,
                        onCheckedChange = onToggleTask?.let { toggle -> { _: Boolean -> toggle(task.line) } },
                        colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.outline,
                            checkmarkColor = colors.onPrimary))
                    Text(task.text, Modifier.weight(1f).padding(end = 8.dp), style = MaterialTheme.typography.bodyLarge,
                        maxLines = itemLines, overflow = TextOverflow.Ellipsis,
                        color = if (task.done) colors.onSurfaceVariant else colors.onSurface,
                        textDecoration = if (task.done) TextDecoration.LineThrough else null)
                }
            }
        }
        if (tags.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(if (full) 2.dp else 10.dp)) {
            tags.forEachIndexed { index, tag ->
                val def = tagDefs.match(tag.label)
                if (full) LeafSurface(Modifier.fillMaxWidth(), shape = groupedLeafShape(index, tags.size),
                    color = colors.surfaceContainerHigh) {
                    Box(Modifier.padding(16.dp)) { NoteTagRow(def, tag, itemLines) }
                } else NoteTagRow(def, tag, itemLines)
            }
        }
        if (hidden > 0) LeafSurface(shape = CircleShape, color = colors.surfaceContainerHighest, contentColor = colors.onSurfaceVariant) {
            Text("+$hidden", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun NoteTagRow(def: NoteTagDef, tag: NoteTag, lines: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LeafIconBadge(def.icon, size = LeafIconSize.Small, tone = def.tone)
        Column(Modifier.weight(1f)) {
            Text(tag.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(tag.text, style = MaterialTheme.typography.bodyLarge, maxLines = lines, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NotePreviewGroup(full: Boolean, icon: ImageVector?, title: String, tone: LeafTone,
    content: @Composable ColumnScope.() -> Unit) {
    if (!full) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        return
    }
    LeafSurface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (icon != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LeafIconBadge(icon, size = LeafIconSize.Small, tone = tone)
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            content()
        }
    }
}

@Composable
fun NoteSummary(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val doc = remember(text) { NoteFormat.parse(text) }
    val tagDefs = noteTagDefs()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val headline = doc.headline()
        if (headline.isNotBlank()) Text(headline, style = MaterialTheme.typography.bodyMedium, maxLines = 2,
            overflow = TextOverflow.Ellipsis)
        if (doc.hasBlocks) Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (doc.tasks.isNotEmpty()) NoteCount(if (doc.tasks.all { it.done }) LeafIcons.TaskAlt else LeafIcons.Checklist,
                "${doc.tasks.count { it.done }}/${doc.tasks.size}")
            if (doc.bullets.isNotEmpty()) NoteCount(LeafIcons.FormatListBulleted, doc.bullets.size.toString())
            if (doc.moments.isNotEmpty()) NoteCount(LeafIcons.Schedule, doc.moments.size.toString())
            doc.tags.take(3).forEach { tag ->
                Icon(LeafIcons.filled(tagDefs.match(tag.label).icon), tag.label, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun NoteCount(icon: ImageVector, value: String) {
    val colors = MaterialTheme.colorScheme
    LeafSurface(shape = CircleShape, color = colors.surface, contentColor = colors.onSurfaceVariant) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, Modifier.size(14.dp))
            Text(value, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun TaskProgress(done: Int, total: Int) {
    val colors = MaterialTheme.colorScheme
    val complete = done == total
    val progress by animateFloatAsState(if (total == 0) 0f else done / total.toFloat(), spring(stiffness = Spring.StiffnessLow),
        label = "taskProgress")
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        LeafIconBadge(if (complete) LeafIcons.TaskAlt else LeafIcons.Checklist, size = LeafIconSize.Small,
            tone = if (complete) LeafTone.Solid else LeafTone.Primary, shape = if (complete) LeafRosette() else null)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.b9_note_tasks_done, done, total),
                style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            LeafWavyProgress(progress, !complete, Modifier.fillMaxWidth().height(14.dp), colors.primary, colors.primaryContainer)
        }
    }
}
