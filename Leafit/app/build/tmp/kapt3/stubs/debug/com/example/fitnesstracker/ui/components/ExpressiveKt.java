package com.example.fitnesstracker.ui.components;

import androidx.compose.animation.core.Spring;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.layout.ColumnScope;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.StrokeJoin;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.unit.Dp;
import androidx.compose.ui.window.DialogProperties;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\u001ay\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0015\b\u0002\u0010\u0004\u001a\u000f\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0003\u00a2\u0006\u0002\b\u00052\u0011\u0010\u0006\u001a\r\u0012\u0004\u0012\u00020\u00010\u0003\u00a2\u0006\u0002\b\u00052\u0015\b\u0002\u0010\u0007\u001a\u000f\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0003\u00a2\u0006\u0002\b\u00052 \b\u0002\u0010\b\u001a\u001a\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\t\u00a2\u0006\u0002\b\u0005\u00a2\u0006\u0002\b\u000bH\u0007\u001a3\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u0007\u00a2\u0006\u0004\b\u0015\u0010\u0016\u001a&\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u0011\u001a\u00020\u00122\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0007\u00a8\u0006\u001c"}, d2 = {"ExpressiveDialog", "", "onDismissRequest", "Lkotlin/Function0;", "title", "Landroidx/compose/runtime/Composable;", "confirmButton", "dismissButton", "content", "Lkotlin/Function1;", "Landroidx/compose/foundation/layout/ColumnScope;", "Lkotlin/ExtensionFunctionType;", "GlowProgressBar", "progress", "", "color", "Landroidx/compose/ui/graphics/Color;", "modifier", "Landroidx/compose/ui/Modifier;", "height", "Landroidx/compose/ui/unit/Dp;", "GlowProgressBar-fOGDGlw", "(FJLandroidx/compose/ui/Modifier;F)V", "LargeScreenTitle", "text", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "app_debug"})
public final class ExpressiveKt {
    
    /**
     * Єдиний «мова» спливаючих вікон застосунку: радіус 28dp, surfaceContainerHigh
     * та пружинна pop-анімація масштабу — як у water popup на головному екрані.
     * Заміна стандартного AlertDialog.
     */
    @androidx.compose.runtime.Composable()
    public static final void ExpressiveDialog(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, @org.jetbrains.annotations.Nullable()
    androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> title, @org.jetbrains.annotations.NotNull()
    androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> confirmButton, @org.jetbrains.annotations.Nullable()
    androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> dismissButton, @org.jetbrains.annotations.Nullable()
    androidx.compose.runtime.internal.ComposableFunction1<? super androidx.compose.foundation.layout.ColumnScope, kotlin.Unit> content) {
    }
    
    /**
     * Великий заголовок екрана у вигляді pill-плашки (M3 Expressive).
     * Підтримує опціональну іконку зліва, відкидає легку тінь та має тональний акцент.
     */
    @androidx.compose.runtime.Composable()
    public static final void LargeScreenTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.Nullable()
    androidx.compose.ui.graphics.vector.ImageVector icon) {
    }
}