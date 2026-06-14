package com.example.fitnesstracker.data.dao;

import androidx.room.*;
import com.example.fitnesstracker.data.entities.WaterRecord;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H\'J \u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H\'J \u0010\u000b\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\r\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0012\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0013\u00a8\u0006\u0014\u00c0\u0006\u0003"}, d2 = {"Lcom/example/fitnesstracker/data/dao/WaterDao;", "", "getForDay", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitnesstracker/data/entities/WaterRecord;", "dayStart", "", "dayEnd", "totalMlForDay", "", "getLatestToday", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "record", "(Lcom/example/fitnesstracker/data/entities/WaterRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao()
public abstract interface WaterDao {
    
    @androidx.room.Query(value = "SELECT * FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.WaterRecord>> getForDay(long dayStart, long dayEnd);
    
    @androidx.room.Query(value = "SELECT SUM(amountMl) FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> totalMlForDay(long dayStart, long dayEnd);
    
    @androidx.room.Query(value = "SELECT * FROM water_records WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp DESC LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLatestToday(long dayStart, long dayEnd, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.fitnesstracker.data.entities.WaterRecord> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.WaterRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.WaterRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM water_records")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}