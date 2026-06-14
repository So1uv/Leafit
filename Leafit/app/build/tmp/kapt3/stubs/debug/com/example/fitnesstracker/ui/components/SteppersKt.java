package com.example.fitnesstracker.ui.components;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.input.KeyboardType;
import java.util.Locale;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\u001a$\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001aH\u0010\u0005\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u0007H\u0007\u001a`\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\t2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00122\b\b\u0002\u0010\u0014\u001a\u00020\u00152\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\tH\u0007\u00a8\u0006\u0017"}, d2 = {"StepperArrows", "", "onUp", "Lkotlin/Function0;", "onDown", "StepperIntField", "value", "", "label", "", "range", "Lkotlin/ranges/IntRange;", "onChange", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "step", "StepperDecimalField", "", "max", "isError", "", "supportingText", "app_debug"})
public final class SteppersKt {
    
    /**
     * Вертикальна пара стрілочок ▲▼ у стилі застосунку
     */
    @androidx.compose.runtime.Composable()
    private static final void StepperArrows(kotlin.jvm.functions.Function0<kotlin.Unit> onUp, kotlin.jvm.functions.Function0<kotlin.Unit> onDown) {
    }
    
    /**
     * Числове поле зі стрілочками для Int-значень з циклічним переходом (години/хвилини)
     */
    @androidx.compose.runtime.Composable()
    public static final void StepperIntField(int value, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    kotlin.ranges.IntRange range, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onChange, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, int step) {
    }
    
    /**
     * Числове поле зі стрілочками для String-значень (вік, зріст, вага, калорії)
     */
    @androidx.compose.runtime.Composable()
    public static final void StepperDecimalField(@org.jetbrains.annotations.NotNull()
    java.lang.String value, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChange, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, float step, float max, boolean isError, @org.jetbrains.annotations.Nullable()
    java.lang.String supportingText) {
    }
}