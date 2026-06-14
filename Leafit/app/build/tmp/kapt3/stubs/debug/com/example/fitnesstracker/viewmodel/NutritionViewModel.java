package com.example.fitnesstracker.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.fitnesstracker.data.entities.Meal;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.data.repository.AchievementRepository;
import com.example.fitnesstracker.data.repository.MealRepository;
import com.example.fitnesstracker.data.repository.UserRepository;
import com.example.fitnesstracker.utils.DateUtils;
import com.example.fitnesstracker.utils.HealthCalc;
import kotlinx.coroutines.flow.*;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\u000e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\fJ\u000e\u0010(\u001a\u00020&2\u0006\u0010)\u001a\u00020\u0013J\u000e\u0010*\u001a\u00020&2\u0006\u0010)\u001a\u00020\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00120\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0010R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00120\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u0019\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0010R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0010R\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0010R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0010R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0010R\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020\u001b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0010\u00a8\u0006+"}, d2 = {"Lcom/example/fitnesstracker/viewmodel/NutritionViewModel;", "Landroidx/lifecycle/ViewModel;", "mealRepo", "Lcom/example/fitnesstracker/data/repository/MealRepository;", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "<init>", "(Lcom/example/fitnesstracker/data/repository/MealRepository;Lcom/example/fitnesstracker/data/repository/UserRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;)V", "_selectedDay", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "selectedDay", "Lkotlinx/coroutines/flow/StateFlow;", "getSelectedDay", "()Lkotlinx/coroutines/flow/StateFlow;", "meals", "", "Lcom/example/fitnesstracker/data/entities/Meal;", "getMeals", "availableDates", "getAvailableDates", "profile", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "getProfile", "dailyCalorieGoal", "", "getDailyCalorieGoal", "totalCalories", "getTotalCalories", "totalProteins", "getTotalProteins", "totalFats", "getTotalFats", "totalCarbs", "getTotalCarbs", "selectDay", "", "ts", "addMeal", "meal", "deleteMeal", "app_debug"})
public final class NutritionViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.MealRepository mealRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.UserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _selectedDay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> selectedDay = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Meal>> meals = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Long>> availableDates = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> profile = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> dailyCalorieGoal = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> totalCalories = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> totalProteins = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> totalFats = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> totalCarbs = null;
    
    public NutritionViewModel(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.MealRepository mealRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.UserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getSelectedDay() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Meal>> getMeals() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Long>> getAvailableDates() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> getProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getDailyCalorieGoal() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getTotalCalories() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getTotalProteins() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getTotalFats() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getTotalCarbs() {
        return null;
    }
    
    public final void selectDay(long ts) {
    }
    
    public final void addMeal(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.Meal meal) {
    }
    
    public final void deleteMeal(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.Meal meal) {
    }
}