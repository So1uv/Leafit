package com.example.fitnesstracker.workout

import com.example.fitnesstracker.utils.SleepTimeMath
import org.junit.Assert.*
import org.junit.Test

class WorkoutJournalTest {
    @Test fun sleepDurationCrossesMidnight() {
        assertEquals(465, SleepTimeMath.durationMinutes(23 * 60 + 30, 7 * 60 + 15))
        assertEquals(465, SleepTimeMath.durationMinutes(60, 8 * 60 + 45))
        assertEquals(465, SleepTimeMath.durationMinutes(0, 465))
    }
    @Test fun sameSleepTimesDoNotBecomeTwentyFourHours() {
        assertEquals(0, SleepTimeMath.durationMinutes(1320, 1320))
    }
    @Test fun plannedSetsDoNotCountAsCompletedWork() {
        val plan = WorkoutPlan(exercises = listOf(JournalExercise(name = "Row", sets = listOf(
            JournalSet(reps = 8, weightKg = 12f, completed = true),
            JournalSet(reps = 10, weightKg = 12f, completed = false)))))
        assertEquals(2, plan.setCount)
        assertEquals(1, plan.completedCount)
        assertEquals(8, plan.totalReps)
        assertEquals(96.0, plan.volumeKg, .001)
    }
    @Test fun zeroScoreIsAValidGameResultButEmptyRepsAreNot() {
        assertTrue(JournalSet(score = 0).hasResult(EntryMode.SCORE))
        assertFalse(JournalSet(reps = 0).hasResult(EntryMode.REPS))
        assertFalse(JournalSet().hasResult(EntryMode.TIME))
    }
    @Test fun newSessionKeepsPlanAndResetsResults() {
        val source = WorkoutPlan(title = "Plan", exercises = listOf(JournalExercise(name = "Move", restSeconds = 45,
            sets = listOf(JournalSet(reps = 5, completed = true)))))
        val next = source.fresh()
        assertEquals("Plan", next.title)
        assertEquals(45, next.exercises.single().restSeconds)
        assertEquals(5, next.exercises.single().sets.single().reps)
        assertEquals(0, next.completedCount)
        assertNotEquals(source.exercises.single().id, next.exercises.single().id)
        assertTrue(source.exercises.single().sets.single().completed)
    }
    @Test fun distanceCountsOnlyCompletedDistanceEntries() {
        val plan = WorkoutPlan(exercises = listOf(JournalExercise(name = "Lap", mode = EntryMode.DISTANCE, sets = listOf(
            JournalSet(distanceMeters = 200f, completed = true), JournalSet(distanceMeters = 150f, completed = false)))))
        assertEquals(200f, plan.recordedMeters, .001f)
    }
    @Test fun indoorVariantsNeverOfferGps() {
        assertTrue(WorkoutCatalog.variants.filter { it.outdoor }.all { it.type in setOf("run", "walk", "cycle") })
        assertFalse(WorkoutCatalog.variants.first { it.id == "treadmill" }.outdoor)
        assertFalse(WorkoutCatalog.variants.first { it.id == "stationary_bike" }.outdoor)
    }
}
