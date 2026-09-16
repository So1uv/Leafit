package com.example.fitnesstracker.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

object Motion {

    fun <T> select() = spring<T>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)

    fun <T> settle() = spring<T>(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)

    fun <T> bounce() = spring<T>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
}

object Radii {
    val card = 28.dp        // primary surfaces
    val inner = 18.dp       // nested containers, icon plates
    val small = 12.dp       // compact chips, checkboxes
    val pill = 50.dp        // fully rounded pills/buttons
}
