package com.example.fitnesstracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StepAccumulatorTest {
    @Test fun firstReadingEstablishesBaseline() {
        assertEquals(0, StepAccumulator.update(StepSnapshot("", 0, -1, 1), "2026-09-07", 4500, 1).steps)
    }
    @Test fun repeatedReadingDoesNotDoubleCount() {
        val state = StepSnapshot("2026-09-07", 120, 5000, 1)
        assertEquals(120, StepAccumulator.update(state, state.day, 5000, 1).steps)
        assertEquals(127, StepAccumulator.update(state, state.day, 5007, 1).steps)
    }
    @Test fun processRestartContinuesSavedTotal() {
        val saved = StepSnapshot("2026-09-07", 3500, 9000, 2)
        assertEquals(3620, StepAccumulator.update(saved, saved.day, 9120, 2).steps)
    }
    @Test fun rebootPreservesTodaysSteps() {
        val saved = StepSnapshot("2026-09-07", 3500, 9000, 2)
        assertEquals(3525, StepAccumulator.update(saved, saved.day, 25, 3).steps)
    }
    @Test fun rebootDetectedEvenIfNewTotalExceedsOldTotal() {
        val saved = StepSnapshot("2026-09-07", 10, 10, 2)
        assertEquals(50, StepAccumulator.update(saved, saved.day, 40, 3).steps)
    }
    @Test fun sensorResetPreservesTodaysSteps() {
        val saved = StepSnapshot("2026-09-07", 3500, 9000, 2)
        assertEquals(3505, StepAccumulator.update(saved, saved.day, 5, 2).steps)
    }
    @Test fun midnightDoesNotAttributeAnUnknownOvernightGapToToday() {
        val saved = StepSnapshot("2026-09-07", 3500, 9000, 2)
        val next = StepAccumulator.update(saved, "2026-09-08", 9500, 2)
        assertEquals(0, next.steps)
        assertEquals(8, StepAccumulator.update(next, next.day, 9508, 2).steps)
    }
    @Test fun longAbsenceDoesNotImportPreviousDays() {
        val saved = StepSnapshot("2026-09-01", 100, 1000, 1)
        assertEquals(0, StepAccumulator.update(saved, "2026-09-07", 30000, 1).steps)
    }
}
