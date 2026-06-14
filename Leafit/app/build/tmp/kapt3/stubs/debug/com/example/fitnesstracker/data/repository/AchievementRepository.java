package com.example.fitnesstracker.data.repository;

import com.example.fitnesstracker.data.dao.*;
import com.example.fitnesstracker.data.entities.*;
import com.example.fitnesstracker.utils.DateUtils;
import kotlinx.coroutines.flow.Flow;

/**
 * Репозиторій досягнень
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0012\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007J\u0012\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u000e\u0010\u0010\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u001c\u0010\u0012\u001a\u00020\f2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0086@\u00a2\u0006\u0002\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "", "dao", "Lcom/example/fitnesstracker/data/dao/AchievementDao;", "<init>", "(Lcom/example/fitnesstracker/data/dao/AchievementDao;)V", "getAll", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/fitnesstracker/data/entities/Achievement;", "getUnlocked", "unlock", "", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureDefaults", "defaults", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AchievementRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.dao.AchievementDao dao = null;
    
    public AchievementRepository(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.dao.AchievementDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> getAll() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.fitnesstracker.data.entities.Achievement>> getUnlocked() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object unlock(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object resetAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object ensureDefaults(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.fitnesstracker.data.entities.Achievement> defaults, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}