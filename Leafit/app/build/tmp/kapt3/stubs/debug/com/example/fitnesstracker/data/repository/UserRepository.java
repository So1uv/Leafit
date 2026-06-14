package com.example.fitnesstracker.data.repository;

import com.example.fitnesstracker.data.dao.*;
import com.example.fitnesstracker.data.entities.*;
import com.example.fitnesstracker.utils.DateUtils;
import kotlinx.coroutines.flow.Flow;

/**
 * Репозиторій профілю
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u000e\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0007J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\r\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/example/fitnesstracker/data/repository/UserRepository;", "", "dao", "Lcom/example/fitnesstracker/data/dao/UserProfileDao;", "<init>", "(Lcom/example/fitnesstracker/data/dao/UserProfileDao;)V", "getProfile", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "save", "", "profile", "(Lcom/example/fitnesstracker/data/entities/UserProfile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clear", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class UserRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.dao.UserProfileDao dao = null;
    
    public UserRepository(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.dao.UserProfileDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.example.fitnesstracker.data.entities.UserProfile> getProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object save(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.UserProfile profile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clear(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}