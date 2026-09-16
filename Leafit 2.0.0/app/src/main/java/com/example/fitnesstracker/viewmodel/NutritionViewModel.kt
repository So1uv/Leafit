package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.Meal
import com.example.fitnesstracker.data.entities.UserProfile
import com.example.fitnesstracker.data.repository.AchievementRepository
import com.example.fitnesstracker.data.repository.MealRepository
import com.example.fitnesstracker.data.repository.UserRepository
import com.example.fitnesstracker.data.repository.WaterRepository
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.HealthCalc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NutritionViewModel(
    application: android.app.Application,
    private val mealRepo: MealRepository,
    private val userRepo: UserRepository,
    private val achievementRepo: AchievementRepository,
    private val waterRepo: WaterRepository,
    private val extrasRepo: com.example.fitnesstracker.data.repository.ExtrasRepository
) : ViewModel() {

    private val appContext = application.applicationContext

    private val _selectedDay = MutableStateFlow(DateUtils.todayStart())
    val selectedDay: StateFlow<Long> = _selectedDay

    val meals: StateFlow<List<Meal>> = _selectedDay
        .flatMapLatest { day -> mealRepo.getMealsForDay(day) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableDates: StateFlow<List<Long>> = mealRepo.getAvailableDates()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val dailyCalorieGoal: StateFlow<Float> = profile.map { p ->
        p?.let { HealthCalc.dailyCalories(it.weightKg, it.heightCm, it.age, it.gender) } ?: 2000f
    }.stateIn(viewModelScope, SharingStarted.Lazily, 2000f)

    val totalCalories: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.calories.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalProteins: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.proteins.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalFats: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.fats.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalCarbs: StateFlow<Float> = meals
        .map { list -> list.sumOf { it.carbs.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val savedMeals: StateFlow<List<com.example.fitnesstracker.data.entities.SavedMeal>> = mealRepo.getSavedMeals()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weightHistory: StateFlow<List<com.example.fitnesstracker.data.entities.WeightRecord>> =
        extrasRepo.getWeightHistory()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addWeight(kg: Float) {
        viewModelScope.launch {
            extrasRepo.insertWeight(com.example.fitnesstracker.data.entities.WeightRecord(weightKg = kg))
            userRepo.getProfile().firstOrNull()?.let { profile ->
                userRepo.save(profile.copy(weightKg = kg))
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val waterMlToday: StateFlow<Int> = _selectedDay
        .flatMapLatest { day -> waterRepo.getForDay(day) }
        .map { list -> list.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val waterGoalMl: StateFlow<Int> = profile.map { p ->
        com.example.fitnesstracker.utils.GoalPreferences.getWaterGoal(appContext)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 2000)

    fun selectDay(ts: Long) { _selectedDay.value = DateUtils.dayStart(ts) }

    fun addWater(ml: Int) {
        viewModelScope.launch {
            // stamp the record with midday of the selected day so history stays browsable
            val ts = if (_selectedDay.value == DateUtils.todayStart()) System.currentTimeMillis()
                     else _selectedDay.value + 12 * 3600_000L
            waterRepo.insert(com.example.fitnesstracker.data.entities.WaterRecord(timestamp = ts, amountMl = ml))
        }
    }

    fun removeWater() {
        viewModelScope.launch { waterRepo.removeLastForDay(_selectedDay.value) }
    }

    fun saveMealTemplate(meal: Meal) {
        viewModelScope.launch {
            mealRepo.insertSaved(com.example.fitnesstracker.data.entities.SavedMeal(
                name = meal.name, calories = meal.calories,
                proteins = meal.proteins, fats = meal.fats, carbs = meal.carbs
            ))
        }
    }

    fun deleteSavedMeal(saved: com.example.fitnesstracker.data.entities.SavedMeal) {
        viewModelScope.launch { mealRepo.deleteSaved(saved) }
    }

    val products: StateFlow<List<com.example.fitnesstracker.data.entities.FoodProduct>> =
        extrasRepo.getProducts()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addProduct(p: com.example.fitnesstracker.data.entities.FoodProduct) {
        viewModelScope.launch { extrasRepo.insertProduct(p) }
    }

    fun deleteProduct(p: com.example.fitnesstracker.data.entities.FoodProduct) {
        viewModelScope.launch { extrasRepo.deleteProduct(p) }
    }

    fun addFromProduct(p: com.example.fitnesstracker.data.entities.FoodProduct, grams: Float, mealType: String) {
        val k = grams / 100f
        addMeal(Meal(
            mealType = mealType, name = appContext.let { com.example.fitnesstracker.utils.LanguagePreferences.localizedContext(it).getString(com.example.fitnesstracker.R.string.product_serving_name, p.name, grams.toInt()) },
            calories = p.caloriesPer100 * k, proteins = p.proteinsPer100 * k,
            fats = p.fatsPer100 * k, carbs = p.carbsPer100 * k
        ))
    }

    fun addFromSavedScaled(saved: com.example.fitnesstracker.data.entities.SavedMeal, mealType: String, factor: Float) {
        viewModelScope.launch {
            mealRepo.insert(Meal(
                date = _selectedDay.value, mealType = mealType,
                name = if (factor == 1f) saved.name else "${saved.name} ×${if (factor % 1f == 0f) factor.toInt().toString() else factor.toString()}",
                calories = saved.calories * factor, proteins = saved.proteins * factor,
                fats = saved.fats * factor, carbs = saved.carbs * factor
            ))
            achievementRepo.unlock("first_meal")
        }
    }

    fun addFromSaved(saved: com.example.fitnesstracker.data.entities.SavedMeal, mealType: String) {
        viewModelScope.launch {
            mealRepo.insert(Meal(
                date = _selectedDay.value, mealType = mealType, name = saved.name,
                calories = saved.calories, proteins = saved.proteins, fats = saved.fats, carbs = saved.carbs
            ))
            achievementRepo.unlock("first_meal")
        }
    }

    fun addMeal(meal: Meal) {
        viewModelScope.launch {
            mealRepo.insert(meal.copy(date = _selectedDay.value))
            achievementRepo.unlock("first_meal")
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch { mealRepo.delete(meal) }
    }

    fun updateMeal(meal: Meal) {
        viewModelScope.launch { mealRepo.update(meal) }
    }
}
