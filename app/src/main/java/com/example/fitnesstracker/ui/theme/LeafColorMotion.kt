package com.example.fitnesstracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

object LeafTones {
    val sleep: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val onSleep: Color @Composable get() = MaterialTheme.colorScheme.onTertiaryContainer
    val warm: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val onWarm: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
    val dawn: Color @Composable get() = MaterialTheme.colorScheme.primary
    val dawnContainer: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val onDawn: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val mint: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val onMint: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
}
