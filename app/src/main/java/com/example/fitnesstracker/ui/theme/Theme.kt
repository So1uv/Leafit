package com.example.fitnesstracker.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import com.example.fitnesstracker.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

object ThemePreferences {
    private const val PREFS = "ui_theme_prefs"
    private const val KEY_MODE = "theme_mode"

    fun dynamicColor(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("dynamic_color", false)

    fun setDynamicColor(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("dynamic_color", enabled).apply()
    }

    fun get(context: Context): AppThemeMode {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_MODE, AppThemeMode.SYSTEM.name)
        return runCatching { AppThemeMode.valueOf(value ?: AppThemeMode.SYSTEM.name) }
            .getOrDefault(AppThemeMode.SYSTEM)
    }

    fun set(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.name)
            .apply()
    }
}

internal val LeafLightColors = lightColorScheme(
    primary = Color(0xFF3B674D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBEAD3),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF536257),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E7D9),
    onSecondaryContainer = Color(0xFF111E16),
    tertiary = Color(0xFF635A76),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF1F1730),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF5FBF6),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF5FBF6),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFD9E5DB),
    onSurfaceVariant = Color(0xFF3C473F),
    outline = Color(0xFF6C776E),
    outlineVariant = Color(0xFFB8C8BB),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E312E),
    inverseOnSurface = Color(0xFFECF2ED),
    inversePrimary = Color(0xFFA2D2B1),
    surfaceDim = Color(0xFFD2DDD4),
    surfaceBright = Color(0xFFF5FBF6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF6F0),
    surfaceContainer = Color(0xFFE7F1E9),
    surfaceContainerHigh = Color(0xFFE0ECE3),
    surfaceContainerHighest = Color(0xFFDBE5DD),
    surfaceTint = Color(0xFF3B674D)
)

internal val LeafDarkColors = darkColorScheme(
    primary = Color(0xFFA2D2B1),
    onPrimary = Color(0xFF083821),
    primaryContainer = Color(0xFF314D3B),
    onPrimaryContainer = Color(0xFFBDEECC),
    secondary = Color(0xFFBBCABD),
    onSecondary = Color(0xFF26332A),
    secondaryContainer = Color(0xFF3C4A40),
    onSecondaryContainer = Color(0xFFD7E7D9),
    tertiary = Color(0xFFCDC1E2),
    onTertiary = Color(0xFF342C46),
    tertiaryContainer = Color(0xFF4B435D),
    onTertiaryContainer = Color(0xFFE9DDFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111412),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF111412),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF404942),
    onSurfaceVariant = Color(0xFFC0C9C0),
    outline = Color(0xFF8A938B),
    outlineVariant = Color(0xFF404942),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E3DE),
    inverseOnSurface = Color(0xFF2E312E),
    inversePrimary = Color(0xFF3B674D),
    surfaceDim = Color(0xFF111412),
    surfaceBright = Color(0xFF373A37),
    surfaceContainerLowest = Color(0xFF0C0F0D),
    surfaceContainerLow = Color(0xFF191C1A),
    surfaceContainer = Color(0xFF1D201E),
    surfaceContainerHigh = Color(0xFF282B28),
    surfaceContainerHighest = Color(0xFF323633),
    surfaceTint = Color(0xFFA2D2B1)
)

private val AppFontFamily = FontFamily(
    Font(R.font.roboto_flex_400, FontWeight.Normal),
    Font(R.font.roboto_flex_500, FontWeight.Medium),
    Font(R.font.roboto_flex_600, FontWeight.SemiBold),
    Font(R.font.roboto_flex_700, FontWeight.Bold)
)

val NumericFontFamily: FontFamily = AppFontFamily

private val FitnessTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-.25).sp),
    displayMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = .15.sp),
    titleSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .1.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = .5.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .25.sp),
    bodySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .4.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .1.sp),
    labelMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .5.sp),
    labelSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .4.sp)
)

private val FitnessShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small      = RoundedCornerShape(16.dp),
    medium     = RoundedCornerShape(20.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val context = LocalContext.current
    var dynamicColor by remember { mutableStateOf(ThemePreferences.dynamicColor(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences("ui_theme_prefs", Context.MODE_PRIVATE)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "dynamic_color") dynamicColor = ThemePreferences.dynamicColor(context)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    val colorScheme = remember(context, dynamicColor, darkTheme) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (darkTheme) LeafDarkColors else LeafLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = FitnessTypography, shapes = FitnessShapes) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides androidx.compose.material3.RippleConfiguration(),
            androidx.compose.ui.platform.LocalHapticFeedback provides remember(context.applicationContext) {
                com.example.fitnesstracker.utils.LeafHapticFeedback(context)
            },
            content = content
        )
    }
}
