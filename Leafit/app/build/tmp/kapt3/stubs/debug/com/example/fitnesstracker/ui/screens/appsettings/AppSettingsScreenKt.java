package com.example.fitnesstracker.ui.screens.appsettings;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import com.example.fitnesstracker.ui.theme.AppThemeMode;
import com.example.fitnesstracker.utils.WaterReminder;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000<\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000b\u001aN\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a$\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\u00032\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a8\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u000b\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00142\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u0010\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000fH\u0003\u001a=\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u000f2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u00a2\u0006\u0004\b\u001c\u0010\u001d\u001aK\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\u00122\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00a2\u0006\u0004\b!\u0010\"\u001a \u0010#\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u000fH\u0003\u00a8\u0006$"}, d2 = {"AppSettingsScreen", "", "themeMode", "Lcom/example/fitnesstracker/ui/theme/AppThemeMode;", "onThemeModeChange", "Lkotlin/Function1;", "onBack", "Lkotlin/Function0;", "onProfileClick", "onResetData", "ThemeModeSelector", "selected", "onChange", "ThemeModeChip", "text", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "", "modifier", "Landroidx/compose/ui/Modifier;", "onClick", "SettingsGroupLabel", "SettingsClickItem", "iconColor", "Landroidx/compose/ui/graphics/Color;", "title", "subtitle", "SettingsClickItem-iJQMabo", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V", "SettingsSwitchItem", "checked", "onToggle", "SettingsSwitchItem-3IgeMak", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function1;)V", "SettingsInfoItem", "app_debug"})
public final class AppSettingsScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void AppSettingsScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.ui.theme.AppThemeMode themeMode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.ui.theme.AppThemeMode, kotlin.Unit> onThemeModeChange, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onProfileClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onResetData) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ThemeModeSelector(com.example.fitnesstracker.ui.theme.AppThemeMode selected, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.ui.theme.AppThemeMode, kotlin.Unit> onChange) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ThemeModeChip(java.lang.String text, androidx.compose.ui.graphics.vector.ImageVector icon, boolean selected, androidx.compose.ui.Modifier modifier, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsGroupLabel(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsInfoItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle) {
    }
}