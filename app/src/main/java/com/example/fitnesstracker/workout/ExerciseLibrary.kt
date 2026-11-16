package com.example.fitnesstracker.workout

import com.example.fitnesstracker.R

data class ExerciseInfo(val muscle: String, val equipment: String, val techniqueRes: Int)

object ExerciseLibrary {
    val equipment = listOf("bodyweight", "mat", "dumbbell", "barbell", "machine",
        "bands", "pullup", "treadmill", "bike", "pool", "outdoor")
    val muscles = listOf("legs", "chest", "back", "shoulders", "arms", "core", "cardio", "mobility")

    fun info(preset: ExercisePreset): ExerciseInfo = when (preset.labelRes) {
        R.string.exercise_squat -> ExerciseInfo("legs", "bodyweight", R.string.tech_squat)
        R.string.exercise_lunge -> ExerciseInfo("legs", "bodyweight", R.string.tech_lunge)
        R.string.exercise_push_up -> ExerciseInfo("chest", "bodyweight", R.string.tech_pushup)
        R.string.exercise_pull_up -> ExerciseInfo("back", "pullup", R.string.tech_pullup)
        R.string.exercise_bench_press -> ExerciseInfo("chest", "barbell", R.string.tech_bench)
        R.string.exercise_dumbbell_press -> ExerciseInfo("chest", "dumbbell", R.string.tech_press)
        R.string.exercise_dumbbell_row -> ExerciseInfo("back", "dumbbell", R.string.tech_row)
        R.string.exercise_plank, R.string.exercise_side_plank -> ExerciseInfo("core", "mat", R.string.tech_plank)
        R.string.exercise_leg_press, R.string.exercise_leg_curl ->
            ExerciseInfo("legs", "machine", R.string.tech_machine)
        R.string.exercise_cable_row, R.string.exercise_lat_pull ->
            ExerciseInfo("back", "machine", R.string.tech_machine)
        R.string.exercise_overhead_press -> ExerciseInfo("shoulders", "dumbbell", R.string.tech_press)
        R.string.exercise_biceps_curl -> ExerciseInfo("arms", "dumbbell", R.string.tech_curl)
        R.string.exercise_triceps_extension -> ExerciseInfo("arms", "dumbbell", R.string.tech_triceps)
        R.string.exercise_calf_raise -> ExerciseInfo("legs", "bodyweight", R.string.tech_calf)
        R.string.exercise_bridge, R.string.exercise_tree_pose, R.string.exercise_child_pose,
        R.string.exercise_warrior_pose, R.string.exercise_down_dog, R.string.exercise_stretch,
        R.string.exercise_mobility_move, R.string.exercise_balance_hold, R.string.exercise_pilates_move ->
            ExerciseInfo("mobility", "mat", R.string.tech_mobility)
        R.string.exercise_running_lap, R.string.exercise_walking_lap ->
            ExerciseInfo("cardio", "outdoor", R.string.tech_cardio)
        R.string.exercise_cycling_lap -> ExerciseInfo("cardio", "bike", R.string.tech_cardio)
        R.string.exercise_rowing_lap, R.string.exercise_elliptical_round ->
            ExerciseInfo("cardio", "machine", R.string.tech_cardio)
        R.string.exercise_freestyle, R.string.exercise_breaststroke,
        R.string.exercise_backstroke, R.string.exercise_swim_lap ->
            ExerciseInfo("cardio", "pool", R.string.tech_cardio)
        else -> ExerciseInfo(if (preset.mode == EntryMode.SCORE) "cardio" else "mobility",
            "bodyweight", R.string.tech_general)
    }

    val movements = listOf("squat", "lunge", "push", "pull", "core", "locomotion", "mobility", "isolation")

    fun movement(preset: ExercisePreset): String = when (preset.labelRes) {
        R.string.exercise_squat, R.string.exercise_leg_press -> "squat"
        R.string.exercise_lunge -> "lunge"
        R.string.exercise_push_up, R.string.exercise_bench_press, R.string.exercise_dumbbell_press,
        R.string.exercise_overhead_press -> "push"
        R.string.exercise_pull_up, R.string.exercise_dumbbell_row, R.string.exercise_cable_row,
        R.string.exercise_lat_pull -> "pull"
        R.string.exercise_plank, R.string.exercise_side_plank -> "core"
        R.string.exercise_biceps_curl, R.string.exercise_triceps_extension, R.string.exercise_calf_raise,
        R.string.exercise_leg_curl -> "isolation"
        else -> if (info(preset).muscle == "cardio") "locomotion" else "mobility"
    }

    fun movementLabel(key: String): Int = when (key) {
        "squat" -> R.string.movement_squat
        "lunge" -> R.string.movement_lunge
        "push" -> R.string.movement_push
        "pull" -> R.string.movement_pull
        "core" -> R.string.movement_core
        "locomotion" -> R.string.movement_locomotion
        "isolation" -> R.string.movement_isolation
        else -> R.string.movement_mobility
    }

    fun tag(key: String): Int = when (key) {
        "pullup" -> R.string.equipment_pullup
        "legs" -> R.string.muscle_legs
        "chest" -> R.string.muscle_chest
        "back" -> R.string.muscle_back
        "shoulders" -> R.string.muscle_shoulders
        "arms" -> R.string.muscle_arms
        "core" -> R.string.muscle_core
        "cardio" -> R.string.muscle_cardio
        "mobility" -> R.string.muscle_mobility
        "mat" -> R.string.equipment_mat
        "dumbbell" -> R.string.equipment_dumbbell
        "barbell" -> R.string.equipment_barbell
        "machine" -> R.string.equipment_machine
        "bands" -> R.string.equipment_bands
        "treadmill" -> R.string.equipment_treadmill
        "bike" -> R.string.equipment_bike
        "pool" -> R.string.equipment_pool
        "outdoor" -> R.string.equipment_outdoor
        else -> R.string.equipment_bodyweight
    }

    fun alternatives(target: ExercisePreset, available: Set<String>?): List<ExercisePreset> {
        val group = info(target).muscle
        return WorkoutCatalog.exercises.filter {
            it.labelRes != target.labelRes && info(it).muscle == group && movement(it) == movement(target) &&
                (available == null || info(it).equipment in available || info(it).equipment == "bodyweight")
        }.take(4)
    }
}
