package com.example.fitnesstracker.tracking

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.utils.LanguagePreferences
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject
import java.util.UUID

internal object LiveWorkoutController {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun precise(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun outdoor(type: String) =
        type in setOf("run", "walk", "cycle")

    fun command(
        context: Context,
        action: String,
        type: String = "run",
        note: String = "",
        variantId: String = "",
        sessionTitle: String = ""
    ): Boolean {
        val app = context.applicationContext

        if (!LiveWorkoutService.alive && action in setOf("save", "discard")) {
            scope.launch {
                LiveWorkoutStore.hydrate(app)

                if (action == "save") {
                    saveLiveSession(app, note)
                } else {
                    clearLiveSession(app)
                }
            }

            return true
        }

        if (!LiveWorkoutService.alive && !precise(app)) {
            LiveWorkoutStore.publish(
                LiveWorkoutStore.state.value.copy(
                    status = GpsStatus.PERMISSION
                )
            )
            return false
        }

        return runCatching {
            val intent =
                Intent(app, LiveWorkoutService::class.java)
                    .setAction(action)
                    .putExtra("type", type)
                    .putExtra("note", note)
                    .putExtra("variant", variantId)
                    .putExtra("title", sessionTitle.take(120))

            if (LiveWorkoutService.alive) {
                app.startService(intent)
            } else {
                ContextCompat.startForegroundService(app, intent)
            }

            if (
                context is android.app.Activity &&
                action in setOf("start", "resume", "pause")
            ) {
                com.example.fitnesstracker.utils.Haptics.play(
                    app,
                    if (action == "pause") {
                        com.example.fitnesstracker.utils.HapticTexture.PAUSE
                    } else {
                        com.example.fitnesstracker.utils.HapticTexture.START
                    }
                )
            }

            true
        }.getOrElse {
            LiveWorkoutStore.publish(
                LiveWorkoutStore.state.value.copy(
                    status = GpsStatus.PERMISSION
                )
            )
            false
        }
    }
}

internal suspend fun saveLiveSession(
    context: Context,
    note: String
): Boolean {
    val snapshot = LiveWorkoutStore.state.value

    if (!snapshot.active || snapshot.phase == LivePhase.SAVING) {
        return false
    }

    LiveWorkoutStore.publish(
        snapshot.copy(
            phase = LivePhase.SAVING
        )
    )

    return try {
        val payload = withContext(Dispatchers.Default) {
            JSONObject()
                .put(
                    "leaf_route_v1",
                    LiveWorkoutStore.encode(snapshot)
                )
                .toString()
        }

        AppDatabase
            .getInstance(context)
            .workoutDao()
            .insertLiveOnce(
                Workout(
                    startTime = snapshot.startedAt,
                    endTime = System.currentTimeMillis(),
                    durationSeconds = snapshot.elapsedMs / 1000,
                    distanceMeters = snapshot.distanceMeters,
                    note = note,
                    type = snapshot.type,
                    metrics = payload
                ),
                snapshot.token
            )

        LiveWorkoutStore.persist(
            context,
            LiveSession()
        )

        LiveWorkoutStore.publish(
            LiveSession()
        )

        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        LiveWorkoutStore.publish(
            snapshot.copy(
                phase = LivePhase.PAUSED,
                status = GpsStatus.SAVE_ERROR
            )
        )

        false
    }
}

internal suspend fun clearLiveSession(
    context: Context
): Boolean = try {
    LiveWorkoutStore.persist(
        context,
        LiveSession()
    )

    LiveWorkoutStore.publish(
        LiveSession()
    )

    true
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    LiveWorkoutStore.publish(
        LiveWorkoutStore.state.value.copy(
            status = GpsStatus.STORAGE
        )
    )

    false
}

class LiveWorkoutService : Service() {

    private data class Event(
        val action: String,
        val type: String = "run",
        val note: String = "",
        val location: Location? = null,
        val variantId: String = "",
        val sessionTitle: String = ""
    )

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val events =
        Channel<Event>(Channel.UNLIMITED)

    private lateinit var fused: FusedLocationProviderClient

    // FIX: NotificationManager is now a class field.
    private lateinit var notificationManager: NotificationManager

    private var lastTick = 0L
    private var lastPersist = 0L
    private var lastFixElapsed = 0L
    private var lastMotionElapsed = 0L

    private val speedWindow = SpeedWindow()

    private var lastAccepted: Location? = null
    private var updatesRequested = false
    private var finishing = false

    private val callback =
        object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {
                result.locations.forEach {
                    events.trySend(
                        Event(
                            "point",
                            location = it
                        )
                    )
                }
            }

