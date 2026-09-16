package com.example.fitnesstracker

import kotlinx.coroutines.flow.first

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.fitnesstracker.ui.components.LeafLoadingIndicator
import com.example.fitnesstracker.ui.components.LeafPage
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import com.example.fitnesstracker.ui.components.LocalLeafPageActive
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.fitnesstracker.ui.components.LeafTabController
import com.example.fitnesstracker.ui.theme.animatedLeafBackground
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private var shortcutGeneration by mutableStateOf(0)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutGeneration++
    }


    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.example.fitnesstracker.utils.LanguagePreferences.applyTo(newBase))
    }

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
    ) {  }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        requestStepPermissionIfNeeded()
        requestNotificationPermissionIfNeeded()
        com.example.fitnesstracker.widgets.LeafWidgets.requestUpdate(this)

        setContent {
            var themeMode by remember { mutableStateOf(ThemePreferences.get(this@MainActivity)) }

            FitnessTrackerTheme(themeMode = themeMode) {
                val hasProfile by mainVm.hasProfile.collectAsState()

                if (hasProfile == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LeafLoadingIndicator()
                    }
                    return@FitnessTrackerTheme
                }

                val lifecycle = LocalLifecycleOwner.current.lifecycle
                DisposableEffect(lifecycle) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME && StepCounter.hasActivityRecognitionPermission(this@MainActivity)) {
                            homeVm.startStepTracking()
                        } else if (event == Lifecycle.Event.ON_STOP) {
                            homeVm.stopStepTracking()
                        }
                    }
                    lifecycle.addObserver(observer)
                    onDispose { lifecycle.removeObserver(observer) }
                }

                val profileReady = hasProfile == true

                key(profileReady) {
                    val navController = rememberNavController()
                    val startDestination = if (profileReady) Screen.Home.route else Screen.Onboarding.route
                    val navBackStack by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStack?.destination?.route
                    val tabRoutes = remember { bottomNavItems.map { it.screen.route } }
                    val pager = rememberPagerState(pageCount = { tabRoutes.size })
                    val tabScope = rememberCoroutineScope()
                    val tabs = remember(pager, tabScope) { LeafTabController(pager, tabScope) }
                    val showBottomBar = profileReady && currentRoute == Screen.Home.route
                    val selectedTab by remember { derivedStateOf { pager.currentPage } }
                    BackHandler(enabled = showBottomBar && selectedTab != 0) { tabs.select(0) }

                    fun navigateTab(route: String) {
                        val index = tabRoutes.indexOf(route)
                        if (profileReady && index >= 0) tabs.select(index)
                    }

                    fun navigateOnce(route: String) {
                        if (!profileReady || currentRoute == route) return
                        navController.safeNavigateOnce(route)
                    }

                    LaunchedEffect(profileReady, shortcutGeneration) {
                        if (profileReady) {
                            val shortcut = intent?.getStringExtra("shortcut_action")
                            if (shortcut != null && navController.currentDestination?.route != Screen.Home.route) {
                                navController.popBackStack(Screen.Home.route, false)
                            }
                            when (shortcut) {
                                "home" -> tabs.select(0)
                                "water" -> tabs.select(2)
                                "workout" -> tabs.select(1)
                                "sleep" -> tabs.select(3)
                            }
                            intent?.removeExtra("shortcut_action")
                            intent?.getStringExtra("finish_workout_token")?.let { token ->
                                intent?.removeExtra("finish_workout_token")
                                val session = com.example.fitnesstracker.workout.WorkoutSessionEngine.get(this@MainActivity)
                                session.ready.first { it }
                                session.requestFinish(token)
                            }
                        }
                    }

                    fun backOrHome() {
                        val popped = runCatching { navController.popBackStack() }.getOrDefault(false)
                        if (!popped) {
                            navController.safeNavigateOnce(if (profileReady) Screen.Home.route else Screen.Onboarding.route)
                        }
                    }

                    Box(Modifier.fillMaxSize().animatedLeafBackground(MaterialTheme.colorScheme.surface)) {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                slideInHorizontally(spring(.92f, 420f)) { it } + fadeIn(tween(180))
                            },
                            exitTransition = {
                                slideOutHorizontally(spring(1f, 450f)) { -it / 4 } + fadeOut(tween(140))
                            },
                            popEnterTransition = {
                                slideInHorizontally(spring(.93f, 380f)) { -it / 4 } + scaleIn(initialScale = .97f) + fadeIn(tween(160))
                            },
                            popExitTransition = {
                                slideOutHorizontally(spring(.93f, 380f)) { it } + scaleOut(targetScale = .94f) + fadeOut(tween(220))
                            }
                        ) {
                            composable(Screen.Onboarding.route) {
                                val importCtx = LocalContext.current
                                OnboardingScreen(
                                    onFinish = { profile ->
                                        settingsVm.saveProfile(profile)
                                        navController.safeNavigateAfterOnboarding()
                                    },
                                    onImportSnapshot = { uri, onResult ->
                                        this@MainActivity.lifecycleScope.launch {
                                            val ok = com.example.fitnesstracker.utils.BackupManager.importJson(importCtx, uri, replaceProfile = true)
                                            onResult(ok)
                                            if (ok) {
                                                kotlinx.coroutines.delay(600)
                                                (importCtx as? android.app.Activity)?.recreate()
                                            }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Home.route) {
                                LeafPage {
                                    HorizontalPager(
                                        state = pager,
                                        modifier = Modifier.fillMaxSize(),
                                        key = { tabRoutes[it] },
                                        beyondViewportPageCount = 1,
                                        userScrollEnabled = false,
                                        // The bar owns horizontal paging. Never intercept a child list's fling.
                                        pageNestedScrollConnection = remember {
                                            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {}
                                        }
                                    ) { index ->
                                        val pageActive by remember(index, pager) { derivedStateOf { pager.currentPage == index } }
                                        CompositionLocalProvider(LocalLeafPageActive provides pageActive) {
                                        Box(Modifier.fillMaxSize().graphicsLayer {
                                            val distance = kotlin.math.abs(tabs.position - index).coerceIn(0f, 1f)
                                            scaleX = 1f - .018f * distance
                                            scaleY = scaleX
                                            shape = RoundedCornerShape(24.dp)
                                            clip = distance > .001f
                                        }) {
                                            when (tabRoutes[index]) {
                                                Screen.Home.route -> HomeScreen(homeVm,
                                                    onSettingsClick = { navigateOnce(Screen.AppSettings.route) },
                                                    onProfileClick = { navigateOnce(Screen.Settings.route) },
                                                    onSleepClick = { navigateTab(Screen.Sleep.route) },
                                                    onWorkoutClick = { navigateTab(Screen.Workout.route) })
                                                Screen.Workout.route -> WorkoutScreen(workoutVm)
                                                Screen.Nutrition.route -> NutritionScreen(nutritionVm)
                                                Screen.Sleep.route -> SleepScreen(sleepVm, onBack = { tabs.select(0) })
                                            }
                                        }
                                        }
                                    }
                                }
                            }
                            composable(Screen.Settings.route) { LeafPage { SettingsScreen(vm = settingsVm, onBack = { backOrHome() }) } }
                            composable(Screen.AppSettings.route) {
                                LeafPage { AppSettingsScreen(
                                    themeMode = themeMode,
                                    onThemeModeChange = { mode: AppThemeMode ->
                                        themeMode = mode
                                        ThemePreferences.set(this@MainActivity, mode)
                                    },
                                    onBack = { backOrHome() },
                                    onResetData = {
                                        settingsVm.resetAllData()
                                        navController.safeNavigateToOnboarding()
                                    }
                                ) }
                            }
                        }

                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        ) {
                            ExpressiveBottomBar(
                                controller = tabs,
                                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()
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
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
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
