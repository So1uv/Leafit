package com.example.fitnesstracker.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.fitnesstracker.data.entities.SleepRecord;
import com.example.fitnesstracker.data.repository.AchievementRepository;
import com.example.fitnesstracker.data.repository.SleepRepository;
import com.example.fitnesstracker.utils.DateUtils;
import kotlinx.coroutines.flow.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\nJ\u000e\u0010\u001d\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u0011J\u000e\u0010\u001f\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000eR\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00100\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u000eR\u0019\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u000eR\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u000e\u00a8\u0006 "}, d2 = {"Lcom/example/fitnesstracker/viewmodel/SleepViewModel;", "Landroidx/lifecycle/ViewModel;", "sleepRepo", "Lcom/example/fitnesstracker/data/repository/SleepRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "<init>", "(Lcom/example/fitnesstracker/data/repository/SleepRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;)V", "_selectedDay", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "selectedDay", "Lkotlinx/coroutines/flow/StateFlow;", "getSelectedDay", "()Lkotlinx/coroutines/flow/StateFlow;", "history", "", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "getHistory", "weekSleepHours", "", "getWeekSleepHours", "latestSleep", "getLatestSleep", "selectedRecord", "getSelectedRecord", "selectDay", "", "ts", "saveRecord", "record", "deleteRecord", "app_debug"})
public final class SleepViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.SleepRepository sleepRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _selectedDay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> selectedDay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.SleepRecord>> history = null;
    
    /**
     * Години сну по днях поточного тижня (Пн..Нд) для міні-графіка
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Float>> weekSleepHours = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> latestSleep = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> selectedRecord = null;
    
    public SleepViewModel(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.SleepRepository sleepRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getSelectedDay() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.SleepRecord>> getHistory() {
        return null;
    }
    
    /**
     * Години сну по днях поточного тижня (Пн..Нд) для міні-графіка
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Float>> getWeekSleepHours() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> getLatestSleep() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.SleepRecord> getSelectedRecord() {
        return null;
    }
    
    public final void selectDay(long ts) {
    }
    
    public final void saveRecord(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record) {
    }
    
    public final void deleteRecord(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record) {
    }
}