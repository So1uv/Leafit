package com.example.fitnesstracker.tracking

import android.content.Context
import android.util.AtomicFile
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal enum class LivePhase { IDLE, ACTIVE, PAUSED, SAVING }
internal enum class GpsStatus { WAITING, GOOD, WEAK, DISABLED, PERMISSION, RECOVERED, STORAGE, SAVE_ERROR }
internal data class TrackPoint(val lat: Double, val lon: Double, val time: Long, val accuracy: Float, val segment: Int,
    val monotonicMs: Long? = null) {
    val position: LatLng get() = LatLng(lat, lon)
}
internal data class LiveSession(
    val token: String = "", val type: String = "run", val variantId: String = "", val title: String = "", val phase: LivePhase = LivePhase.IDLE,
    val startedAt: Long = 0, val elapsedMs: Long = 0, val distanceMeters: Float = 0f,
    val points: List<TrackPoint> = emptyList(), val segment: Int = 0,
    val status: GpsStatus = GpsStatus.WAITING, val accuracy: Float? = null,
    val currentSpeedMps: Float? = null, val fastestPaceSeconds: Float? = null,
    val slowestPaceSeconds: Float? = null
) {
    val active get() = phase != LivePhase.IDLE
}

internal object LiveWorkoutStore {
    private val mutable = MutableStateFlow(LiveSession())
    val state: StateFlow<LiveSession> = mutable.asStateFlow()
    private val mutableRoute = MutableStateFlow<List<LatLng>>(emptyList())
    val route: StateFlow<List<LatLng>> = mutableRoute.asStateFlow()
    private val mutex = Mutex()
    private var loaded = false
    private fun file(context: Context) = AtomicFile(File(context.noBackupFilesDir, "live_workout_draft.json"))

    fun publish(session: LiveSession) {
        val changed = mutable.value.points !== session.points
        mutable.value = session
        if (changed) mutableRoute.value = session.points.map { it.position }
    }

    suspend fun hydrate(context: Context) = mutex.withLock {
        if (!loaded) {
            val draft = withContext(Dispatchers.IO) {
                runCatching { decode(JSONObject(file(context).openRead().bufferedReader().use { it.readText() })) }.getOrNull()
            }
            if (draft != null && draft.active) publish(draft.copy(phase = LivePhase.PAUSED,
                segment = draft.segment + 1, status = GpsStatus.RECOVERED, accuracy = null, currentSpeedMps = null))
            loaded = true
        }
    }

    suspend fun persist(context: Context, session: LiveSession) = withContext(Dispatchers.IO) {
        val target = file(context)
        if (!session.active) { target.delete(); return@withContext }
        val stream = target.startWrite()
        try { stream.write(encode(session).toString().toByteArray(Charsets.UTF_8)); target.finishWrite(stream) }
        catch (e: Exception) { target.failWrite(stream); throw e }
    }

    fun encode(s: LiveSession): JSONObject = JSONObject().apply {
        put("token", s.token); put("type", s.type); put("variant", s.variantId); put("title", s.title); put("phase", s.phase.name)
        put("startedAt", s.startedAt); put("elapsedMs", s.elapsedMs); put("distanceMeters", s.distanceMeters)
        put("segment", s.segment)
        put("telemetryVersion", 1)
        s.fastestPaceSeconds?.takeIf { it.isFinite() && it > 0 }?.let { put("fastestPaceSeconds", it) }
        s.slowestPaceSeconds?.takeIf { it.isFinite() && it > 0 }?.let { put("slowestPaceSeconds", it) }
        put("points", JSONArray().apply { s.points.forEach { p ->
            put(JSONArray().put(p.lat).put(p.lon).put(p.time).put(p.accuracy).put(p.segment).put(p.monotonicMs ?: JSONObject.NULL))
        } })
    }
    fun decode(o: JSONObject): LiveSession {
        val array = o.optJSONArray("points") ?: JSONArray()
        val points = List(array.length()) { i ->
            val p = array.getJSONArray(i)
            TrackPoint(p.getDouble(0), p.getDouble(1), p.getLong(2), p.getDouble(3).toFloat(), p.getInt(4),
                if (p.length() > 5 && !p.isNull(5)) p.getLong(5) else null)
        }.filter { it.lat.isFinite() && it.lon.isFinite() && it.lat in -90.0..90.0 && it.lon in -180.0..180.0 }
        fun pace(key: String) = o.optDouble(key, Double.NaN).toFloat().takeIf { it.isFinite() && it > 0 }
        val type = o.optString("type", "run")
        val range = if (o.optInt("telemetryVersion", 0) >= 1)
            PaceRange(pace("fastestPaceSeconds"), pace("slowestPaceSeconds")) else historicalPaceRange(points, type)
        return LiveSession(token = o.getString("token"), type = type, variantId = o.optString("variant"), title = o.optString("title"),
            phase = LivePhase.valueOf(o.optString("phase", "PAUSED")), startedAt = o.getLong("startedAt"),
            elapsedMs = o.optLong("elapsedMs").coerceAtLeast(0),
            distanceMeters = o.optDouble("distanceMeters", 0.0).toFloat().takeIf { it.isFinite() && it >= 0 } ?: 0f,
            segment = o.optInt("segment"), points = points,
            fastestPaceSeconds = range.fastest, slowestPaceSeconds = range.slowest)
    }
    fun fromMetrics(metrics: String): LiveSession? = runCatching {
        decode(JSONObject(metrics).getJSONObject("leaf_route_v1"))
    }.getOrNull()
}