            override fun onLocationAvailability(
                result: LocationAvailability
            ) {
                if (!result.isLocationAvailable) {
                    events.trySend(
                        Event("weak")
                    )
                }
            }
        }

    override fun onCreate() {
        super.onCreate()

        fused =
            LocationServices
                .getFusedLocationProviderClient(this)

        // FIX: assign the class field instead of creating local "manager".
        notificationManager =
            getSystemService(NotificationManager::class.java)

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL,
                text(R.string.live_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
        )

        try {
            if (!LiveWorkoutController.precise(this)) {
                throw SecurityException()
            }

            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(
                    NOTIFICATION,
                    notification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(
                    NOTIFICATION,
                    notification()
                )
            }
        } catch (_: Exception) {
            LiveWorkoutStore.publish(
                LiveWorkoutStore.state.value.copy(
                    status = GpsStatus.PERMISSION
                )
            )

            stopSelf()
            return
        }

        alive = true

        scope.launch {
            LiveWorkoutStore.hydrate(
                applicationContext
            )

            for (event in events) {
                if (finishing) {
                    break
                }

                tick()

                when (event.action) {

                    "start" -> {
                        if (
                            !LiveWorkoutStore.state.value.active &&
                            LiveWorkoutController.outdoor(event.type)
                        ) {
                            LiveWorkoutStore.publish(
                                LiveSession(
                                    token = UUID.randomUUID().toString(),
                                    type = event.type,
                                    variantId = event.variantId,
                                    title = event.sessionTitle,
                                    phase = LivePhase.ACTIVE,
                                    startedAt = System.currentTimeMillis()
                                )
                            )

                            lastTick =
                                SystemClock.elapsedRealtime()

                            requestLocations()
                            checkpoint()
                        }
                    }

                    "resume" -> {
                        if (
                            LiveWorkoutStore.state.value.phase ==
                            LivePhase.PAUSED
                        ) {
                            val s =
                                LiveWorkoutStore.state.value

                            if (
                                LiveWorkoutController.precise(
                                    this@LiveWorkoutService
                                )
                            ) {
                                LiveWorkoutStore.publish(
                                    s.copy(
                                        phase = LivePhase.ACTIVE,
                                        segment = s.segment + 1,
                                        status = GpsStatus.WAITING,
                                        accuracy = null,
                                        currentSpeedMps = null
                                    )
                                )

                                lastAccepted = null
                                speedWindow.clear()

                                lastMotionElapsed = 0L
                                lastFixElapsed = 0L

                                lastTick =
                                    SystemClock.elapsedRealtime()

                                requestLocations()
                                checkpoint()
                            } else {
                                LiveWorkoutStore.publish(
                                    s.copy(
                                        status = GpsStatus.PERMISSION
                                    )
                                )
                            }
                        }
                    }

                    "pause" -> {
                        pause()
                        checkpoint()
                    }

                    "save" -> {
                        pause()
                        checkpoint()

                        if (
                            saveLiveSession(
                                applicationContext,
                                event.note
                            )
                        ) {
                            finishService()
                        }
                    }

                    "discard" -> {
                        pause()

                        if (
                            clearLiveSession(
                                applicationContext
                            )
                        ) {
                            finishService()
                        }
                    }

                    "point" -> {
                        event.location?.let {
                            accept(it)
                        }
                    }

                    "weak" -> {
                        if (
                            LiveWorkoutStore.state.value.phase ==
                            LivePhase.ACTIVE
                        ) {
                            LiveWorkoutStore.publish(
                                LiveWorkoutStore.state.value.copy(
                                    status = GpsStatus.WEAK,
                                    currentSpeedMps = null
                                )
                            )
                        }
                    }

                    "permission" -> {
                        pause()

                        LiveWorkoutStore.publish(
                            LiveWorkoutStore.state.value.copy(
                                status = GpsStatus.PERMISSION
                            )
                        )

                        checkpoint()
                    }
                }

                if (
                    !finishing &&
                    LiveWorkoutStore.state.value.active
                ) {
                    if (
                        SystemClock.elapsedRealtime() -
                        lastPersist >= 5000
                    ) {
                        checkpoint()
                    }

                    refreshNotification()
                }
            }
        }

        scope.launch {
            while (isActive) {
                delay(1000)
                events.send(
                    Event("tick")
                )
            }
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (alive) {
            events.trySend(
                Event(
                    action = intent?.action ?: "recover",
                    type =
                        intent?.getStringExtra("type")
                            ?: "run",
                    note =
                        intent?.getStringExtra("note")
                            ?: "",
                    variantId =
                        intent
                            ?.getStringExtra("variant")
                            .orEmpty(),
                    sessionTitle =
                        intent
                            ?.getStringExtra("title")
                            .orEmpty()
                )
            )
        }

        return START_NOT_STICKY
    }

    private fun tick() {
        val now =
            SystemClock.elapsedRealtime()

        val s =
            LiveWorkoutStore.state.value

        if (
            s.phase == LivePhase.ACTIVE &&
            lastTick > 0
        ) {
            val stale =
                lastFixElapsed > 0 &&
                        now - lastFixElapsed > 20000

            LiveWorkoutStore.publish(
                s.copy(
                    elapsedMs =
                        s.elapsedMs +
                                (now - lastTick).coerceAtLeast(0),

                    status =
                        if (
                            stale &&
                            s.status == GpsStatus.GOOD
                        ) {
                            GpsStatus.WEAK
                        } else {
                            s.status
                        },

                    currentSpeedMps =
                        when {
                            stale ||
                                    s.status != GpsStatus.GOOD -> null

                            lastMotionElapsed > 0 &&
                                    now - lastMotionElapsed > 10_000 -> 0f

                            else -> s.currentSpeedMps
                        }
                )
            )
        }

        lastTick = now
    }

    private fun pause() {
        removeLocations()

        lastAccepted = null
        speedWindow.clear()

        val s =
            LiveWorkoutStore.state.value

        if (s.active) {
            LiveWorkoutStore.publish(
                s.copy(
                    phase = LivePhase.PAUSED,
                    accuracy = null,
                    currentSpeedMps = null
                )
            )
        }
    }

    private fun requestLocations() {
        if (updatesRequested) {
            return
        }

        if (
            !LiveWorkoutController.precise(this)
        ) {
            events.trySend(
                Event("permission")
            )
            return
        }

        val request =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
            )
                .setMinUpdateIntervalMillis(1500)
                .setMinUpdateDistanceMeters(2f)
                .setMaxUpdateDelayMillis(3000)
                .setWaitForAccurateLocation(true)
                .build()

        try {
            updatesRequested = true

            fused
                .requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
                .addOnFailureListener {
                    updatesRequested = false

                    events.trySend(
                        Event("permission")
                    )
                }

        } catch (_: SecurityException) {
            updatesRequested = false

            events.trySend(
                Event("permission")
            )
        }
    }

