package com.example.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.repository.AchievementRepository
import com.example.fitnesstracker.data.repository.WorkoutRepository
import com.example.fitnesstracker.data.repository.UserRepository
import com.example.fitnesstracker.utils.HealthCalc
import com.example.fitnesstracker.utils.StepCounter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel тренування з підрахунком кроків через датчик.
 */
class WorkoutViewModel(
    app: Application,
    private val workoutRepo: WorkoutRepository,
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository
) : AndroidViewModel(app) {

    private val stepCounter = StepCounter(app.applicationContext)

    val history: StateFlow<List<Workout>> = workoutRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _startTime = MutableStateFlow(0L)
    private var timerJob: Job? = null

    // Кроки під час тренування: поточне значення датчика мінус базове на початку тренування
    private var stepsAtWorkoutStart: Int = -1
    private val _workoutSteps = MutableStateFlow(0)
    val workoutSteps: StateFlow<Int> = _workoutSteps

    val stepsSensorAvailable: StateFlow<Boolean> = stepCounter.available

    val estimatedCalories: StateFlow<Float> = combine(_elapsedSeconds, profile) { sec, prof ->
        HealthCalc.workoutCalories(prof?.weightKg ?: 70f, sec)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun startWorkout() {
        _isRunning.value = true
        _isPaused.value = false
        _elapsedSeconds.value = 0L
        _workoutSteps.value = 0
        _startTime.value = System.currentTimeMillis()

        // Запускаємо датчик і запам'ятовуємо базове значення стартових кроків дня
        stepCounter.start()
        stepsAtWorkoutStart = stepCounter.stepsToday.value

        // Слідкуємо за зміною кроків: дельта відносно моменту старту тренування
        viewModelScope.launch {
            stepCounter.stepsToday.collect { totalToday ->
                if (stepsAtWorkoutStart >= 0 && _isRunning.value) {
                    _workoutSteps.value = (totalToday - stepsAtWorkoutStart).coerceAtLeast(0)
                }
            }
        }

        launchTimer()
    }

    fun pauseWorkout() {
        _isPaused.value = true
        timerJob?.cancel()
        stepCounter.stop()
    }

    fun resumeWorkout() {
        _isPaused.value = false
        stepCounter.start()
        launchTimer()
    }

    fun stopAndSave(note: String = "") {
        timerJob?.cancel()
        stepCounter.stop()
        val duration = _elapsedSeconds.value
        val cal = estimatedCalories.value
        val steps = _workoutSteps.value
        viewModelScope.launch {
            workoutRepo.insert(Workout(
                startTime = _startTime.value,
                endTime = System.currentTimeMillis(),
                durationSeconds = duration,
                steps = steps,
                caloriesBurned = cal,
                note = note
            ))
            // Тригери досягнень
            val total = workoutRepo.count()
            achievementRepo.unlock("first_workout")
            if (total >= 10) achievementRepo.unlock("ten_workouts")
            reset()
        }
    }

    fun stopAndDiscard() {
        timerJob?.cancel()
        stepCounter.stop()
        reset()
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch { workoutRepo.delete(workout) }
    }

    private fun launchTimer() {
        timerJob = viewModelScope.launch {
            while (true) { delay(1000); _elapsedSeconds.value++ }
        }
    }

    private fun reset() {
        _isRunning.value = false
        _isPaused.value = false
        _elapsedSeconds.value = 0L
        _workoutSteps.value = 0
        stepsAtWorkoutStart = -1
    }

    override fun onCleared() {
        super.onCleared()
        stepCounter.stop()
    }
}
