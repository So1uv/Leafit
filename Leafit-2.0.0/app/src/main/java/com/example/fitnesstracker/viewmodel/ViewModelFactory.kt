package com.example.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.repository.*

class ViewModelFactory(
    private val application: Application,
    private val db: AppDatabase
) : ViewModelProvider.Factory {

    private val userRepo    by lazy { UserRepository(db.userProfileDao()) }
    private val workoutRepo by lazy { WorkoutRepository(db.workoutDao()) }
    private val mealRepo    by lazy { MealRepository(db.mealDao()) }
    private val sleepRepo   by lazy { SleepRepository(db.sleepDao()) }
    private val waterRepo   by lazy { WaterRepository(db.waterDao()) }
    private val extrasRepo  by lazy { com.example.fitnesstracker.data.repository.ExtrasRepository(db.extrasDao()) }
    private val achieveRepo by lazy { AchievementRepository(db.achievementDao()) }
    private val stepRepo    by lazy { StepRepository(db.stepDao()) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(application, userRepo, waterRepo, workoutRepo, sleepRepo, achieveRepo, stepRepo, extrasRepo) as T
        modelClass.isAssignableFrom(WorkoutViewModel::class.java) ->
            WorkoutViewModel(application, workoutRepo, userRepo, achieveRepo, stepRepo) as T
        modelClass.isAssignableFrom(NutritionViewModel::class.java) ->
            NutritionViewModel(application, mealRepo, userRepo, achieveRepo, waterRepo, extrasRepo) as T
        modelClass.isAssignableFrom(SleepViewModel::class.java) ->
            SleepViewModel(sleepRepo, achieveRepo) as T
        modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
            SettingsViewModel(userRepo, achieveRepo, workoutRepo, mealRepo, sleepRepo, waterRepo, stepRepo, extrasRepo) as T
        modelClass.isAssignableFrom(MainViewModel::class.java) ->
            MainViewModel(userRepo, achieveRepo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
