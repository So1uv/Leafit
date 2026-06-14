package com.example.fitnesstracker.ui.screens.home;

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
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.window.PopupProperties;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.data.entities.Achievement;
import com.example.fitnesstracker.utils.HealthCalc;
import com.example.fitnesstracker.viewmodel.HomeViewModel;
import java.io.File;
import java.util.Calendar;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000z\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\"\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\b\u001a\\\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a^\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u0003\u001a-\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00112\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00a2\u0006\u0002\u0010\u001a\u001a7\u0010\u001b\u001a\u00020\u00012\u0006\u0010\u001c\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u001d\u001a\u00020\u001e2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00a2\u0006\u0004\b\u001f\u0010 \u001ax\u0010!\u001a\u00020\u00012\u0006\u0010\"\u001a\u00020\u00112\u0006\u0010#\u001a\u00020\u00112\u0006\u0010$\u001a\u00020\f2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020&2\u0006\u0010)\u001a\u00020&2\u0006\u0010*\u001a\u00020&2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aO\u0010.\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010/\u001a\u00020\u000e2\u0006\u00100\u001a\u00020\u000e2\u0006\u00101\u001a\u00020\u000e2\u0006\u00102\u001a\u00020&2\u0006\u00103\u001a\u0002042\u000e\u0010\u0019\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0005H\u0003\u00a2\u0006\u0004\b5\u00106\u001a:\u00107\u001a\u00020\u00012\u0006\u0010\'\u001a\u00020\u00112\u0006\u0010(\u001a\u00020&2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a2\u00108\u001a\u00020\u00012\u0006\u0010\'\u001a\u00020\u00112\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a2\u00109\u001a\u00020\u00012\u0006\u0010:\u001a\u00020\u00112\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010,2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a&\u0010=\u001a\u00020\u00012\u0006\u0010>\u001a\u00020\u00112\u0006\u0010:\u001a\u00020\f2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a2\u0010?\u001a\u00020\u00012\f\u0010@\u001a\b\u0012\u0004\u0012\u00020\u00110A2\f\u0010B\u001a\b\u0012\u0004\u0012\u00020&0C2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0010\u0010D\u001a\u00020\u00012\u0006\u0010E\u001a\u00020\u000eH\u0003\u001a(\u0010F\u001a\u00020\u00012\b\u0010G\u001a\u0004\u0018\u00010H2\u0006\u0010I\u001a\u00020&2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a?\u0010J\u001a\u00020\u00012\u0006\u0010K\u001a\u00020&2\u0006\u0010L\u001a\u0002042\u0006\u0010M\u001a\u00020&2\u0006\u0010N\u001a\u0002042\u0006\u0010O\u001a\u00020&2\u0006\u0010P\u001a\u000204H\u0003\u00a2\u0006\u0004\bQ\u0010R\u001a\u001f\u0010S\u001a\u00020\u00012\u0006\u0010T\u001a\u00020\u000e2\u0006\u00103\u001a\u000204H\u0003\u00a2\u0006\u0004\bU\u0010V\u001a$\u0010W\u001a\u00020\u00012\f\u0010X\u001a\b\u0012\u0004\u0012\u00020Y0C2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u001f\u0010Z\u001a\u00020\u00012\u0006\u00102\u001a\u00020&2\u0006\u00103\u001a\u000204H\u0003\u00a2\u0006\u0004\b[\u0010\\\u001a\u0010\u0010]\u001a\u00020\u000e2\u0006\u0010^\u001a\u00020\u0011H\u0002\u001a\u0010\u0010_\u001a\u00020\u00172\u0006\u0010^\u001a\u00020\u0011H\u0002\u001a\b\u0010`\u001a\u00020\u000eH\u0002\u00a8\u0006a"}, d2 = {"HomeScreen", "", "vm", "Lcom/example/fitnesstracker/viewmodel/HomeViewModel;", "onSettingsClick", "Lkotlin/Function0;", "onProfileClick", "onSleepClick", "onNutritionClick", "onWorkoutClick", "HomeFloatingHeader", "collapsed", "", "profileName", "", "avatarUri", "achievementsCount", "", "onAchievementsClick", "modifier", "Landroidx/compose/ui/Modifier;", "HeaderIconButton", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "badgeCount", "onClick", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/Integer;Lkotlin/jvm/functions/Function0;)V", "AvatarBubble", "name", "size", "Landroidx/compose/ui/unit/Dp;", "AvatarBubble-d8LSEHM", "(Ljava/lang/String;Ljava/lang/String;FLkotlin/jvm/functions/Function0;)V", "DailySummaryCard", "calories", "steps", "stepsAvailable", "stepsProgress", "", "waterMl", "waterProgress", "calProgress", "sleepProgress", "onAddWater", "Lkotlin/Function1;", "onRemoveWater", "MetricTile", "title", "value", "subtitle", "progress", "color", "Landroidx/compose/ui/graphics/Color;", "MetricTile-hftG7rw", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;FJLkotlin/jvm/functions/Function0;)V", "WaterMetricTile", "WaterActionDock", "WaterVolumePopup", "selected", "onSelect", "onDismiss", "VolumeOptionPill", "volume", "WeekActivityCard", "weekDays", "", "weekMinutes", "", "DatePill", "text", "LastSleepCard", "sleep", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "sleepH", "ActivityRings", "outer", "outerColor", "middle", "middleColor", "inner", "innerColor", "ActivityRings-XHECPz0", "(FJFJFJ)V", "RingLegend", "label", "RingLegend-4WTKRHQ", "(Ljava/lang/String;J)V", "AchievementsDialog", "allAchievements", "Lcom/example/fitnesstracker/data/entities/Achievement;", "ThinProgressBar", "ThinProgressBar-4WTKRHQ", "(FJ)V", "greetingForHour", "hour", "greetingIconForHour", "todayLabel", "app_debug"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.viewmodel.HomeViewModel vm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSettingsClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onProfileClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSleepClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNutritionClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onWorkoutClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void HomeFloatingHeader(boolean collapsed, java.lang.String profileName, java.lang.String avatarUri, int achievementsCount, kotlin.jvm.functions.Function0<kotlin.Unit> onProfileClick, kotlin.jvm.functions.Function0<kotlin.Unit> onAchievementsClick, kotlin.jvm.functions.Function0<kotlin.Unit> onSettingsClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void HeaderIconButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.Integer badgeCount, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DailySummaryCard(int calories, int steps, boolean stepsAvailable, float stepsProgress, int waterMl, float waterProgress, float calProgress, float sleepProgress, kotlin.jvm.functions.Function0<kotlin.Unit> onWorkoutClick, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onAddWater, kotlin.jvm.functions.Function0<kotlin.Unit> onRemoveWater) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void WaterMetricTile(int waterMl, float waterProgress, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onAddWater, kotlin.jvm.functions.Function0<kotlin.Unit> onRemoveWater) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void WaterActionDock(int waterMl, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onAddWater, kotlin.jvm.functions.Function0<kotlin.Unit> onRemoveWater) {
    }
    
    /**
     * Маленьке плаваюче вікно вибору об'єму — спливає над кнопкою налаштувань
     * з пружинною анімацією масштабу, в стилі контекстних меню iOS,
     * але на токенах Material 3.
     */
    @androidx.compose.runtime.Composable()
    private static final void WaterVolumePopup(int selected, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void VolumeOptionPill(int volume, boolean selected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void WeekActivityCard(java.util.Set<java.lang.Integer> weekDays, java.util.List<java.lang.Float> weekMinutes, kotlin.jvm.functions.Function0<kotlin.Unit> onWorkoutClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void DatePill(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LastSleepCard(com.example.fitnesstracker.data.entities.SleepRecord sleep, float sleepH, kotlin.jvm.functions.Function0<kotlin.Unit> onSleepClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AchievementsDialog(java.util.List<com.example.fitnesstracker.data.entities.Achievement> allAchievements, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final java.lang.String greetingForHour(int hour) {
        return null;
    }
    
    private static final androidx.compose.ui.graphics.vector.ImageVector greetingIconForHour(int hour) {
        return null;
    }
    
    private static final java.lang.String todayLabel() {
        return null;
    }
}