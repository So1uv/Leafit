package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R
import java.util.Locale

/** A grouped HH:mm keyboard control, with editable drafts and valid values delivered to the model. */
@Composable
fun ExpressiveTimeInput(label: String, icon: ImageVector, minutes: Int, onChange: (Int) -> Unit,
                        modifier: Modifier = Modifier) {
    val palette = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = palette.surfaceContainerLow) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, null, tint = palette.tertiary, modifier = Modifier.size(22.dp))
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeSegment(minutes / 60, 23, stringResource(R.string.sleep_hour_label), label,
                    Modifier.weight(1f)) { onChange(it * 60 + minutes % 60) }
                Text(":", style = MaterialTheme.typography.headlineLarge, color = palette.onSurfaceVariant)
                TimeSegment(minutes % 60, 59, stringResource(R.string.sleep_minute_label), label,
                    Modifier.weight(1f)) { onChange(minutes / 60 * 60 + it) }
            }
        }
    }
}

@Composable
private fun TimeSegment(value: Int, max: Int, label: String, groupLabel: String,
                        modifier: Modifier, onValue: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    var draft by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(String.format(Locale.ROOT, "%02d", value)))
    }
    LaunchedEffect(value, focused) {
        if (!focused) draft = TextFieldValue(String.format(Locale.ROOT, "%02d", value))
    }
    val fill by animateColorAsState(if (focused) colors.tertiaryContainer else colors.surfaceContainerHighest,
        spring(stiffness = 450f), label = "timeFocus")
    val corner by animateDpAsState(if (focused) 16.dp else 24.dp,
        spring(dampingRatio = .9f, stiffness = 600f), label = "timeFocusShape")
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicTextField(value = draft, onValueChange = { next ->
            val text = next.text
            if (text.length <= 2 && text.all { it in '0'..'9' } && (text.isEmpty() || (text.toIntOrNull() ?: -1) in 0..max)) {
                draft = next
                onValue(text.toIntOrNull() ?: 0)
            }
        }, singleLine = true, interactionSource = interactions,
            textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center,
                fontFamily = com.example.fitnesstracker.ui.theme.NumericFontFamily,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                fontFeatureSettings = "tnum", letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                color = if (focused) colors.onTertiaryContainer else colors.onSurface),
            cursorBrush = SolidColor(colors.tertiary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                .background(fill, RoundedCornerShape(corner))

                .semantics { contentDescription = "$groupLabel, $label" }
                .onFocusChanged { state ->
                    if (state.isFocused) draft = draft.copy(selection = TextRange(0, draft.text.length))
                    else draft = TextFieldValue(String.format(Locale.ROOT, "%02d", value))
                }.padding(horizontal = 8.dp, vertical = 16.dp),
            decorationBox = { field -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { field() } })
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
    }
}
