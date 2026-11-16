package com.example.fitnesstracker.workout

import com.example.fitnesstracker.data.entities.Workout
import java.util.Locale

data class ExerciseBest(val maxReps: Int = 0, val maxWeightKg: Float = 0f, val sessions: Int = 0,
    val maxSeconds: Int = 0, val maxMeters: Float = 0f, val maxScore: Int = 0)

fun exerciseKey(name: String, mode: EntryMode): String = mode.name + ":" + name.trim().lowercase(Locale.ROOT)

fun JournalExercise.beats(previous: ExerciseBest?): Boolean = sets.any { set ->
    set.completed && set.hasResult(mode) && when (mode) {
        EntryMode.REPS -> (set.weightKg ?: 0f) > (previous?.maxWeightKg ?: 0f) || (set.reps ?: 0) > (previous?.maxReps ?: 0)
        EntryMode.TIME -> (set.seconds ?: 0) > (previous?.maxSeconds ?: 0)
        EntryMode.DISTANCE -> (set.distanceMeters ?: 0f) > (previous?.maxMeters ?: 0f)
        EntryMode.SCORE -> (set.score ?: 0) > (previous?.maxScore ?: 0)
    }
}

fun exerciseBests(history: List<Workout>): Map<String, ExerciseBest> {
    val bests = mutableMapOf<String, ExerciseBest>()
    history.forEach { workout ->
        val plan = WorkoutJournal.decode(workout.metrics) ?: return@forEach
        plan.exercises.groupBy { exerciseKey(it.name, it.mode) }.forEach { (key, entries) ->
            val completed = entries.flatMap { e -> e.sets.filter { it.completed && it.hasResult(e.mode) } }
            if (completed.isNotEmpty()) {
                val old = bests[key] ?: ExerciseBest()
                bests[key] = ExerciseBest(
                    maxOf(old.maxReps, completed.maxOf { it.reps ?: 0 }),
                    maxOf(old.maxWeightKg, completed.maxOf { it.weightKg ?: 0f }), old.sessions + 1,
                    maxOf(old.maxSeconds, completed.maxOf { it.seconds ?: 0 }),
                    maxOf(old.maxMeters, completed.maxOf { it.distanceMeters ?: 0f }),
                    maxOf(old.maxScore, completed.maxOf { it.score ?: 0 }))
            }
        }
    }
    return bests
}
