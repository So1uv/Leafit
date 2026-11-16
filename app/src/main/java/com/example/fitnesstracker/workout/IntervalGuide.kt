package com.example.fitnesstracker.workout

import org.json.JSONObject

data class IntervalGuide(val workSeconds: Int, val restSeconds: Int, val rounds: Int,
    val round: Int, val exerciseIndex: Int, val phase: String, val remaining: Int) {
    fun json(): JSONObject = JSONObject().apply {
        put("work", workSeconds)
        put("rest", restSeconds)
        put("rounds", rounds)
        put("round", round)
        put("index", exerciseIndex)
        put("phase", phase)
        put("remaining", remaining)
    }

    companion object {
        fun from(json: JSONObject): IntervalGuide {
            val work = json.optInt("work", 45).coerceIn(5, 3600)
            val rest = json.optInt("rest", 15).coerceIn(0, 1800)
            val rounds = json.optInt("rounds", 1).coerceIn(1, 20)
            val stage = json.optString("phase").takeIf { it == "work" || it == "rest" } ?: "done"
            val maximum = when (stage) { "work" -> work; "rest" -> rest; else -> 0 }
            return IntervalGuide(work, rest, rounds, json.optInt("round", 1).coerceIn(1, rounds),
                json.optInt("index").coerceIn(0, 99), stage, json.optInt("remaining").coerceIn(0, maximum))
        }
    }
}
