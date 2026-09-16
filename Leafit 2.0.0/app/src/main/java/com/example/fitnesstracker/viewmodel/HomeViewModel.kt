@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

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
    private val achievementRepo: AchievementRepository,
    private val stepRepo: StepRepository,
    private val extrasRepo: com.example.fitnesstracker.data.repository.ExtrasRepository
) : AndroidViewModel(app) {

    val todayNote: kotlinx.coroutines.flow.StateFlow<com.example.fitnesstracker.data.entities.DayNote?> =
        com.example.fitnesstracker.utils.currentDayFlow().flatMapLatest { extrasRepo.getNote(it) }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val noteHistory: kotlinx.coroutines.flow.StateFlow<List<com.example.fitnesstracker.data.entities.DayNote>> =
        extrasRepo.getAllNotes()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveNote(text: String) {
        viewModelScope.launch {
            if (text.isBlank()) extrasRepo.deleteNote(com.example.fitnesstracker.utils.DateUtils.todayStart())
            else extrasRepo.saveNoteText(DateUtils.todayStart(), text.trim())
        }
    }

    fun updateNote(note: DayNote) {
        if (note.text.isBlank()) return
        viewModelScope.launch { extrasRepo.saveNoteText(note.dayStart, note.text.trim()) }
    }

    fun deleteNote(note: DayNote) {
        viewModelScope.launch { extrasRepo.deleteNote(note.dayStart) }
    }

    fun toggleNotePin(note: DayNote) {
        viewModelScope.launch { extrasRepo.toggleNotePin(note.dayStart) }
    }

    val weightHistory = extrasRepo.getWeightHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addWeight(kg: Float) {
        if (!kg.isFinite() || kg !in 20f..350f) return
        viewModelScope.launch {
            extrasRepo.insertWeight(WeightRecord(weightKg = kg))
            userRepo.getProfile().firstOrNull()?.let { userRepo.save(it.copy(weightKg = kg)) }
        }
    }

    fun updateWeight(record: WeightRecord, onResult: (Boolean) -> Unit) {
        if (!record.weightKg.isFinite() || record.weightKg !in 20f..350f) { onResult(false); return }
        viewModelScope.launch {
            val success = try { extrasRepo.updateWeight(record) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { false }
            onResult(success)
        }
    }

    fun deleteWeight(record: WeightRecord, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = try { extrasRepo.deleteWeight(record); true }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { false }
            onResult(success)
        }
    }

    init {
        viewModelScope.launch {
            achievementRepo.ensureDefaults(com.example.fitnesstracker.data.database.AppDatabase.DEFAULT_ACHIEVEMENTS)
            extrasRepo.getAllNotes().collect { achievementRepo.evaluateNotes(it) }
        }
        viewModelScope.launch {
            achievementRepo.ensureDefaults(com.example.fitnesstracker.data.database.AppDatabase.DEFAULT_ACHIEVEMENTS)
            sleepRepo.getAll().collect { achievementRepo.evaluateSleepJournal(it) }
        }
    }

    private val appContext = app.applicationContext
    private val stepCounter = StepCounter.getInstance(appContext)

    val weekSteps: StateFlow<List<com.example.fitnesstracker.data.entities.StepRecord>> =
        stepRepo.getSinceWeekStart()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val profile: StateFlow<UserProfile?> = userRepo.getProfile()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val totalWaterMl: StateFlow<Int> = waterRepo.totalMlToday()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val todayWorkouts: StateFlow<List<Workout>> = workoutRepo.getTodayWorkouts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayCaloriesBurned: StateFlow<Float> = todayWorkouts
        .map { list -> list.sumOf { it.caloriesBurned.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val lastSleep: StateFlow<SleepRecord?> = sleepRepo.getLatest()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val unlockedAchievements: StateFlow<List<Achievement>> = achievementRepo.getUnlocked()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val workoutCount = workoutRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val sleepCount = sleepRepo.getAll().map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAchievements: StateFlow<List<Achievement>> = achievementRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weekActivityMinutes: StateFlow<List<Float>> = workoutRepo.workoutsThisWeek()
        .map { list ->
            val byDay = FloatArray(7)
            list.forEach { w -> byDay[DateUtils.getDayOfWeek(w.startTime)] += w.durationSeconds / 60f }
            byDay.toList()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, List(7) { 0f })

    val steps: StateFlow<Int> = stepCounter.stepsToday
    val stepsSensorAvailable: StateFlow<Boolean> = stepCounter.available

    val weekStepsByDay: StateFlow<List<Int>> = weekSteps
        .map { records ->
            val byDay = IntArray(7)
            records.forEach { r -> byDay[DateUtils.getDayOfWeek(r.dayStart)] = r.steps }
            byDay.toList()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, List(7) { 0 })

    fun startStepTracking() {
        if (com.example.fitnesstracker.utils.StepTrackingPreferences.isBackgroundEnabled(appContext)) {
            StepCounterService.start(appContext)
        }
        stepCounter.start()
    }

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
