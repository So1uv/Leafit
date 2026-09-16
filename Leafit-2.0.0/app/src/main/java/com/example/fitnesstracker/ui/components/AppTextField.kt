package com.example.fitnesstracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.R

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    placeholder: String? = null
) {
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val palette = MaterialTheme.colorScheme
    val container by animateColorAsState(
        when {
            isError -> palette.errorContainer
            focused -> palette.primaryContainer
            else -> palette.surfaceContainerHighest
        }, spring(dampingRatio = 1f, stiffness = 500f), label = "fieldFocusTone"
    )
    val corner by animateDpAsState(if (focused) 18.dp else 24.dp,
        spring(dampingRatio = .9f, stiffness = 600f), label = "fieldFocusShape")
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        modifier = modifier,
        interactionSource = interactions,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        textStyle = MaterialTheme.typography.bodyLarge,
        isError = isError,
        supportingText = if (supportingText != null) { { Text(supportingText) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(corner),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            errorContainerColor = container,
            focusedTextColor = palette.onPrimaryContainer,
            errorTextColor = palette.onErrorContainer,
            focusedLabelColor = palette.primary,
            cursorColor = palette.primary,
            focusedPlaceholderColor = palette.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledContainerColor = palette.surfaceContainerHighest,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun AppSearchBar(query: String, onQueryChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val palette = MaterialTheme.colorScheme
    val fill by animateColorAsState(if (focused) palette.primaryContainer else palette.surfaceContainerHighest,
        spring(dampingRatio = 1f, stiffness = 500f), label = "searchFocusTone")
    TextField(value = query, onValueChange = onQueryChange, singleLine = true,
        interactionSource = interactions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text(hint) },
        leadingIcon = { Icon(LeafIcons.Search, null,
            tint = if (focused) palette.primary else palette.onSurfaceVariant) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                Icon(LeafIcons.Close, stringResource(R.string.notes_clear_search))
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fill, unfocusedContainerColor = fill,
            focusedTextColor = palette.onPrimaryContainer,
            cursorColor = palette.primary,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent, errorIndicatorColor = Color.Transparent))
}
