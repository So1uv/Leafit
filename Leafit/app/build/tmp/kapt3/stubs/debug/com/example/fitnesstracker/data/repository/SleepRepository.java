package com.example.fitnesstracker.data.repository;

import com.example.fitnesstracker.data.dao.*;
import com.example.fitnesstracker.data.entities.*;
import com.example.fitnesstracker.utils.DateUtils;
import kotlinx.coroutines.flow.Flow;

/**
 * Репозиторій сну
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0012\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007J\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u00072\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\u0007J\u0016\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u000e\u0010\u0013\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/example/fitnesstracker/data/repository/SleepRepository;", "", "dao", "Lcom/example/fitnesstracker/data/dao/SleepDao;", "<init>", "(Lcom/example/fitnesstracker/data/dao/SleepDao;)V", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitnesstracker/data/entities/SleepRecord;", "getForDay", "dayTs", "", "getLatest", "insert", "record", "(Lcom/example/fitnesstracker/data/entities/SleepRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "count", "", "app_debug"})
public final class SleepRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.dao.SleepDao dao = null;
    
    public SleepRepository(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.dao.SleepDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.SleepRecord>> getAll() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.fitnesstracker.data.entities.SleepRecord> getForDay(long dayTs) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.fitnesstracker.data.entities.SleepRecord> getLatest() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.SleepRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object count(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
}