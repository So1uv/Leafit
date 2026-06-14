package com.example.fitnesstracker.ui.screens.nutrition;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ButtonDefaults;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.DatePickerDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.TopAppBarDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import com.example.fitnesstracker.data.entities.Meal;
import com.example.fitnesstracker.utils.DateUtils;
import com.example.fitnesstracker.viewmodel.NutritionViewModel;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000T\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0004\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a0\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006H\u0003\u001a1\u0010\u000b\u001a\u00020\u00012\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0003\u00a2\u0006\u0004\b\u0013\u0010\u0014\u001a\\\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\r2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u00182\u0006\u0010\u001a\u001a\u00020\u001b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u001d2\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u001f2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u001fH\u0003\u001a$\u0010!\u001a\u00020\u00012\u0006\u0010\"\u001a\u00020\u00192\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u001fH\u0003\u001a$\u0010#\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\r2\u0012\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u001fH\u0003\u001a@\u0010$\u001a\u00020\u00012\f\u0010%\u001a\b\u0012\u0004\u0012\u00020&0\u00182\u0006\u0010\'\u001a\u00020&2\u0012\u0010(\u001a\u000e\u0012\u0004\u0012\u00020&\u0012\u0004\u0012\u00020\u00010\u001f2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00010\u001dH\u0003\u00a8\u0006*"}, d2 = {"NutritionScreen", "", "vm", "Lcom/example/fitnesstracker/viewmodel/NutritionViewModel;", "NutritionSummaryCard", "totalCal", "", "goal", "proteins", "fats", "carbs", "MacroItem", "label", "", "value", "color", "Landroidx/compose/ui/graphics/Color;", "modifier", "Landroidx/compose/ui/Modifier;", "MacroItem-9LQNqLg", "(Ljava/lang/String;FJLandroidx/compose/ui/Modifier;)V", "MealSection", "type", "meals", "", "Lcom/example/fitnesstracker/data/entities/Meal;", "expanded", "", "onToggle", "Lkotlin/Function0;", "onAdd", "Lkotlin/Function1;", "onDelete", "MealRow", "meal", "InlineAddMealForm", "DateSelectDialog", "dates", "", "selectedDay", "onSelect", "onDismiss", "app_debug"})
public final class NutritionScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void NutritionScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.viewmodel.NutritionViewModel vm) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NutritionSummaryCard(float totalCal, float goal, float proteins, float fats, float carbs) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void MealSection(java.lang.String type, java.util.List<com.example.fitnesstracker.data.entities.Meal> meals, boolean expanded, kotlin.jvm.functions.Function0<kotlin.Unit> onToggle, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.Meal, kotlin.Unit> onAdd, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.Meal, kotlin.Unit> onDelete) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void MealRow(com.example.fitnesstracker.data.entities.Meal meal, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.Meal, kotlin.Unit> onDelete) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void InlineAddMealForm(java.lang.String type, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.Meal, kotlin.Unit> onAdd) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void DateSelectDialog(java.util.List<java.lang.Long> dates, long selectedDay, kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}