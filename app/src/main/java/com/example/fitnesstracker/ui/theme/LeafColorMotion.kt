package com.example.fitnesstracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

object LeafTones {
    val sleep: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val onSleep: Color @Composable get() = MaterialTheme.colorScheme.onTertiaryContainer
    val warm: Color @Composable get() {
        val c = MaterialTheme.colorScheme
        return lerp(c.surfaceContainer, if (c.surface.luminance() > .5f) Color(0xFFF2E2D3) else Color(0xFF504437), .78f)
    }
    val onWarm: Color @Composable get() = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFF67503D) else Color(0xFFE9D3BD)
    val dawn: Color @Composable get() = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFF8B601B) else Color(0xFFEBC57F)
    val dawnContainer: Color @Composable get() = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFFF6E3BC) else Color(0xFF504125)
    val onDawn: Color @Composable get() = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFF513A18) else Color(0xFFF6E3BC)
    val mint: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val onMint: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
}