    private fun removeLocations() {
        if (::fused.isInitialized) {
            fused.removeLocationUpdates(
                callback
            )
        }

        updatesRequested = false
    }

    private fun accept(
        location: Location
    ) {
        val s =
            LiveWorkoutStore.state.value

        if (s.phase != LivePhase.ACTIVE) {
            return
        }

        val age =
            (
                    SystemClock.elapsedRealtimeNanos() -
                            location.elapsedRealtimeNanos
                    ) / 1_000_000

        if (
            !location.latitude.isFinite() ||
            !location.longitude.isFinite() ||
            !location.hasAccuracy() ||
            !location.accuracy.isFinite() ||
            location.accuracy <= 0 ||
            location.accuracy > 35 ||
            age !in 0..15000
        ) {
            LiveWorkoutStore.publish(
                s.copy(
                    status = GpsStatus.WEAK,
                    currentSpeedMps = null
                )
            )

            return
        }

        lastFixElapsed =
            SystemClock.elapsedRealtime()

        val previous =
            lastAccepted

        var segment =
            s.segment

        var distance =
            0f

        var intervalMs =
            0L

        if (previous != null) {
            val seconds =
                (
                        location.elapsedRealtimeNanos -
                                previous.elapsedRealtimeNanos
                        ) / 1e9

            if (seconds <= 0) {
                return
            }

            intervalMs =
                (
                        location.elapsedRealtimeNanos -
                                previous.elapsedRealtimeNanos
                        ) / 1_000_000

            if (seconds > 20) {
                segment++
            } else {
                distance =
                    previous.distanceTo(location)

                val maxSpeed =
                    maximumRouteSpeed(s.type)

                if (
                    distance / seconds >
                    maxSpeed
                ) {
                    LiveWorkoutStore.publish(
                        s.copy(
                            status = GpsStatus.WEAK,
                            currentSpeedMps = null
                        )
                    )

                    return
                }

                val noiseFloor =
                    maxOf(
                        3f,
                        minOf(
                            previous.accuracy,
                            location.accuracy
                        ) * .5f
                    )

                if (
                    distance <
                    noiseFloor
                ) {
                    LiveWorkoutStore.publish(
                        s.copy(
                            status = GpsStatus.GOOD,
                            accuracy = location.accuracy
                        )
                    )

                    return
                }
            }
        }

        if (
            segment != s.segment ||
            previous == null
        ) {
            distance = 0f
            speedWindow.clear()
        }

        val speed =
            if (
                distance > 0 &&
                intervalMs > 0
            ) {
                speedWindow.add(
                    distance,
                    intervalMs,
                    segment
                )
            } else {
                null
            }

        val range =
            PaceRange(
                s.fastestPaceSeconds,
                s.slowestPaceSeconds
            ).add(
                speed,
                speedWindow.readyForExtremes
            )

        if (
            distance > 0 ||
            previous == null
        ) {
            lastMotionElapsed =
                SystemClock.elapsedRealtime()
        }

        lastAccepted =
            Location(location)

        val point =
            TrackPoint(
                location.latitude,
                location.longitude,
                location.time,
                location.accuracy,
                segment,
                location.elapsedRealtimeNanos /
                        1_000_000
            )

        LiveWorkoutStore.publish(
            s.copy(
                points =
                    s.points + point,

                segment =
                    segment,

                distanceMeters =
                    s.distanceMeters + distance,

                status =
                    GpsStatus.GOOD,

                accuracy =
                    location.accuracy,

                currentSpeedMps =
                    speed,

                fastestPaceSeconds =
                    range.fastest,

                slowestPaceSeconds =
                    range.slowest
            )
        )
    }

