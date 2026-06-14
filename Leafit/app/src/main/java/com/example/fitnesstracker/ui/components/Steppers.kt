package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Вертикальна пара стрілочок ▲▼ у стилі застосунку */
@Composable
private fun StepperArrows(onUp: () -> Unit, onDown: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(Icons.Default.KeyboardArrowUp to onUp,
               Icons.Default.KeyboardArrowDown to onDown).forEach { (icon, action) ->
            Box(
                Modifier.size(width = 30.dp, height = 25.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        action()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Числове поле зі стрілочками для Int-значень з циклічним переходом (години/хвилини) */
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StepperArrows(onUp = { onChange(wrap(value + step)) }, onDown = { onChange(wrap(value - step)) })
        AppTextField(
            value = "%02d".format(value),
            onValueChange = { it.toIntOrNull()?.let { v -> if (v in range) onChange(v) } },
            label = label, keyboardType = KeyboardType.Number,
            modifier = Modifier.width(86.dp)
        )
    }
}

/** Числове поле зі стрілочками для String-значень (вік, зріст, вага, калорії) */
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StepperArrows(onUp = { bump(step) }, onDown = { bump(-step) })
        AppTextField(
            value = value, onValueChange = onChange, label = label,
            keyboardType = KeyboardType.Decimal,
            isError = isError, supportingText = supportingText,
            modifier = Modifier.weight(1f)
        )
    }
}
