package com.example.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.*
import com.example.fitnesstracker.data.repository.*
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.StepCounter
import com.example.fitnesstracker.utils.StepCounterService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    app: Application,
    private val userRepo: UserRepository,
    private val waterRepo: WaterRepository,
    private val workoutRepo: WorkoutRepository,
    private val sleepRepo: SleepRepository,
    private val achievementRepo: AchievementRepository
) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val stepCounter = StepCounter(appContext)

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalWaterMl: StateFlow<Int> = waterRepo.totalMlToday()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayWorkouts: StateFlow<List<Workout>> = workoutRepo.getTodayWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCaloriesBurned: StateFlow<Float> = todayWorkouts
        .map { list -> list.sumOf { it.caloriesBurned.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val lastSleep: StateFlow<SleepRecord?> = sleepRepo.getLatest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unlockedAchievements: StateFlow<List<Achievement>> = achievementRepo.getUnlocked()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievements: StateFlow<List<Achievement>> = achievementRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekActivityDays: StateFlow<Set<Int>> = workoutRepo.workoutDaysThisWeek()
        .map { timestamps -> timestamps.map { DateUtils.getDayOfWeek(it) }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Хвилини активності по днях тижня (Пн..Нд) для графіка */
    val weekActivityMinutes: StateFlow<List<Float>> = workoutRepo.workoutsThisWeek()
        .map { list ->
            val byDay = FloatArray(7)
            list.forEach { w -> byDay[DateUtils.getDayOfWeek(w.startTime)] += w.durationSeconds / 60f }
            byDay.toList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0f })

    val steps: StateFlow<Int> = stepCounter.stepsToday
    val stepsSensorAvailable: StateFlow<Boolean> = stepCounter.available

    fun startStepTracking() {
        StepCounterService.start(appContext)
        stepCounter.start()
    }

    /**
     * Зупиняємо тільки локальний listener екрана. Foreground service лишається
     * активним, щоб кроки рахувалися після згортання або закриття застосунку.
     */
    fun stopStepTracking()  { stepCounter.stop() }

    fun addWater(ml: Int) {
        viewModelScope.launch { waterRepo.insert(WaterRecord(amountMl = ml)) }
    }

    fun removeLastWater() {
        viewModelScope.launch { waterRepo.removeLastToday() }
    }

    override fun onCleared() {
        super.onCleared()
        stepCounter.release()
    }
}
