package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.repository.AchievementRepository
import com.example.fitnesstracker.data.repository.MealRepository
import com.example.fitnesstracker.data.repository.UserRepository
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.HealthCalc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NutritionViewModel(
    private val mealRepo: MealRepository,
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(DateUtils.todayStart())
    val selectedDay: StateFlow<Long> = _selectedDay

    val meals: StateFlow<List<Meal>> = _selectedDay
        .flatMapLatest { day -> mealRepo.getMealsForDay(day) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableDates: StateFlow<List<Long>> = mealRepo.getAvailableDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyCalorieGoal: StateFlow<Float> = profile.map { p ->
        p?.let { HealthCalc.dailyCalories(it.weightKg, it.heightCm, it.age, it.gender) } ?: 2000f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000f)

    val totalCalories: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.calories.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalProteins: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.proteins.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalFats: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.fats.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalCarbs: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.carbs.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun selectDay(ts: Long) { _selectedDay.value = DateUtils.dayStart(ts) }

    fun addMeal(meal: Meal) {
        viewModelScope.launch {
            mealRepo.insert(meal.copy(date = _selectedDay.value))
            achievementRepo.unlock("first_meal")
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch { mealRepo.delete(meal) }
    }
}
