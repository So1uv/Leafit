package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository,
    private val workoutRepo: WorkoutRepository,
    private val mealRepo: MealRepository,
    private val sleepRepo: SleepRepository,
    private val waterRepo: WaterRepository,
    private val stepRepo: StepRepository,
    private val extrasRepo: com.example.fitnesstracker.data.repository.ExtrasRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<Achievement>> = achievementRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutCount: StateFlow<Int> = workoutRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sleepCount: StateFlow<Int> = sleepRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activityMatrix: StateFlow<List<Float>> = combine(
        workoutRepo.getAll(), stepRepo.getAll(), com.example.fitnesstracker.utils.currentDayFlow()
    ) { workouts, steps, currentDay ->
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(currentDay).atZone(zone).toLocalDate()
        List(49) { i ->
            val date = today.minusDays((48 - i).toLong())
            val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val workoutMin = workouts.filter { it.startTime in start until end }.sumOf { it.durationSeconds } / 60.0
            val daySteps = steps.filter { it.dayStart in start until end }.sumOf { it.steps }
            (workoutMin / 60.0 + daySteps / 10000.0).coerceIn(0.0, 1.0).toFloat()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(49) { 0f })

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            userRepo.save(profile)
            achievementRepo.unlock("profile_complete")
        }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfile()
            userRepo.save(current.copy(avatarUri = uri))
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            com.example.fitnesstracker.workout.WorkoutSessionEngine.current()?.clearJournal()
            workoutRepo.deleteAll()
            stepRepo.clear()
            mealRepo.deleteAll()
            sleepRepo.deleteAll()
            waterRepo.deleteAll()
            achievementRepo.resetAll()
            extrasRepo.clearAll()
            userRepo.clear()
        }
    }
}
