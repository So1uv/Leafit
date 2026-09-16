package com.example.fitnesstracker.utils

enum class SleepStage(val level: Int) {
    AWAKE(3),
    REM(2),
    LIGHT(1),
    DEEP(0)
}

data class HypnoSegment(
    val stage: SleepStage,
    val startMin: Int,
    val endMin: Int
)

data class HypnogramResult(
    val segments: List<HypnoSegment>,
    val cycleCount: Int,

    val wakeCyclePosition: Float,

    val wakeCycleIndex: Int,
    val wakeStage: SleepStage
)

object SleepCycleModel {
    const val TIME_TO_FALL_ASLEEP = 15
    const val CYCLE_MINUTES = 90

    fun build(totalMinutes: Int): HypnogramResult {
        val segs = mutableListOf<HypnoSegment>()
        var t = 0

        val asleepAt = TIME_TO_FALL_ASLEEP.coerceAtMost(totalMinutes)
        if (asleepAt > 0) {
            segs.add(HypnoSegment(SleepStage.AWAKE, 0, asleepAt))
            t = asleepAt
        }

        val sleepMinutes = (totalMinutes - asleepAt).coerceAtLeast(0)
        val fullCycles = sleepMinutes / CYCLE_MINUTES
        val remainder = sleepMinutes % CYCLE_MINUTES
        val cycleCount = fullCycles + if (remainder > 0) 1 else 0

        fun addStage(stage: SleepStage, dur: Int) {
            if (dur <= 0) return
            segs.add(HypnoSegment(stage, t, t + dur))
            t += dur
        }

        for (c in 0 until cycleCount) {
            val isLast = c == cycleCount - 1
            val cycleLen = if (isLast && remainder > 0) remainder else CYCLE_MINUTES
            val prog = if (cycleCount > 1) c.toFloat() / (cycleCount - 1) else 0f

            // Deep fraction: ~0.55 early → ~0.05 late. REM: ~0.05 early → ~0.35 late.
            val deepFrac = (0.55f - 0.50f * prog).coerceIn(0.05f, 0.55f)
            val remFrac = (0.05f + 0.30f * prog).coerceIn(0.05f, 0.40f)
            val lightFrac = (1f - deepFrac - remFrac).coerceAtLeast(0.15f)

            val lightLen = (cycleLen * lightFrac).toInt()
            val deepLen = (cycleLen * deepFrac).toInt()
            val remLen = (cycleLen * remFrac).toInt()
            val firstLight = lightLen / 2
            val secondLight = lightLen - firstLight

            addStage(SleepStage.LIGHT, firstLight)
            addStage(SleepStage.DEEP, deepLen)
            addStage(SleepStage.LIGHT, secondLight)
            addStage(SleepStage.REM, remLen)
            // pad any rounding gap with light sleep so cycles align
            val consumed = firstLight + deepLen + secondLight + remLen
            if (consumed < cycleLen) addStage(SleepStage.LIGHT, cycleLen - consumed)
        }

        // Trim/extend last segment so timeline ends exactly at totalMinutes
        if (segs.isNotEmpty()) {
            val last = segs.last()
            if (last.endMin != totalMinutes) {
                segs[segs.size - 1] = last.copy(endMin = totalMinutes)
            }
        }

        val wakeStage = segs.lastOrNull()?.stage ?: SleepStage.AWAKE
        val sleepElapsed = (totalMinutes - asleepAt).coerceAtLeast(0)
        val posInCycle = if (CYCLE_MINUTES > 0) (sleepElapsed % CYCLE_MINUTES).toFloat() / CYCLE_MINUTES else 0f
        val wakeCycleIndex = (sleepElapsed / CYCLE_MINUTES) + 1

        return HypnogramResult(
            segments = segs,
            cycleCount = cycleCount,
            wakeCyclePosition = posInCycle,
            wakeCycleIndex = wakeCycleIndex.coerceAtLeast(1),
            wakeStage = wakeStage
        )
    }

    fun suggestBetterWake(totalMinutes: Int): Int {
        val asleep = TIME_TO_FALL_ASLEEP
        val sleepMin = totalMinutes - asleep
        val nearestCycleEnd = Math.round(sleepMin.toFloat() / CYCLE_MINUTES) * CYCLE_MINUTES
        return asleep + nearestCycleEnd.coerceAtLeast(CYCLE_MINUTES)
    }
}
