package com.example.fitnesstracker.ui.navigation

import com.example.fitnesstracker.ui.components.LeafIcons
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fitnesstracker.R

sealed class Screen(val route: String) {
    object Onboarding   : Screen("onboarding")
    object Home         : Screen("home")
    object Workout      : Screen("workout")
    object Nutrition    : Screen("nutrition")
    object Sleep        : Screen("sleep")
    object Settings     : Screen("settings")
    object AppSettings  : Screen("app_settings")
}

data class BottomNavItem(val labelRes: Int, val icon: ImageVector, val selectedIcon: ImageVector, val screen: Screen)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_home, LeafIcons.Home, LeafIcons.HomeFilled, Screen.Home),
    BottomNavItem(R.string.nav_workout, LeafIcons.Exercise, LeafIcons.ExerciseFilled, Screen.Workout),
    BottomNavItem(R.string.nav_nutrition, LeafIcons.Dining, LeafIcons.DiningFilled, Screen.Nutrition),
    BottomNavItem(R.string.nav_sleep, LeafIcons.Bedtime, LeafIcons.BedtimeFilled, Screen.Sleep),
)
