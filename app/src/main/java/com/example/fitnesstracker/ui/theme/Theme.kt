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
    primary = Color(0xFF376B57),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD2EADF),
    onPrimaryContainer = Color(0xFF203E2B),
    secondary = Color(0xFF426455),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E5DC),
    onSecondaryContainer = Color(0xFF234331),
    tertiary = Color(0xFF726384),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE9E1EF),
    onTertiaryContainer = Color(0xFF50415F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFAFCFA),
    onBackground = Color(0xFF17251D),
    surface = Color(0xFFFAFCFA),
    onSurface = Color(0xFF17251D),
    surfaceVariant = Color(0xFFE2E8E2),
    onSurfaceVariant = Color(0xFF505C55),
    outline = Color(0xFF688270),
    outlineVariant = Color(0xFFBDD4C4),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E322B),
    inverseOnSurface = Color(0xFFEFF2E8),
    inversePrimary = Color(0xFFA0CEB7),
    surfaceDim = Color(0xFFD6E5DA),
    surfaceBright = Color(0xFFFAFCFA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F4EF),
    surfaceContainer = Color(0xFFE9EEE8),
    surfaceContainerHigh = Color(0xFFE1E8E0),
    surfaceContainerHighest = Color(0xFFD7E1D8),
    surfaceTint = Color(0xFF376B57)
)

internal val LeafDarkColors = darkColorScheme(
    primary = Color(0xFFA0CEB7),
    onPrimary = Color(0xFF103B22),
    primaryContainer = Color(0xFF314F41),
    onPrimaryContainer = Color(0xFFC6F2D2),
    secondary = Color(0xFFB5D2C0),
    onSecondary = Color(0xFF213C2D),
    secondaryContainer = Color(0xFF3D5147),
    onSecondaryContainer = Color(0xFFD5F1DF),
    tertiary = Color(0xFFCEC0DC),
    onTertiary = Color(0xFF372C44),
    tertiaryContainer = Color(0xFF494052),
    onTertiaryContainer = Color(0xFFE8DFF0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111613),
    onBackground = Color(0xFFE8F4EB),
    surface = Color(0xFF111613),
    onSurface = Color(0xFFE8F4EB),
    surfaceVariant = Color(0xFF3D473F),
    onSurfaceVariant = Color(0xFFC5CEC7),
    outline = Color(0xFF8FAC97),
    outlineVariant = Color(0xFF3E5B47),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E4DA),
    inverseOnSurface = Color(0xFF2E322B),
    inversePrimary = Color(0xFF376B57),
    surfaceDim = Color(0xFF111613),
    surfaceBright = Color(0xFF3A443D),
    surfaceContainerLowest = Color(0xFF0D110F),
    surfaceContainerLow = Color(0xFF1A211C),
    surfaceContainer = Color(0xFF242C26),
    surfaceContainerHigh = Color(0xFF303931),
    surfaceContainerHighest = Color(0xFF3D473F),
    surfaceTint = Color(0xFFA0CEB7)
)

private val AppFontFamily = FontFamily(
    Font(R.font.roboto_flex_400, FontWeight.Normal),
    Font(R.font.roboto_flex_500, FontWeight.Medium),
    Font(R.font.roboto_flex_600, FontWeight.SemiBold),
    Font(R.font.roboto_flex_700, FontWeight.Bold)
)

val NumericFontFamily: FontFamily = AppFontFamily

private val FitnessTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-.25).sp),
    displayMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = .15.sp),
    titleSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .1.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = .5.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .25.sp),
    bodySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .4.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .1.sp),
    labelMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .5.sp),
    labelSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = .5.sp)
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
            LocalRippleConfiguration provides null,
            androidx.compose.ui.platform.LocalHapticFeedback provides remember(context.applicationContext) {
                com.example.fitnesstracker.utils.LeafHapticFeedback(context)
            },
            content = content
        )
    }
}
