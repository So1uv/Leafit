package com.example.fitnesstracker.workout

import android.content.Context
import android.os.SystemClock
import com.example.fitnesstracker.R

data class SessionDisplay(
    val active: Boolean = false, val paused: Boolean = false, val token: String = "",
    val title: String = "", val type: String = "general", val elapsedBase: Long = 0,
    val frozenSeconds: Long = 0, val restDeadline: Long = 0, val frozenRest: Int = 0,
    val exerciseName: String = "", val timingSet: Boolean = false, val setBase: Long = 0,
    val frozenSetSeconds: Long = 0, val completedSets: Int = 0, val totalSets: Int = 0,
    val guideDeadline: Long = 0, val frozenGuide: Int = 0,
    val guidePhase: String = "", val guideName: String = ""
) {
    val guiding: Boolean get() = guidePhase == "work" || guidePhase == "rest"
    val resting: Boolean get() = restDeadline > 0 || frozenRest > 0
    val clockBase: Long get() = when {
        guiding -> guideDeadline
        resting -> restDeadline
        timingSet -> setBase
        else -> elapsedBase
    }
    fun seconds(): Long {
        if (!active) return 0
        if (paused) return when {
            guiding -> frozenGuide.toLong()
            resting -> frozenRest.toLong()
            timingSet -> frozenSetSeconds
            else -> frozenSeconds
        }.coerceAtLeast(0)
        val now = SystemClock.elapsedRealtime()
        return if (guiding || resting) ((clockBase - now + 999) / 1000).coerceAtLeast(0)
            else ((now - clockBase) / 1000).coerceAtLeast(0)
    }
    fun label(context: Context): String = context.getString(when {
        paused -> R.string.timer_paused
        guiding && guidePhase == "rest" -> R.string.interval_rest
        guiding -> R.string.interval_work
        resting -> R.string.timer_rest
        timingSet -> R.string.timer_set
        else -> R.string.timer_workout
    })
    fun name(context: Context): String = title.takeIf { it.isNotBlank() }
        ?: context.getString(WorkoutCatalog.forType(type).first().labelRes)
}
