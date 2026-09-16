package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun StepperIntField(
    value: Int,
    label: String,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1
) {
    fun wrap(v: Int): Int = when {
        v > range.last  -> range.first
        v < range.first -> range.last
        else -> v
    }
    val haptic = LocalHapticFeedback.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AppTextField(
            value = "%02d".format(value),
            onValueChange = { it.toIntOrNull()?.let { v -> if (v in range) onChange(v) } },
            label = label, keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChange(wrap(value - step))
            }, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(com.example.fitnesstracker.ui.components.LeafIcons.Remove, "$label − $step", modifier = Modifier.size(18.dp))
            }
            FilledTonalIconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChange(wrap(value + step))
            }, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(com.example.fitnesstracker.ui.components.LeafIcons.Add, "$label + $step", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun StepperDecimalField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    step: Float = 1f,
    max: Float = 999f,
    isError: Boolean = false,
    supportingText: String? = null
) {
    fun bump(delta: Float) {
        val next = ((value.toFloatOrNull() ?: 0f) + delta).coerceIn(0f, max)
        onChange(
            if (next % 1f == 0f) String.format(Locale.US, "%.0f", next)
            else String.format(Locale.US, "%.1f", next)
        )
    }
    val haptic = LocalHapticFeedback.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppTextField(value = value, onValueChange = onChange, label = label,
            keyboardType = KeyboardType.Decimal, isError = isError, supportingText = supportingText,
            modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilledTonalIconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); bump(-step)
            }, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(LeafIcons.Remove, "$label − $step", Modifier.size(20.dp))
            }
            FilledTonalIconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); bump(step)
            }, modifier = Modifier.weight(1f).height(48.dp)) {
                Icon(LeafIcons.Add, "$label + $step", Modifier.size(20.dp))
            }
        }
    }
}
