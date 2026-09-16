package com.example.fitnesstracker.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class PickerDatesTest {
    @Test fun pickerRepresentsTheSameCalendarDayInBothHemispheresAndAcrossDst() {
        val zones = listOf("UTC", "Europe/Kyiv", "America/Los_Angeles", "Pacific/Auckland", "Pacific/Honolulu", "Pacific/Kiritimati")
        val dates = listOf("2026-03-08", "2026-03-29", "2026-09-08", "2026-10-25", "2026-11-01", "2028-02-29")
        for (name in zones) for (text in dates) {
            val zone = ZoneId.of(name)
            val day = LocalDate.parse(text)
            val localStart = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val picker = PickerDates.toPickerUtc(localStart, zone)
            assertEquals("picker day for $name / $text", day,
                Instant.ofEpochMilli(picker).atZone(ZoneOffset.UTC).toLocalDate())
            assertEquals("round trip for $name / $text", localStart, PickerDates.fromPickerUtc(picker, zone))
        }
    }

    @Test fun selectingUtcDateReturnsLocalMidnightRatherThanThePreviousEvening() {
        val zone = ZoneId.of("America/Los_Angeles")
        val picked = Instant.parse("2026-09-08T00:00:00Z").toEpochMilli()
        assertEquals(Instant.parse("2026-09-08T07:00:00Z").toEpochMilli(), PickerDates.fromPickerUtc(picked, zone))
    }
}
