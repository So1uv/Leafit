package com.example.fitnesstracker.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

object PickerDates {
    fun toPickerUtc(localTimestamp: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(localTimestamp).atZone(zone).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromPickerUtc(utcTimestamp: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(utcTimestamp).atZone(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
}