    private suspend fun checkpoint() {
        lastPersist =
            SystemClock.elapsedRealtime()

        try {
            LiveWorkoutStore.persist(
                applicationContext,
                LiveWorkoutStore.state.value
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            LiveWorkoutStore.publish(
                LiveWorkoutStore.state.value.copy(
                    status = GpsStatus.STORAGE
                )
            )
        }
    }

    private var notificationKey = ""

    private fun refreshNotification() {
        val state =
            LiveWorkoutStore.state.value

        val key =
            "${state.phase}/${state.status}/" +
                    "${(state.distanceMeters / 10).toInt()}/" +
                    LanguagePreferences.get(this).code

        if (key != notificationKey) {
            notificationKey = key

            // FIX: use the class-level NotificationManager.
            runCatching {
                notificationManager.notify(
                    NOTIFICATION,
                    notification()
                )
            }
        }
    }

    private fun notification(): Notification {
        val s =
            LiveWorkoutStore.state.value

        val context =
            LanguagePreferences.localizedContext(this)

        val km =
            String.format(
                java.util.Locale.forLanguageTag(
                    LanguagePreferences.get(this).code
                ),
                "%.2f",
                s.distanceMeters / 1000
            )

        val intent =
            Intent(
                this,
                LiveWorkoutActivity::class.java
            ).addFlags(
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )

        return NotificationCompat.Builder(
            this,
            CHANNEL
        )
            .setSmallIcon(
                R.drawable.ic_running
            )
            .setContentTitle(
                text(
                    if (
                        s.phase == LivePhase.PAUSED
                    ) {
                        R.string.live_paused
                    } else {
                        R.string.live_notification_title
                    }
                )
            )
            .setContentText(
                if (
                    s.phase == LivePhase.ACTIVE
                ) {
                    context.getString(
                        R.string.widget_distance,
                        km
                    )
                } else {
                    context.getString(
                        R.string.live_notification_distance,
                        km,
                        com.example.fitnesstracker.utils.DateUtils
                            .formatDuration(
                                s.elapsedMs / 1000
                            )
                    )
                }
            )
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    410,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setWhen(
                System.currentTimeMillis() -
                        s.elapsedMs
            )
            .setUsesChronometer(
                s.phase == LivePhase.ACTIVE
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .setVisibility(
                NotificationCompat.VISIBILITY_PRIVATE
            )
            .build()
    }

    private fun text(
        id: Int
    ) =
        LanguagePreferences
            .localizedContext(this)
            .getString(id)

    private fun finishService() {
        finishing = true

        removeLocations()

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        stopSelf()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    override fun onDestroy() {
        alive = false

        removeLocations()
        events.close()
        scope.cancel()

        val s =
            LiveWorkoutStore.state.value

        if (
            !finishing &&
            s.active
        ) {
            LiveWorkoutStore.publish(
                s.copy(
                    phase = LivePhase.PAUSED,
                    status = GpsStatus.RECOVERED,
                    accuracy = null,
                    currentSpeedMps = null
                )
            )
        }

        super.onDestroy()
    }

    companion object {
        internal var alive = false
            private set

        private const val CHANNEL =
            "live_workout_location"

        private const val NOTIFICATION =
            4102
    }
}