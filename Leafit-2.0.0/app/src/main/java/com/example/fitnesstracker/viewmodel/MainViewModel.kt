package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.repository.AchievementRepository
import com.example.fitnesstracker.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository
) : ViewModel() {

    val hasProfile: StateFlow<Boolean?> = userRepo.getProfile()
        .map { it != null && it.name.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {

        viewModelScope.launch {
            achievementRepo.ensureDefaults(AppDatabase.DEFAULT_ACHIEVEMENTS)
        }
    }
}
