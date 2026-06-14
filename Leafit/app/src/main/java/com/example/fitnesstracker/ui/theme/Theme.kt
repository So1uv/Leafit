package com.example.fitnesstracker.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font as GoogleFontFun
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.fitnesstracker.R

/** 3 режима темы: вручную светлая, вручную тёмная или как в системе. */
enum class AppThemeMode { SYSTEM, LIGHT, DARK }

object ThemePreferences {
    private const val PREFS = "ui_theme_prefs"
    private const val KEY_MODE = "theme_mode"

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

private val MintLight = lightColorScheme(
    primary               = Color(0xFF006A60),
    onPrimary             = Color(0xFFFFFFFF),
    primaryContainer      = Color(0xFFA4F2E5),
    onPrimaryContainer    = Color(0xFF00201C),
    secondary             = Color(0xFF4A635E),
    onSecondary           = Color(0xFFFFFFFF),
    secondaryContainer    = Color(0xFFCDE8E1),
    onSecondaryContainer  = Color(0xFF06201B),
    tertiary              = Color(0xFF6650A4),
    onTertiary            = Color(0xFFFFFFFF),
    tertiaryContainer     = Color(0xFFEADDFF),
    onTertiaryContainer   = Color(0xFF21005D),
    error                 = Color(0xFFBA1A1A),
    onError               = Color(0xFFFFFFFF),
    errorContainer        = Color(0xFFFFDAD6),
    onErrorContainer      = Color(0xFF410002),
    background            = Color(0xFFF7FAF8),
    onBackground          = Color(0xFF191C1B),
    surface               = Color(0xFFF7FAF8),
    onSurface             = Color(0xFF191C1B),
    surfaceVariant        = Color(0xFFDCE5E1),
    onSurfaceVariant      = Color(0xFF404946),
    outline               = Color(0xFF707976),
    outlineVariant        = Color(0xFFC0C9C5),
    surfaceContainer      = Color(0xFFF2F6F4),
    surfaceContainerHigh  = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFEFF5F2)
)

private val MintDark = darkColorScheme(
    primary               = Color(0xFF83D6CA),
    onPrimary             = Color(0xFF003731),
    primaryContainer      = Color(0xFF005047),
    onPrimaryContainer    = Color(0xFFA4F2E5),
    secondary             = Color(0xFFB1CCC5),
    onSecondary           = Color(0xFF1C3530),
    secondaryContainer    = Color(0xFF334B46),
    onSecondaryContainer  = Color(0xFFCDE8E1),
    tertiary              = Color(0xFFCCC2DC),
    onTertiary            = Color(0xFF362A53),
    tertiaryContainer     = Color(0xFF4D4169),
    onTertiaryContainer   = Color(0xFFEADDFF),
    error                 = Color(0xFFFFB4AB),
    onError               = Color(0xFF690005),
    errorContainer        = Color(0xFF93000A),
    onErrorContainer      = Color(0xFFFFDAD6),
    background            = Color(0xFF101412),
    onBackground          = Color(0xFFE0E3E0),
    surface               = Color(0xFF101412),
    onSurface             = Color(0xFFE0E3E0),
    surfaceVariant        = Color(0xFF404946),
    onSurfaceVariant      = Color(0xFFC0C9C5),
    outline               = Color(0xFF8A938F),
    outlineVariant        = Color(0xFF404946),
    surfaceContainer      = Color(0xFF171C1A),
    surfaceContainerHigh  = Color(0xFF1F2422),
    surfaceContainerHighest = Color(0xFF2A302D)
)

private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val googleSansFlexName = GoogleFont("Google Sans Flex")

private val GoogleSansFlex = FontFamily(
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.ExtraBold),
    GoogleFontFun(googleFont = googleSansFlexName, fontProvider = googleFontsProvider, weight = FontWeight.Black)
)

/** Алиас для виразних цифр. */
val NumericFontFamily: FontFamily = GoogleSansFlex

private val FitnessTypography = Typography(
    displayLarge = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Black, fontSize = 56.sp, lineHeight = 58.sp, letterSpacing = (-1.2).sp),
    displayMedium = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1.0).sp),
    displaySmall = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Black, fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = (-0.9).sp),
    headlineMedium = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.6).sp),
    headlineSmall = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-0.35).sp),
    titleLarge = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = GoogleSansFlex, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)

private val FitnessShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(18.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

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
    val colorScheme = if (darkTheme) MintDark else MintLight

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = FitnessTypography, shapes = FitnessShapes, content = content)
}
