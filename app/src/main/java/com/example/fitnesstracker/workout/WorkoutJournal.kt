package com.example.fitnesstracker.workout

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

fun journalId(): String = UUID.randomUUID().toString()

enum class EntryMode { REPS, TIME, DISTANCE, SCORE }

data class JournalSet(
    val id: String = journalId(), val reps: Int? = null, val weightKg: Float? = null,
    val seconds: Int? = null, val distanceMeters: Float? = null, val score: Int? = null,
    val completed: Boolean = false
) {
    fun hasResult(mode: EntryMode): Boolean = when (mode) {
        EntryMode.REPS -> (reps ?: 0) > 0
        EntryMode.TIME -> (seconds ?: 0) > 0
        EntryMode.DISTANCE -> (distanceMeters ?: 0f) > 0f
        EntryMode.SCORE -> score != null && score >= 0
    }
}

data class JournalExercise(
    val id: String = journalId(), val name: String, val mode: EntryMode = EntryMode.REPS,
    val restSeconds: Int = 60, val note: String = "", val sets: List<JournalSet> = listOf(JournalSet())
)

data class WorkoutPlan(
    val type: String = "general", val variantId: String = "free", val title: String = "",
    val restSeconds: Int = 60, val exercises: List<JournalExercise> = emptyList()
) {
    val setCount: Int get() = exercises.sumOf { it.sets.size }
    val completedCount: Int get() = exercises.sumOf { e -> e.sets.count { it.completed && it.hasResult(e.mode) } }
    val totalReps: Int get() = exercises.filter { it.mode == EntryMode.REPS }
        .sumOf { e -> e.sets.filter { it.completed }.sumOf { it.reps ?: 0 } }
    val volumeKg: Double get() = exercises.filter { it.mode == EntryMode.REPS }
        .sumOf { e -> e.sets.filter { it.completed }.sumOf { (it.reps ?: 0) * (it.weightKg ?: 0f).toDouble() } }
    val recordedMeters: Float get() = exercises.filter { it.mode == EntryMode.DISTANCE }
        .sumOf { e -> e.sets.filter { it.completed }.sumOf { (it.distanceMeters ?: 0f).toDouble() } }.toFloat()
    fun fresh(): WorkoutPlan = copy(exercises = exercises.map { e ->
        e.copy(id = journalId(), sets = e.sets.map { it.copy(id = journalId(), completed = false) })
    })
}

data class WorkoutTemplate(val id: String = journalId(), val name: String, val plan: WorkoutPlan)

object WorkoutJournal {
    const val KIND = "leaf_workout_v2"
    fun encode(plan: WorkoutPlan): String = JSONObject().put("kind", KIND).put("version", 2).put("plan", toJson(plan)).toString()
    fun decode(raw: String): WorkoutPlan? = runCatching {
        val json = JSONObject(raw)
        if (json.optString("kind") != KIND) null else fromJson(json.getJSONObject("plan"))
    }.getOrNull()

    fun toJson(plan: WorkoutPlan): JSONObject = JSONObject().put("type", plan.type).put("variant", plan.variantId)
        .put("title", plan.title).put("rest", plan.restSeconds).put("exercises", JSONArray().apply {
            plan.exercises.forEach { e -> put(JSONObject().put("id", e.id).put("name", e.name).put("mode", e.mode.name)
                .put("rest", e.restSeconds).put("note", e.note).put("sets", JSONArray().apply {
                    e.sets.forEach { s -> put(JSONObject().put("id", s.id).put("reps", s.reps).put("kg", s.weightKg)
                        .put("seconds", s.seconds).put("meters", s.distanceMeters).put("score", s.score).put("done", s.completed)) }
                })) }
        })

    fun fromJson(json: JSONObject): WorkoutPlan {
        fun JSONObject.number(key: String): Float? = if (isNull(key) || !has(key)) null
            else optDouble(key, Double.NaN).toFloat().takeIf { it.isFinite() && it >= 0f && it <= 1_000_000f }
        fun JSONObject.integer(key: String): Int? = number(key)?.toInt()
        val array = json.optJSONArray("exercises") ?: JSONArray()
        val exercises = (0 until array.length().coerceAtMost(100)).mapNotNull { i ->
            val e = array.optJSONObject(i) ?: return@mapNotNull null
            val mode = runCatching { EntryMode.valueOf(e.optString("mode")) }.getOrDefault(EntryMode.REPS)
            val rows = e.optJSONArray("sets") ?: JSONArray()
            JournalExercise(id = e.optString("id").ifBlank { journalId() }, name = e.optString("name").take(120), mode = mode,
                restSeconds = e.optInt("rest", 60).coerceIn(0, 3600), note = e.optString("note").take(2000),
                sets = (0 until rows.length().coerceAtMost(100)).mapNotNull row@ { j ->
                    val s = rows.optJSONObject(j) ?: return@row null
                    JournalSet(id = s.optString("id").ifBlank { journalId() }, reps = s.integer("reps")?.coerceAtMost(10000), weightKg = s.number("kg"),
                        seconds = s.integer("seconds")?.coerceAtMost(86400), distanceMeters = s.number("meters"), score = s.integer("score"),
                        completed = s.optBoolean("done"))
                }.distinctBy { it.id })
        }.distinctBy { it.id }
        val type = json.optString("type", "general")
        return WorkoutPlan(type = type, variantId = json.optString("variant", WorkoutCatalog.forType(type).first().id),
            title = json.optString("title").take(120), restSeconds = json.optInt("rest", 60).coerceIn(0, 3600), exercises = exercises)
    }
}
