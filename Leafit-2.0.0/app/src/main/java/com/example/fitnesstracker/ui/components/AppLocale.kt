package com.example.fitnesstracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.fitnesstracker.utils.LanguagePreferences
import java.util.Locale

/** Date formatters follow the in-app language, not the process/system default. */
@Composable
fun appLocale(): Locale = Locale.forLanguageTag(LanguagePreferences.get(LocalContext.current).code)
