package com.example.fitnesstracker.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.fitnesstracker.data.entities.Workout;
import com.example.fitnesstracker.data.entities.UserProfile;
import com.example.fitnesstracker.data.repository.AchievementRepository;
import com.example.fitnesstracker.data.repository.WorkoutRepository;
import com.example.fitnesstracker.data.repository.UserRepository;
import com.example.fitnesstracker.utils.HealthCalc;
import com.example.fitnesstracker.utils.StepCounter;
import kotlinx.coroutines.flow.*;

/**
 * ViewModel тренування з підрахунком кроків через датчик.
 */
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\'\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0004\b\n\u0010\u000bJ\u0006\u0010-\u001a\u00020.J\u0006\u0010/\u001a\u00020.J\u0006\u00100\u001a\u00020.J\u0010\u00101\u001a\u00020.2\b\b\u0002\u00102\u001a\u000203J\u0006\u00104\u001a\u00020.J\u000e\u00105\u001a\u00020.2\u0006\u00106\u001a\u00020\u0011J\b\u00107\u001a\u00020.H\u0002J\b\u00108\u001a\u00020.H\u0002J\b\u00109\u001a\u00020.H\u0014R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0016\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00150\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00180\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00180\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0013R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001d0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0013R\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001d0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010!\u001a\u0004\u0018\u00010\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010%\u001a\b\u0012\u0004\u0012\u00020$0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020$0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0013R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00180\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0013R\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020+0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u0013\u00a8\u0006:"}, d2 = {"Lcom/example/fitnesstracker/viewmodel/WorkoutViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "app", "Landroid/app/Application;", "workoutRepo", "Lcom/example/fitnesstracker/data/repository/WorkoutRepository;", "userRepo", "Lcom/example/fitnesstracker/data/repository/UserRepository;", "achievementRepo", "Lcom/example/fitnesstracker/data/repository/AchievementRepository;", "<init>", "(Landroid/app/Application;Lcom/example/fitnesstracker/data/repository/WorkoutRepository;Lcom/example/fitnesstracker/data/repository/UserRepository;Lcom/example/fitnesstracker/data/repository/AchievementRepository;)V", "stepCounter", "Lcom/example/fitnesstracker/utils/StepCounter;", "history", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/example/fitnesstracker/data/entities/Workout;", "getHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "profile", "Lcom/example/fitnesstracker/data/entities/UserProfile;", "_isRunning", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "isRunning", "_isPaused", "isPaused", "_elapsedSeconds", "", "elapsedSeconds", "getElapsedSeconds", "_startTime", "timerJob", "Lkotlinx/coroutines/Job;", "stepsAtWorkoutStart", "", "_workoutSteps", "workoutSteps", "getWorkoutSteps", "stepsSensorAvailable", "getStepsSensorAvailable", "estimatedCalories", "", "getEstimatedCalories", "startWorkout", "", "pauseWorkout", "resumeWorkout", "stopAndSave", "note", "", "stopAndDiscard", "deleteWorkout", "workout", "launchTimer", "reset", "onCleared", "app_debug"})
public final class WorkoutViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.UserRepository userRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.fitnesstracker.utils.StepCounter stepCounter = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Workout>> history = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.UserProfile> profile = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isRunning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isRunning = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isPaused = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPaused = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _elapsedSeconds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> elapsedSeconds = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _startTime = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job timerJob;
    private int stepsAtWorkoutStart = -1;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _workoutSteps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> workoutSteps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> stepsSensorAvailable = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Float> estimatedCalories = null;
    
    public WorkoutViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application app, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.WorkoutRepository workoutRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.UserRepository userRepo, @org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.repository.AchievementRepository achievementRepo) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.fitnesstracker.data.entities.Workout>> getHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isRunning() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPaused() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getElapsedSeconds() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getWorkoutSteps() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getStepsSensorAvailable() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Float> getEstimatedCalories() {
        return null;
    }
    
    public final void startWorkout() {
    }
    
    public final void pauseWorkout() {
    }
    
    public final void resumeWorkout() {
    }
    
    public final void stopAndSave(@org.jetbrains.annotations.NotNull()
    java.lang.String note) {
    }
    
    public final void stopAndDiscard() {
    }
    
    public final void deleteWorkout(@org.jetbrains.annotations.NotNull()
    com.example.fitnesstracker.data.entities.Workout workout) {
    }
    
    private final void launchTimer() {
    }
    
    private final void reset() {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}