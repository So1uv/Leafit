package com.example.fitnesstracker.ui.screens.settings;

import android.content.Context;
import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import java.io.File;
import com.example.fitnesstracker.data.entities.Achievement;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.utils.HealthCalc;
import com.example.fitnesstracker.viewmodel.SettingsViewModel;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000Z\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a2\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a$\u0010\f\u001a\u00020\u00012\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001a\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0002\u001a/\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u00112\u0006\u0010\u001b\u001a\u00020\u001cH\u0003\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a\u0010\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\u0011H\u0002\u00a8\u0006\""}, d2 = {"SettingsScreen", "", "vm", "Lcom/example/fitnesstracker/viewmodel/SettingsViewModel;", "onBack", "Lkotlin/Function0;", "EditProfileDialog", "profile", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "onSave", "Lkotlin/Function1;", "onDismiss", "AchievementsListDialog", "achievements", "", "Lcom/example/fitnesstracker/data/entities/Achievement;", "copyAvatarToInternalStorage", "", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "ProfileStatTile", "modifier", "Landroidx/compose/ui/Modifier;", "value", "label", "color", "Landroidx/compose/ui/graphics/Color;", "ProfileStatTile-g2O1Hgs", "(Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/String;J)V", "genderIcon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "g", "app_debug"})
public final class SettingsScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.viewmodel.SettingsViewModel vm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void EditProfileDialog(com.example.fitnesstracker.data.entities.UserProfile profile, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.UserProfile, kotlin.Unit> onSave, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AchievementsListDialog(java.util.List<com.example.fitnesstracker.data.entities.Achievement> achievements, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Копіює вибране зображення у внутрішнє сховище застосунку.
     * Повертає абсолютний шлях до файлу або null при помилці.
     */
    private static final java.lang.String copyAvatarToInternalStorage(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private static final androidx.compose.ui.graphics.vector.ImageVector genderIcon(java.lang.String g) {
        return null;
    }
}