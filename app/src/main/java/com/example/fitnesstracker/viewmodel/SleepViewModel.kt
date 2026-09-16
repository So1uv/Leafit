package com.example.fitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.entities.SleepRecord
import com.example.fitnesstracker.data.repository.AchievementRepository
import com.example.fitnesstracker.data.repository.SleepRepository
import com.example.fitnesstracker.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SleepViewModel(
    private val sleepRepo: SleepRepository,
    private val achievementRepo: AchievementRepository
) : ViewModel() {

    private val _selectedDay = MutableStateFlow(DateUtils.todayStart())
    val selectedDay: StateFlow<Long> = _selectedDay

    val history: StateFlow<List<SleepRecord>> = sleepRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weekSleepHours: StateFlow<List<Float>> = sleepRepo.getAll()
        .map { list ->
            val weekStart = DateUtils.weekStart()
            val byDay = FloatArray(7)
            list.filter { it.bedTime >= weekStart }.forEach { r ->
                byDay[DateUtils.getDayOfWeek(r.bedTime)] += (r.wakeTime - r.bedTime) / 3600000f
            }
            byDay.toList()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, List(7) { 0f })

    val latestSleep: StateFlow<SleepRecord?> = sleepRepo.getLatest()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val selectedRecord: StateFlow<SleepRecord?> = _selectedDay
        .flatMapLatest { day -> sleepRepo.getForDay(day) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectDay(ts: Long) { _selectedDay.value = DateUtils.dayStart(ts) }

    fun saveRecord(record: SleepRecord) {
        viewModelScope.launch {
            sleepRepo.insert(record)
            achievementRepo.ensureDefaults(com.example.fitnesstracker.data.database.AppDatabase.DEFAULT_ACHIEVEMENTS)
            achievementRepo.evaluateSleepJournal(sleepRepo.getAll().first())
            achievementRepo.unlock("first_sleep")
            if (sleepRepo.count() >= 7) achievementRepo.unlock("sleep_week")
        }
    }

    fun deleteRecord(record: SleepRecord) {
        viewModelScope.launch { sleepRepo.delete(record) }
    }
}
