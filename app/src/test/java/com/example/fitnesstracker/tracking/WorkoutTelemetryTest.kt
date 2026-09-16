package com.example.fitnesstracker.tracking

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class WorkoutTelemetryTest {
    @Test fun oneKilometreInFiveMinutesHasConsistentUnits() {
        assertEquals(300.0, averagePaceSeconds(300_000, 1000f)!!, .001)
        assertEquals(12.0, averageSpeedKmh(300_000, 1000f)!!, .001)
        assertEquals("5:00", formatPace(300.0, Locale.US))
    }

    @Test fun noReliableDistanceDoesNotProduceInfinityOrAnInventedPace() {
        assertNull(averagePaceSeconds(0, 100f))
        assertNull(averagePaceSeconds(10_000, 0f))
        assertNull(averagePaceSeconds(10_000, 29f))
        assertNull(averagePaceSeconds(10_000, Float.NaN))
        assertEquals("—", formatPace(Double.POSITIVE_INFINITY, Locale.US))
    }

    @Test fun paceRoundingCarriesIntoTheNextMinute() {
        assertEquals("5:00", formatPace(299.8, Locale.US))
    }

    @Test fun paceRangeUsesFasterAsLessTimePerKilometre() {
        val range = PaceRange().add(2f, true).add(4f, true).add(0f, true).add(.1f, true)
        assertEquals(250f, range.fastest!!, .001f)
        assertEquals(500f, range.slowest!!, .001f)
    }

    @Test fun shortFixesCannotSetTheExtremes() {
        val window = SpeedWindow()
        val speed = window.add(6f, 3000, 0)
        assertFalse(window.readyForExtremes)
        assertNull(PaceRange().add(speed, window.readyForExtremes).fastest)
    }

    @Test fun speedIsWeightedByTimeInsteadOfAveragingSamples() {
        val window = SpeedWindow()
        window.add(10f, 2000, 0)
        val speed = window.add(8f, 8000, 0)
        assertEquals(1.8f, speed!!, .001f)
        assertTrue(window.readyForExtremes)
    }

    @Test fun aPauseOrNewSegmentStartsAFreshWindow() {
        val window = SpeedWindow()
        window.add(40f, 10_000, 0)
        assertTrue(window.readyForExtremes)
        assertEquals(1f, window.add(2f, 2000, 1)!!, .001f)
        assertFalse(window.readyForExtremes)
        window.clear()
        assertNull(window.add(20f, 30_000, 1))
        assertFalse(window.readyForExtremes)
    }

    @Test fun oldRouteExtremesDoNotJoinPointsAcrossPauses() {
        val points = listOf(
            TrackPoint(50.0, 30.0, 0, 5f, 0),
            TrackPoint(50.1, 30.1, 10_000, 5f, 1))
        assertEquals(PaceRange(), historicalPaceRange(points, "run"))
    }

    @Test fun monotonicTimestampsIgnoreWallClockCorrections() {
        val points = (0..10).map { i ->
            TrackPoint(50.0 + i * .00002, 30.0, 100_000L - i * 1000L, 5f, 0, i * 1000L)
        }
        val range = historicalPaceRange(points, "walk")
        assertNotNull(range.fastest)
        assertTrue(range.fastest!! in 440f..460f)
        assertTrue(range.slowest!! in 440f..460f)
    }
}
