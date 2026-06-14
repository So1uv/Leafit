package com.example.fitnesstracker.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.fitnesstracker.data.database.AppDatabase;
import com.example.fitnesstracker.data.repository.AchievementRepository;
import com.example.fitnesstracker.data.repository.UserRepository;
import kotlinx.coroutines.flow.*;

/**
 * ViewModel для визначення стартового маршруту + ініціалізація досягнень.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\r"}, d2 = {"Lcom/example/fitnesstracker/viewmodel/MainViewModel;", "Landroidx/lifecycle/ViewModel;", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "<init>", "(Lcom/example/fitnesstracker/data/repository/UserRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;)V", "hasProfile", "Lkotlinx/coroutines/flow/StateFlow;", "", "getHasProfile", "()Lkotlinx/coroutines/flow/StateFlow;", "app_debug"})
public final class MainViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.UserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasProfile = null;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.UserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasProfile() {
        return null;
    }
}