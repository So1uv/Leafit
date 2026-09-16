package com.example.fitnesstracker.utils

internal data class StepSnapshot(
    val day: String,
    val steps: Int,
    val total: Int,
    val boot: Int
)

internal object StepAccumulator {
    fun update(previous: StepSnapshot, day: String, total: Int, boot: Int): StepSnapshot {
        val sameDay = previous.day == day
        val rebooted = previous.boot >= 0 && boot >= 0 && previous.boot != boot
        val delta = when {
            !sameDay || previous.total < 0 -> 0
            rebooted || total < previous.total -> total
            else -> total - previous.total
        }
        return StepSnapshot(day, ((if (sameDay) previous.steps else 0).toLong() + delta)
            .coerceIn(0, Int.MAX_VALUE.toLong()).toInt(), total, boot)
    }
}
