package com.example.fitnesstracker.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

enum class WorkoutType(
    val key: String,
    val icon: ImageVector
) {
    GENERAL("general", com.example.fitnesstracker.ui.components.LeafIcons.FitnessCenter),
    RUN("run", com.example.fitnesstracker.ui.components.LeafIcons.DirectionsRun),
    WALK("walk", com.example.fitnesstracker.ui.components.LeafIcons.DirectionsWalk),
    CYCLE("cycle", LeafIcons.DirectionsBike),
    GYM("gym", com.example.fitnesstracker.ui.components.LeafIcons.FitnessCenter),
    YOGA("yoga", LeafIcons.SelfImprovement),
    SPORT("sport", LeafIcons.SportsTennis);

    companion object {
        fun fromKey(key: String): WorkoutType = entries.firstOrNull { it.key == key } ?: GENERAL
    }
}
