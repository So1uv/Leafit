package com.example.fitnesstracker.utils

object SleepTimeMath {
    fun normalize(minutes: Int): Int = Math.floorMod(minutes, 1440)
    fun durationMinutes(bed: Int, wake: Int): Int = normalize(normalize(wake) - normalize(bed))
}
