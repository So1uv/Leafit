package com.example.fitnesstracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.workout.*

class WorkoutViewModel(app: Application) : AndroidViewModel(app) {
    private val session = WorkoutSessionEngine.get(app)
    val history = session.history
    val ready = session.ready
    val isRunning = session.isRunning
    val isPaused = session.isPaused
    val elapsedSeconds = session.elapsedSeconds
    val workoutSteps = session.workoutSteps
    val stepsSensorAvailable = session.stepsSensorAvailable
    val plan = session.plan
    val workoutType = session.workoutType
    val templates = session.templates
    val restRemaining = session.restRemaining
    val setTimer = session.setTimer
    val setSeconds = session.setSeconds
    val saving = session.saving
    val message = session.message
    val finishRequested = session.finishRequested
    fun clearMessage() = session.clearMessage()
    fun setWorkoutType(type: String) = session.setWorkoutType(type)
    fun updatePlan(value: WorkoutPlan) = session.updatePlan(value)
    fun saveExercise(exercise: JournalExercise) = session.saveExercise(exercise)
    fun deleteExercise(id: String) = session.deleteExercise(id)
    fun moveExercise(id: String, delta: Int) = session.moveExercise(id, delta)
    fun startSetTimer(exerciseId: String, setId: String) = session.startSetTimer(exerciseId, setId)
    fun finishSetTimer() = session.finishSetTimer()
    fun toggleSet(exerciseId: String, setId: String) = session.toggleSet(exerciseId, setId)
    fun skipRest() = session.skipRest()
    fun saveTemplate(name: String) = session.saveTemplate(name)
    fun useTemplate(template: WorkoutTemplate) = session.useTemplate(template)
    fun deleteTemplate(id: String) = session.deleteTemplate(id)
    fun startWorkout() = session.startWorkout()
    fun pauseWorkout() = session.pauseWorkout()
    fun resumeWorkout() = session.resumeWorkout()
    fun stopAndSave(note: String = "") = session.stopAndSave(note)
    fun stopAndDiscard() = session.stopAndDiscard()
    fun deleteWorkout(workout: Workout) = session.deleteWorkout(workout)
    fun updateWorkout(workout: Workout, onResult: (Boolean) -> Unit) = session.updateWorkout(workout, onResult)
    fun consumeFinishRequest() = session.consumeFinishRequest()
}
