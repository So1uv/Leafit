package com.example.fitnesstracker.ui.screens.workout;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import com.example.fitnesstracker.data.entities.Workout;
import com.example.fitnesstracker.utils.DateUtils;
import com.example.fitnesstracker.viewmodel.WorkoutViewModel;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000P\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a$\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001a2\u0010\t\u001a\u00020\u00012\b\b\u0002\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012H\u0003\u001a7\u0010\u0013\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\u0015H\u0003\u00a2\u0006\u0004\b\u0016\u0010\u0017\u001a2\u0010\u0018\u001a\u00020\u00012\u0006\u0010\u0019\u001a\u00020\u001a2\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u001dH\u0003\u00a8\u0006\u001e"}, d2 = {"WorkoutScreen", "", "vm", "Lcom/example/fitnesstracker/viewmodel/WorkoutViewModel;", "WorkoutHistoryCard", "workout", "Lcom/example/fitnesstracker/data/entities/Workout;", "onDelete", "Lkotlin/Function1;", "WorkoutStatChip", "modifier", "Landroidx/compose/ui/Modifier;", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "", "label", "active", "", "WeekStatTile", "color", "Landroidx/compose/ui/graphics/Color;", "WeekStatTile-xwkQ0AY", "(Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;J)V", "WorkoutDatePicker", "selected", "", "onSelect", "onDismiss", "Lkotlin/Function0;", "app_debug"})
public final class WorkoutScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void WorkoutScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.viewmodel.WorkoutViewModel vm) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void WorkoutHistoryCard(com.example.fitnesstracker.data.entities.Workout workout, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.Workout, kotlin.Unit> onDelete) {
    }
    
    /**
     * Матовий чіп статистики всередині таймер-картки
     */
    @androidx.compose.runtime.Composable()
    private static final void WorkoutStatChip(androidx.compose.ui.Modifier modifier, androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String value, java.lang.String label, boolean active) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void WorkoutDatePicker(long selected, kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}