package com.example.fitnesstracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val CYCLE_MINUTES = 90
const val TIME_TO_FALL_ASLEEP = 15

enum class SleepStage(val level: Int) {
    AWAKE(0), REM(1), LIGHT(2), DEEP(3)
}

data class HypnoSegment(val startMin: Float, val endMin: Float, val stage: SleepStage)

fun buildHypnogram(totalMinutes: Int): List<HypnoSegment> {
    val segments = mutableListOf<HypnoSegment>()
    if (totalMinutes <= 0) return segments
    if (totalMinutes <= TIME_TO_FALL_ASLEEP) return listOf(HypnoSegment(0f, totalMinutes.toFloat(), SleepStage.AWAKE))

    segments.add(HypnoSegment(0f, TIME_TO_FALL_ASLEEP.toFloat(), SleepStage.AWAKE))

    var t = TIME_TO_FALL_ASLEEP.toFloat()
    val end = totalMinutes.toFloat()
    var cycleIndex = 0

    while (t < end) {
        val cycleLen = minOf(CYCLE_MINUTES.toFloat(), end - t)
        if (cycleLen < 8f) {
            segments.add(HypnoSegment(t, end, SleepStage.LIGHT))
            break
        }

        val deepFrac = (0.55f - cycleIndex * 0.14f).coerceIn(0.0f, 0.6f)
        val remFrac = (0.08f + cycleIndex * 0.10f).coerceIn(0.05f, 0.35f)
        val onsetLight = 0.12f
        val lightFrac = (1f - deepFrac - remFrac - onsetLight).coerceAtLeast(0.1f)

        var c = t
        val onsetLen = cycleLen * onsetLight
        segments.add(HypnoSegment(c, c + onsetLen, SleepStage.LIGHT)); c += onsetLen
        if (deepFrac > 0.01f) {
            val deepLen = cycleLen * deepFrac
            segments.add(HypnoSegment(c, c + deepLen, SleepStage.DEEP)); c += deepLen
        }
        val lightLen = cycleLen * lightFrac
        segments.add(HypnoSegment(c, c + lightLen, SleepStage.LIGHT)); c += lightLen
        val remLen = (t + cycleLen) - c
        if (remLen > 1f) segments.add(HypnoSegment(c, t + cycleLen, SleepStage.REM))

        t += cycleLen
        cycleIndex++
    }
    return segments
}

fun cycleCount(totalMinutes: Int): Int =
    ((totalMinutes - TIME_TO_FALL_ASLEEP).coerceAtLeast(0) / CYCLE_MINUTES)

@Composable
fun SleepHypnogram(durationMinutes: Int, modifier: Modifier = Modifier, startMinutes: Int? = null) {
    val locale = appLocale()
    val duration = durationMinutes.coerceIn(0, 1440)
    fun time(minutes: Int): String {
        val value = if (startMinutes == null) minutes else Math.floorMod(startMinutes + minutes, 1440)
        return String.format(locale, "%d:%02d", value / 60, value % 60)
    }
    SleepStageChart(duration, time(0), time(duration), modifier)
}
