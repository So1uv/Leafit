package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.Achievement
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel налаштувань / профілю.
 */
class SettingsViewModel(
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository,
    private val workoutRepo: WorkoutRepository,
    private val mealRepo: MealRepository,
    private val sleepRepo: SleepRepository,
    private val waterRepo: WaterRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val achievements: StateFlow<List<Achievement>> = achievementRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Лічильники для статистики профілю */
    val workoutCount: StateFlow<Int> = workoutRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val sleepCount: StateFlow<Int> = sleepRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            userRepo.save(profile)
            // Розблокуємо досягнення "профіль заповнено"
            achievementRepo.unlock("profile_complete")
        }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfile()
            userRepo.save(current.copy(avatarUri = uri))
        }
    }

    /** Повне скидання: всі таблиці + профіль + досягнення скидаються до початкового стану */
    fun resetAllData() {
        viewModelScope.launch {
            workoutRepo.deleteAll()
            mealRepo.deleteAll()
            sleepRepo.deleteAll()
            waterRepo.deleteAll()
            achievementRepo.resetAll()
            userRepo.clear()
        }
    }
}
