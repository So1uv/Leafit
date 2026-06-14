package com.example.fitnesstracker.ui.screens.onboarding;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.utils.HealthCalc;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\u001a\u001c\u0010\u0000\u001a\u00020\u00012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001a\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002\u00a8\u0006\t"}, d2 = {"OnboardingScreen", "", "onFinish", "Lkotlin/Function1;", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "genderIcon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "g", "", "app_debug"})
public final class OnboardingScreenKt {
    
    /**
     * Екран первинного налаштування з валідацією:
     * вік 5–100, зріст 100–220 см, вага 20–645 кг.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void OnboardingScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.UserProfile, kotlin.Unit> onFinish) {
    }
    
    private static final androidx.compose.ui.graphics.vector.ImageVector genderIcon(java.lang.String g) {
        return null;
    }
}