package com.example.fitnesstracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class DateUtilsTest {
    private fun checkDay(date: String, hours: Long) {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"))
            val zone = ZoneId.of("Europe/Kyiv")
            val day = LocalDate.parse(date)
            val noon = day.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            assertEquals(day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), DateUtils.dayEnd(noon))
            assertEquals(hours * 3_600_000L, DateUtils.dayEnd(noon) - DateUtils.dayStart(noon))
        } finally {
            TimeZone.setDefault(previous)
        }
    }
    @Test fun springDayHas23Hours() = checkDay("2026-03-29", 23)
    @Test fun autumnDayHas25Hours() = checkDay("2026-10-25", 25)
    @Test fun regularDayHas24Hours() = checkDay("2026-09-07", 24)
}
