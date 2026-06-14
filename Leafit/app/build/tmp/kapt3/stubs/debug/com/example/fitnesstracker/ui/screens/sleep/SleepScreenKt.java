package com.example.fitnesstracker.ui.screens.sleep;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.unit.Dp;
import com.example.fitnesstracker.data.entities.SleepRecord;
import com.example.fitnesstracker.utils.DateUtils;
import com.example.fitnesstracker.viewmodel.SleepViewModel;
import java.util.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 2, xi = 48, d1 = {"\u0000P\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0010\u0007\n\u0000\u001a \u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a$\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a8\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000b2\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\n2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a@\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0012\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\n2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a$\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00122\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a9\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u00122\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\u0016\b\u0002\u0010\u0018\u001a\u0010\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u0001\u0018\u00010\nH\u0003\u00a2\u0006\u0004\b\u001c\u0010\u001d\u001a&\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a2\u0010$\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u000b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0016\u0010&\u001a\u00020\u00012\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020)0(H\u0003\u00a8\u0006*"}, d2 = {"SleepScreen", "", "vm", "Lcom/example/fitnesstracker/viewmodel/SleepViewModel;", "onBack", "Lkotlin/Function0;", "AddSleepInline", "selectedDay", "", "onSave", "Lkotlin/Function1;", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "SleepRecordCard", "record", "onDelete", "onEdit", "TimeRow", "hour", "", "minute", "onHour", "onMinute", "QualityPicker", "quality", "onSelect", "QualityStars", "size", "Landroidx/compose/ui/unit/Dp;", "QualityStars-lG28NQ4", "(IFLkotlin/jvm/functions/Function1;)V", "TagChip", "text", "", "selected", "", "onClick", "EditSleepDialog", "onDismiss", "SleepWeekChart", "hours", "", "", "app_debug"})
public final class SleepScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SleepScreen(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.viewmodel.SleepViewModel vm, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void AddSleepInline(long selectedDay, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.SleepRecord, kotlin.Unit> onSave) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SleepRecordCard(com.example.fitnesstracker.data.entities.SleepRecord record, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.SleepRecord, kotlin.Unit> onDelete, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.SleepRecord, kotlin.Unit> onEdit) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TimeRow(int hour, int minute, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onHour, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onMinute) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void QualityPicker(int quality, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onSelect) {
    }
    
    /**
     * Тег сну: м'яка заливка без рамок, з галочкою при виборі
     */
    @androidx.compose.runtime.Composable()
    private static final void TagChip(java.lang.String text, boolean selected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    /**
     * Діалог редагування запису сну
     */
    @androidx.compose.runtime.Composable()
    private static final void EditSleepDialog(com.example.fitnesstracker.data.entities.SleepRecord record, kotlin.jvm.functions.Function1<? super com.example.fitnesstracker.data.entities.SleepRecord, kotlin.Unit> onSave, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SleepWeekChart(java.util.List<java.lang.Float> hours) {
    }
}