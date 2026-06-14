package com.example.fitnesstracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.ui.components.ExpressiveBottomBar
import com.example.fitnesstracker.ui.navigation.Screen
import com.example.fitnesstracker.ui.navigation.bottomNavItems
import com.example.fitnesstracker.ui.screens.appsettings.AppSettingsScreen
import com.example.fitnesstracker.ui.screens.home.HomeScreen
import com.example.fitnesstracker.ui.screens.nutrition.NutritionScreen
import com.example.fitnesstracker.ui.screens.onboarding.OnboardingScreen
import com.example.fitnesstracker.ui.screens.settings.SettingsScreen
import com.example.fitnesstracker.ui.screens.sleep.SleepScreen
import com.example.fitnesstracker.ui.screens.workout.WorkoutScreen
import com.example.fitnesstracker.ui.theme.AppThemeMode
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.example.fitnesstracker.ui.theme.ThemePreferences
import com.example.fitnesstracker.utils.StepCounter
import com.example.fitnesstracker.viewmodel.HomeViewModel
import com.example.fitnesstracker.viewmodel.MainViewModel
import com.example.fitnesstracker.viewmodel.NutritionViewModel
import com.example.fitnesstracker.viewmodel.SettingsViewModel
import com.example.fitnesstracker.viewmodel.SleepViewModel
import com.example.fitnesstracker.viewmodel.ViewModelFactory
import com.example.fitnesstracker.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val factory by lazy { ViewModelFactory(application, db) }

    private val mainVm: MainViewModel by viewModels { factory }
    private val homeVm: HomeViewModel by viewModels { factory }
    private val workoutVm: WorkoutViewModel by viewModels { factory }
    private val nutritionVm: NutritionViewModel by viewModels { factory }
    private val sleepVm: SleepViewModel by viewModels { factory }
    private val settingsVm: SettingsViewModel by viewModels { factory }

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) homeVm.startStepTracking()
    }

    private val notificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ok / no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestStepPermissionIfNeeded()
        requestNotificationPermissionIfNeeded()

        setContent {
            var themeMode by remember { mutableStateOf(ThemePreferences.get(this@MainActivity)) }

            FitnessTrackerTheme(themeMode = themeMode) {
                val hasProfile by mainVm.hasProfile.collectAsState()

                if (hasProfile == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@FitnessTrackerTheme
                }

                val lifecycle = LocalLifecycleOwner.current.lifecycle
                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME && StepCounter.hasActivityRecognitionPermission(this@MainActivity)) {
                            homeVm.startStepTracking()
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose { lifecycle.removeObserver(observer) }
                }

                val profileReady = hasProfile == true

                // Ключ пересоздаёт NavController при полном сбросе профиля.
                // Так старый back stack не остаётся поверх Onboarding и не вызывает краши.
                key(profileReady) {
                    val navController = rememberNavController()
                    val startDestination = if (profileReady) Screen.Home.route else Screen.Onboarding.route
                    val navBackStack by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStack?.destination?.route
                    val bottomRoutes = remember { bottomNavItems.map { it.screen.route }.toSet() }
                    val showBottomBar = profileReady && currentRoute in bottomRoutes

                    fun navigateTab(route: String) {
                        if (!profileReady || currentRoute == route) return
                        navController.safeNavigateTopLevel(route)
                    }

                    fun navigateOnce(route: String) {
                        if (!profileReady || currentRoute == route) return
                        navController.safeNavigateOnce(route)
                    }

                    fun backOrHome() {
                        val popped = runCatching { navController.popBackStack() }.getOrDefault(false)
                        if (!popped) {
                            navController.safeNavigateOnce(if (profileReady) Screen.Home.route else Screen.Onboarding.route)
                        }
                    }

                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { fadeIn(tween(180)) },
                            exitTransition = { fadeOut(tween(90)) },
                            popEnterTransition = { fadeIn(tween(180)) },
                            popExitTransition = { fadeOut(tween(90)) }
                        ) {
                            composable(Screen.Onboarding.route) {
                                OnboardingScreen(onFinish = { profile ->
                                    settingsVm.saveProfile(profile)
                                    navController.safeNavigateAfterOnboarding()
                                })
                            }
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    vm = homeVm,
                                    onSettingsClick = { navigateOnce(Screen.AppSettings.route) },
                                    onProfileClick = { navigateOnce(Screen.Settings.route) },
                                    onSleepClick = { navigateTab(Screen.Sleep.route) },
                                    onNutritionClick = { navigateTab(Screen.Nutrition.route) },
                                    onWorkoutClick = { navigateTab(Screen.Workout.route) }
                                )
                            }
                            composable(Screen.Workout.route) { WorkoutScreen(vm = workoutVm) }
                            composable(Screen.Nutrition.route) { NutritionScreen(vm = nutritionVm) }
                            composable(Screen.Sleep.route) { SleepScreen(vm = sleepVm, onBack = { navigateTab(Screen.Home.route) }) }
                            composable(Screen.Settings.route) { SettingsScreen(vm = settingsVm, onBack = { backOrHome() }) }
                            composable(Screen.AppSettings.route) {
                                AppSettingsScreen(
                                    themeMode = themeMode,
                                    onThemeModeChange = { mode: AppThemeMode ->
                                        themeMode = mode
                                        ThemePreferences.set(this@MainActivity, mode)
                                    },
                                    onBack = { backOrHome() },
                                    onProfileClick = { navigateOnce(Screen.Settings.route) },
                                    onResetData = {
                                        settingsVm.resetAllData()
                                        navController.safeNavigateToOnboarding()
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                        ) {
                            ExpressiveBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { navigateTab(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestStepPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                homeVm.startStepTracking()
            } else {
                activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            homeVm.startStepTracking()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private fun NavController.safeNavigateTopLevel(route: String) {
    runCatching {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }
}

private fun NavController.safeNavigateOnce(route: String) {
    runCatching {
        navigate(route) {
            launchSingleTop = true
            restoreState = false
        }
    }
}

private fun NavController.safeNavigateAfterOnboarding() {
    runCatching {
        navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }
}

private fun NavController.safeNavigateToOnboarding() {
    runCatching {
        navigate(Screen.Onboarding.route) {
            popUpTo(graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
            restoreState = false
        }
    }
}
