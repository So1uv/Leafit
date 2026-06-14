package com.example.fitnesstracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Onboarding   : Screen("onboarding")
    object Home         : Screen("home")
    object Workout      : Screen("workout")
    object Nutrition    : Screen("nutrition")
    object Sleep        : Screen("sleep")
    object Settings     : Screen("settings")   // профіль
    object AppSettings  : Screen("app_settings") // налаштування застосунку
}

data class BottomNavItem(val label: String, val icon: ImageVector, val screen: Screen)

val bottomNavItems = listOf(
    BottomNavItem("Головна",    Icons.Default.Home,           Screen.Home),
    BottomNavItem("Тренування", Icons.Default.FitnessCenter,  Screen.Workout),
    BottomNavItem("Харчування", Icons.Default.Restaurant,     Screen.Nutrition),
    BottomNavItem("Сон",        Icons.Default.Bedtime,        Screen.Sleep),
)
