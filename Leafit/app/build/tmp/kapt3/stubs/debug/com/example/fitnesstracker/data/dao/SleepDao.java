package com.example.fitnesstracker.data.dao;

import androidx.room.*;
import com.example.fitnesstracker.data.entities.SleepRecord;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0014\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\'J \u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u00032\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\'J\u0010\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0003H\'J\u0016\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u000e\u0010\u0013\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006\u0014\u00c0\u0006\u0003"}, d2 = {"Lcom/example/fitnesstracker/data/dao/SleepDao;", "", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "getForDay", "dayStart", "", "dayEnd", "getLatest", "insert", "record", "(Lcom/example/fitnesstracker/data/entities/SleepRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "count", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "app_debug"})
@androidx.room.Dao()
public abstract interface SleepDao {
    
    @androidx.room.Query(value = "SELECT * FROM sleep_records ORDER BY bedTime DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.SleepRecord>> getAll();
    
    @androidx.room.Query(value = "SELECT * FROM sleep_records WHERE bedTime >= :dayStart AND bedTime < :dayEnd LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.example.fitnesstracker.data.entities.SleepRecord> getForDay(long dayStart, long dayEnd);
    
    @androidx.room.Query(value = "SELECT * FROM sleep_records ORDER BY bedTime DESC LIMIT 1")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.example.fitnesstracker.data.entities.SleepRecord> getLatest();
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM sleep_records")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object count(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "DELETE FROM sleep_records")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}