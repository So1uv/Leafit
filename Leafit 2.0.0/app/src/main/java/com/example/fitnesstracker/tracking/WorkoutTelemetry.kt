package com.example.fitnesstracker.tracking

import java.util.ArrayDeque
import java.util.Locale
import kotlin.math.*

/** Accepted route legs only; bounded storage and time-weighted smoothing. */
internal class SpeedWindow {
    private data class Leg(var meters: Double, var millis: Long)
    private val legs = ArrayDeque<Leg>()
    private var duration = 0L
    private var distance = 0.0
    private var segment: Int? = null
    val readyForExtremes: Boolean get() = duration >= 8_000 && distance >= 5.0

    fun clear() { legs.clear(); duration = 0L; distance = 0.0; segment = null }

    fun add(meters: Float, millis: Long, segmentId: Int): Float? {
        if (!meters.isFinite() || meters < 0 || millis !in 1L..20_000L) { clear(); return null }
        if (segment != segmentId) clear()
        segment = segmentId
        legs.addLast(Leg(meters.toDouble(), millis))
        distance += meters
        duration += millis
        while (duration > 10_000L) {
            val first = legs.first
            val excess = duration - 10_000L
            if (first.millis <= excess) {
                duration -= first.millis; distance -= first.meters; legs.removeFirst()
            } else {
                val removed = first.meters * excess / first.millis
                first.millis -= excess; first.meters -= removed
                duration -= excess; distance -= removed
            }
        }
        return if (duration > 0) (distance.coerceAtLeast(0.0) * 1000 / duration).toFloat() else null
    }
}

internal data class PaceRange(val fastest: Float? = null, val slowest: Float? = null) {
    fun add(speedMps: Float?, reliable: Boolean): PaceRange {
        if (!reliable || speedMps == null || !speedMps.isFinite() || speedMps < .5f) return this
        val secondsPerKm = 1000f / speedMps
        return PaceRange(fastest?.let { minOf(it, secondsPerKm) } ?: secondsPerKm,
            slowest?.let { maxOf(it, secondsPerKm) } ?: secondsPerKm)
    }
}

internal fun averagePaceSeconds(elapsedMs: Long, distanceMeters: Float): Double? =
    if (elapsedMs > 0 && distanceMeters.isFinite() && distanceMeters >= 30f) elapsedMs.toDouble() / distanceMeters else null

internal fun averageSpeedKmh(elapsedMs: Long, distanceMeters: Float): Double? =
    if (elapsedMs > 0 && distanceMeters.isFinite() && distanceMeters >= 30f) distanceMeters.toDouble() * 3600 / elapsedMs else null

internal fun formatPace(secondsPerKm: Double?, locale: Locale): String {
    if (secondsPerKm == null || !secondsPerKm.isFinite() || secondsPerKm <= 0 || secondsPerKm > 86_400) return "—"
    val seconds = secondsPerKm.roundToLong()
    return String.format(locale, "%d:%02d", seconds / 60, seconds % 60)
}

internal fun maximumRouteSpeed(type: String): Float = when (type) { "walk" -> 5f; "cycle" -> 25f; else -> 12f }

/** Compatibility for routes saved before pace statistics existed. Called off the UI thread. */
internal fun historicalPaceRange(points: List<TrackPoint>, type: String): PaceRange {
    val window = SpeedWindow()
    var range = PaceRange()
    for (i in 1 until points.size) {
        val previous = points[i - 1]
        val next = points[i]
        val millis = if (previous.monotonicMs != null && next.monotonicMs != null)
            next.monotonicMs - previous.monotonicMs else next.time - previous.time
        if (previous.segment != next.segment || millis !in 1L..20_000L ||
            previous.accuracy !in 0f..35f || next.accuracy !in 0f..35f) { window.clear(); continue }
        val lat1 = Math.toRadians(previous.lat)
        val lat2 = Math.toRadians(next.lat)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(next.lon - previous.lon)
        val haversine = (sin(deltaLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2)).coerceIn(0.0, 1.0)
        val meters = (6_371_000 * 2 * atan2(sqrt(haversine), sqrt(1 - haversine))).toFloat()
        if (meters * 1000 / millis > maximumRouteSpeed(type)) { window.clear(); continue }
        val speed = window.add(meters, millis, next.segment)
        range = range.add(speed, window.readyForExtremes)
    }
    return range
}
