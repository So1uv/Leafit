package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Клік з «пружинним» стисканням у стилі iOS + легка хаптика.
 * Без ripple — лише плавний масштаб.
 */
fun Modifier.bouncyClick(scaleDown: Float = 0.97f, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "bouncy"
    )
    // Лёгкое «приглушение» при нажатии — как у нативных кнопок iOS
    val pressAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.82f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bouncyAlpha"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale; alpha = pressAlpha }
        .clickable(interactionSource = interaction, indication = null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}
