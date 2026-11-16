package com.example.fitnesstracker.sleep

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import com.example.fitnesstracker.utils.LeafNotify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

class SleepTrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private var settings = SleepTrackSettings()
    private var start = 0L
    @Volatile private var running = false
    @Volatile private var ringing = false
    private var audioAllowed = false
    private var wakeLock: PowerManager.WakeLock? = null
    private val noise = ArrayList<Int>()
    private val events = ArrayList<SleepEvent>()
    private var noiseSum = 0f
    private var noiseCount = 0
    @Volatile private var lastSound = 0L
    private var alarmAt = 0L
    private var smart = true
    private var player: MediaPlayer? = null
    private var ringJob: Job? = null
    private var clips = 0
    private var lastAntiSnore = 0L
    private var snoozes = 0
    private var alarmOnly = false
    private var alarmId = 0
    private var internalSnooze = false
    private var ringLabel = ""
    private val eventLock = Any()
    @Volatile private var inEvent = false
    private var eventStart = 0L
    private var loud = 0
    private var peak = 0
    private var bursts = 0
    private var zcrSum = 0f
    private var zcrCount = 0
    private var clipWriter: WavWriter? = null
    private var clipName: String? = null
    private var lullabyEnd = 0L
    @Volatile private var lullabyPlaying = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            SleepTracker.ACTION_STOP -> when {
                running -> finish(true)
                ringing -> endAlarmOnly()
                else -> stopSelf()
            }
            SleepTracker.ACTION_DISMISS -> when {
                ringing && running -> { stopRing(); refreshAlarm(System.currentTimeMillis()); publish(); notifyNow() }
                ringing -> endAlarmOnly()
                !running -> stopSelf()
            }
            SleepTracker.ACTION_DISCARD -> if (running) finish(false) else if (ringing) endAlarmOnly() else stopSelf()
            SleepTracker.ACTION_SNOOZE -> snooze()
            SleepTracker.ACTION_RING, SleepTracker.ACTION_TEST -> onRing(intent?.getStringExtra("alarm_label").orEmpty())
            else -> begin()
        }
        return START_STICKY
    }

    private fun onRing(label: String) {
        if (ringing) return
        ringLabel = label
        if (!running) {
            alarmOnly = true
            settings = SleepTrackPrefs.load(this)
            channels()
            snoozes = 0
        }
        ring()
    }

    private fun endAlarmOnly() {
        stopRing()
        alarmOnly = false
        SleepTracker.state.value = SleepLive()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun refreshAlarm(now: Long) {
        if (internalSnooze && alarmAt > now) return
        internalSnooze = false
        val next = SleepAlarms.upcoming(this, now)
        if (next != null && next.second - start < (settings.maxHours + 1) * 3600000L) {
            alarmAt = next.second
            alarmId = next.first.id
            smart = next.first.smart
        } else {
            alarmAt = 0L
            alarmId = 0
        }
        SleepTrackPrefs.setAlarmAt(this, alarmAt)
    }

    private fun begin() {
        if (running) return
        settings = SleepTrackPrefs.load(this)
        val now = System.currentTimeMillis()
        val saved = SleepTrackPrefs.activeStart(this)
        val resumed = saved > 0L && now - saved < settings.maxHours * 3600000L
        start = if (resumed) saved else now
        if (resumed) SleepNightStore.load(this, start)?.let { night ->
            noise += night.noise; events += night.events
            clips = night.events.count { it.clip != null }
        }
        SleepTrackPrefs.setActiveStart(this, start)
        channels()
        audioAllowed = settings.sound &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!enterForeground()) {
            if (resumed) SleepTracker.finalize(this, snapshot(now)) else SleepTrackPrefs.setActiveStart(this, 0L)
            stopSelf()
            return
        }
        running = true
        alarmOnly = false
        snoozes = 0
        refreshAlarm(now)
        lullabyEnd = if (settings.lullaby > 0) start + settings.lullabyMinutes * 60000L else 0L
        runCatching {
            wakeLock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "leafit:sleep").apply {
                setReferenceCounted(false)
                acquire((settings.maxHours + 1) * 3600000L)
            }
        }
        publish()
        if (audioAllowed) scope.launch(Dispatchers.IO) { audioLoop() }
        if (settings.lullaby > 0 && now < lullabyEnd) scope.launch(Dispatchers.IO) { lullabyLoop() }
        scope.launch { ticker() }
    }

    private fun enterForeground(): Boolean {
        val notification = notification()
        if (audioAllowed) {
            try {
                ServiceCompat.startForeground(this, NOTIFICATION, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                return true
            } catch (_: Exception) {
                audioAllowed = false
            }
        }
        return try {
            ServiceCompat.startForeground(this, NOTIFICATION, notification,
                if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun ticker() {
        var minuteMark = SystemClock.elapsedRealtime()
        var minutes = 0
        while (running && scope.isActive) {
            delay(15000)
            if (!running) break
            val now = System.currentTimeMillis()
            if (SystemClock.elapsedRealtime() - minuteMark >= 60000L) {
                minuteMark = SystemClock.elapsedRealtime()
                flushMinute()
                minutes++
                if (minutes % 3 == 0) SleepNightStore.save(this, snapshot(now))
                if (!ringing) { refreshAlarm(now); publish() }
                notifyNow()
            }
            checkAlarm(now)
            if (now - start > settings.maxHours * 3600000L) { finish(true); break }
        }
    }

    private fun flushMinute() = synchronized(lock) {
        if (noise.size < 1200) {
            noise.add(if (noiseCount > 0) (noiseSum / noiseCount).toInt() else 0)
        }
        noiseSum = 0f; noiseCount = 0
    }

    private fun snapshot(end: Long): SleepNight = synchronized(lock) {
        SleepNight(start, end, ArrayList(noise), ArrayList(events))
    }

    private fun publish() {
        val count = synchronized(lock) { events.size }
        val snores = synchronized(lock) { events.count { it.type == SleepEvent.SNORE } }
        SleepTracker.state.update {
            it.copy(active = running, start = start, events = count, snores = snores, ringing = ringing, alarmAt = alarmAt,
                sound = audioAllowed, math = settings.mathDismiss, label = ringLabel,
                lullaby = lullabyPlaying,
                canSnooze = settings.snoozeLimit == 0 || snoozes < settings.snoozeLimit)
        }
    }

    private fun closeEvent(now: Long) = synchronized(eventLock) {
        if (!inEvent) return@synchronized
        clipWriter?.close()
        clipWriter = null
        val average = if (zcrCount > 0) zcrSum / zcrCount else 0f
        val type = when {
            bursts >= 3 && average < .09f -> SleepEvent.SNORE
            loud >= 2 && average >= .06f && average <= .22f -> SleepEvent.TALK
            else -> SleepEvent.NOISE
        }
        val name = clipName
        if (name != null && settings.recordFilter == 1 && type == SleepEvent.NOISE) {
            runCatching { File(SleepNightStore.clipDir(this), name).delete() }
            clipName = null
        }
        if (clipName != null) clips++
        synchronized(lock) { if (events.size < 400) events.add(SleepEvent(eventStart, loud.coerceAtLeast(1), peak, type, clipName)) }
        if (type == SleepEvent.SNORE && settings.antiSnore && running && now - lastAntiSnore > 5 * 60000L) {
            lastAntiSnore = now
            buzz(350)
        }
        inEvent = false
        clipName = null
    }

    private suspend fun audioLoop() {
        val rate = 16000
        val chunk = ShortArray(1600)
        val threshold = floatArrayOf(14f, 10f, 7f)[settings.sensitivity.coerceIn(0, 2)]
        val preRoll = ArrayDeque<ShortArray>()
        var floor = -1f
        var quiet = 0
        var wasLoud = false
        while (running) {
            val minimum = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val record = try {
                AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minimum, rate * 2))
            } catch (_: Exception) {
                null
            }
            if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
                runCatching { record?.release() }
                delay(5000)
                continue
            }
            try {
                record.startRecording()
                var energy = 0.0
                var samples = 0
                var crossings = 0
                var chunks = 0
                while (running) {
                    val read = record.read(chunk, 0, chunk.size)
                    if (read <= 0) break
                    var previous = chunk[0]
                    for (i in 0 until read) {
                        val value = chunk[i]
                        energy += value.toDouble() * value
                        if ((value >= 0) != (previous >= 0)) crossings++
                        previous = value
                    }
                    samples += read
                    chunks++
                    synchronized(eventLock) { clipWriter?.write(chunk, read) }
                    if (settings.recordings) {
                        preRoll.addLast(chunk.copyOf(read))
                        if (preRoll.size > 20) preRoll.removeFirst()
                    }
                    if (chunks < 10) continue
                    val rms = sqrt(energy / samples.coerceAtLeast(1))
                    val db = (20.0 * log10(rms.coerceAtLeast(1.0) / 32768.0) + 90.0).toFloat().coerceIn(0f, 90f)
                    val zcr = crossings.toFloat() / samples.coerceAtLeast(1)
                    energy = 0.0; samples = 0; crossings = 0; chunks = 0
                    floor = when {
                        floor < 0f -> db
                        db < floor -> floor * .9f + db * .1f
                        else -> floor * .998f + db * .002f
                    }
                    synchronized(lock) { noiseSum += db; noiseCount++ }
                    val now = System.currentTimeMillis()
                    val isLoud = !ringing && !lullabyPlaying && now - start >= settings.startDelay * 60000L && db > floor + threshold
                    if (isLoud) {
                        lastSound = now
                        synchronized(eventLock) {
                            if (!inEvent) {
                                inEvent = true; eventStart = now; loud = 0; peak = 0; bursts = 0; zcrSum = 0f; zcrCount = 0
                                quiet = 0
                                if (settings.recordings && clips < settings.maxClips) {
                                    val name = "${start}_$now.wav"
                                    val writer = runCatching { WavWriter(File(SleepNightStore.clipDir(this), name), rate) }.getOrNull()
                                    clipWriter = writer
                                    clipName = if (writer != null) name else null
                                    if (writer != null) preRoll.forEach { writer.write(it, it.size) }
                                }
                            }
                            if (!wasLoud) bursts++
                            loud++
                            if (db.toInt() > peak) peak = db.toInt()
                            zcrSum += zcr
                            zcrCount++
                        }
                        quiet = 0
                    } else if (inEvent) quiet++
                    wasLoud = isLoud
                    if (inEvent) {
                        val length = ((now - eventStart) / 1000L).toInt()
                        if (length >= settings.clipSeconds) synchronized(eventLock) { clipWriter?.close(); clipWriter = null }
                        if (quiet >= 6 || length >= 60) {
                            closeEvent(now)
                            publish()
                        }
                    }
                    val level = ((db - floor) / 30f).coerceIn(0f, 1f)
                    SleepTracker.state.update { it.copy(level = level, db = db.toInt()) }
                }
            } catch (_: Exception) {
            } finally {
                closeEvent(System.currentTimeMillis())
                runCatching { record.stop() }
                runCatching { record.release() }
            }
            if (running) delay(3000)
        }
    }


    private fun checkAlarm(now: Long) {
        if (alarmAt <= 0L || ringing) return
        val window = if (smart) settings.smartWindow * 60000L else 0L
        val stirring = now - lastSound < 90000L
        val early = window > 0L && now >= alarmAt - window && stirring && now - start > 30 * 60000L
        if (now >= alarmAt || early) {
            if (!internalSnooze) SleepAlarms.consume(this, alarmId, alarmAt)
            internalSnooze = false
            ring()
        }
    }

    private fun ring() {
        val peakVolume = settings.alarmVolume / 100f
        ringing = true
        if (alarmOnly) {
            val started = try {
                ServiceCompat.startForeground(this, NOTIFICATION, notification(),
                    if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0)
                true
            } catch (_: Exception) {
                false
            }
            if (!started) { ringing = false; alarmOnly = false; stopSelf(); return }
        }
        publish()
        runCatching {
            val uri = settings.alarmUri.takeIf { it.isNotBlank() }?.let { android.net.Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(this@SleepTrackingService, uri)
                isLooping = true
                prepare()
                val volume = if (settings.gradual) .08f * peakVolume else peakVolume
                setVolume(volume, volume)
                start()
            }
        }
        ringJob?.cancel()
        ringJob = scope.launch {
            var volume = .08f * peakVolume
            val step = peakVolume * 2f / settings.rampSeconds.coerceAtLeast(2)
            var elapsed = 0
            while (isActive && ringing) {
                if (settings.vibrate) buzz(if (settings.strongVibration) 1200 else 500)
                delay(2000)
                elapsed += 2
                if (settings.gradual) {
                    volume = (volume + step).coerceAtMost(peakVolume)
                    runCatching { player?.setVolume(volume, volume) }
                }
                if (elapsed >= 15 * 60) {
                    if (settings.snoozeLimit == 0 || snoozes < settings.snoozeLimit) snooze()
                    else if (running) { stopRing(); refreshAlarm(System.currentTimeMillis()); publish(); notifyNow() }
                    else endAlarmOnly()
                    break
                }
            }
        }
        notifyNow()
    }

    private fun stopRing() {
        ringing = false
        ringJob?.cancel()
        ringJob = null
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }

    private fun snooze() {
        if (!ringing) {
            if (!running) stopSelf()
            return
        }
        if (settings.snoozeLimit > 0 && snoozes >= settings.snoozeLimit) return
        snoozes++
        stopRing()
        val at = System.currentTimeMillis() + settings.snooze * 60000L
        if (running) {
            smart = false
            internalSnooze = true
            alarmAt = at
            SleepTrackPrefs.setAlarmAt(this, alarmAt)
            publish()
            notifyNow()
        } else {
            SleepAlarms.setSnooze(this, at)
            alarmOnly = false
            SleepTracker.state.value = SleepLive()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private suspend fun lullabyLoop() {
        val rate = 22050
        val track = runCatching {
            android.media.AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(maxOf(android.media.AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT), rate))
                .setTransferMode(android.media.AudioTrack.MODE_STREAM).build()
        }.getOrNull() ?: return
        val buffer = ShortArray(2205)
        val random = java.util.Random()
        var b0 = 0f
        var b1 = 0f
        var b2 = 0f
        var brown = 0f
        try {
            track.play()
            lullabyPlaying = true
            publish()
            while (running && !ringing) {
                val remaining = lullabyEnd - System.currentTimeMillis()
                if (remaining <= 0L) break
                val gain = settings.lullabyVolume / 100f * (remaining / 60000f).coerceIn(0f, 1f)
                for (i in buffer.indices) {
                    val white = random.nextFloat() * 2f - 1f
                    val value = when (settings.lullaby) {
                        2 -> {
                            b0 = .99765f * b0 + white * .0990460f
                            b1 = .96300f * b1 + white * .2965164f
                            b2 = .57000f * b2 + white * 1.0526913f
                            (b0 + b1 + b2 + white * .1848f) * .2f
                        }
                        3 -> {
                            brown = (brown + .02f * white) / 1.02f
                            brown * 3.5f
                        }
                        else -> white * .5f
                    }
                    buffer[i] = (value * gain * 14000f).toInt().coerceIn(-32767, 32767).toShort()
                }
                if (track.write(buffer, 0, buffer.size) < 0) break
            }
        } catch (_: Exception) {
        } finally {
            lullabyPlaying = false
            runCatching { track.stop() }
            runCatching { track.release() }
            if (running) publish()
        }
    }

    private fun buzz(millis: Long) {
        runCatching {
            getSystemService(Vibrator::class.java)?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun release() {
        stopRing()
        runCatching { wakeLock?.let { if (it.isHeld) it.release() } }
        wakeLock = null
    }

    private fun finish(save: Boolean) {
        if (!running) { stopSelf(); return }
        running = false
        release()
        closeEvent(System.currentTimeMillis())
        flushMinute()
        val night = snapshot(System.currentTimeMillis())
        SleepTracker.state.value = SleepLive()
        if (save) SleepTracker.finalize(this, night) else {
            night.events.forEach { event -> event.clip?.let { runCatching { File(SleepNightStore.clipDir(this), it).delete() } } }
            SleepTrackPrefs.setActiveStart(this, 0L)
        }
        SleepTrackPrefs.setAlarmAt(this, 0L)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (running) {
            running = false
            flushMinute()
            SleepNightStore.save(this, snapshot(System.currentTimeMillis()))
            SleepTracker.state.value = SleepLive()
        }
        release()
        scope.cancel()
        super.onDestroy()
    }

    private fun channels() {
        val ctx = LanguagePreferences.localizedContext(this)
        getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(
            NotificationChannel(CHANNEL, ctx.getString(R.string.b11_sleep_tracker), NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            },
            NotificationChannel(ALARM_CHANNEL, ctx.getString(R.string.b11_sleep_alarm), NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(false)
            }))
        LeafNotify.organize(this)
    }

    private fun action(name: String, code: Int): PendingIntent = PendingIntent.getService(this, code,
        Intent(this, SleepTrackingService::class.java).setAction(name), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private fun notification(): android.app.Notification {
        val ctx = LanguagePreferences.localizedContext(this)
        val count = synchronized(lock) { events.size }
        val builder = NotificationCompat.Builder(this, if (ringing) ALARM_CHANNEL else CHANNEL)
            .setSmallIcon(R.drawable.ms_bedtime)
            .setColor(LeafNotify.SLEEP)
            .setOngoing(true)
            .setOnlyAlertOnce(!ringing)
            .setContentIntent(LeafNotify.open(this, "sleep", 4390))
        if (ringing) {
            val screen = PendingIntent.getActivity(this, 4393, Intent(this, SleepAlarmActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.setContentTitle(ringLabel.ifBlank { ctx.getString(R.string.b11_sleep_alarm) })
                .setContentText(DateUtils.formatTime(System.currentTimeMillis()))
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(screen)
                .setFullScreenIntent(screen, true)
            if (settings.snoozeLimit == 0 || snoozes < settings.snoozeLimit) builder.addAction(0,
                ctx.getString(R.string.b11_sleep_snooze, settings.snooze), action(SleepTracker.ACTION_SNOOZE, 4392))
            if (!settings.mathDismiss) builder.addAction(0, ctx.getString(R.string.b13_alarm_dismiss),
                action(SleepTracker.ACTION_DISMISS, 4394))
        } else {
            val details = buildList {
                if (audioAllowed) add(ctx.getString(R.string.b11_sleep_events, count))
                if (alarmAt > 0L) add(ctx.getString(R.string.b11_sleep_alarm_at, DateUtils.formatTime(alarmAt)))
            }.joinToString(" · ")
            builder.setContentTitle(ctx.getString(R.string.b11_sleep_tracking_on))
                .setContentText(details.ifEmpty { ctx.getString(R.string.b11_sleep_tracking_hint) })
                .setUsesChronometer(true)
                .setWhen(start)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(0, ctx.getString(R.string.b11_sleep_finish), action(SleepTracker.ACTION_STOP, 4391))
            return LeafNotify.card(this, builder, R.drawable.ms_bedtime, ctx.getString(R.string.b11_sleep_tracking_on), "",
                caption = ctx.getString(R.string.b11_sleep_since, DateUtils.formatTime(start)),
                stats = buildList {
                    if (audioAllowed) add(ctx.getString(R.string.b11_sleep_events, count))
                    if (alarmAt > 0L) add(ctx.getString(R.string.b11_sleep_alarm_at, DateUtils.formatTime(alarmAt)))
                    if (lullabyPlaying) add(ctx.getString(R.string.b13_sleep_lullaby))
                },
                clockBase = (SystemClock.elapsedRealtime() - (System.currentTimeMillis() - start)).coerceAtLeast(1L), sleep = true)
        }
        return LeafNotify.card(this, builder, R.drawable.ms_bedtime, ringLabel.ifBlank { ctx.getString(R.string.b11_sleep_alarm) },
            DateUtils.formatTime(System.currentTimeMillis()), caption = ctx.getString(R.string.b13_alarm_ring), sleep = true)
    }

    private fun notifyNow() {
        if (!running && !alarmOnly) return
        runCatching { getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification()) }
    }

    private class WavWriter(file: File, private val rate: Int) {
        private val out = RandomAccessFile(file, "rw")
        private val buffer = ByteBuffer.allocate(3200).order(ByteOrder.LITTLE_ENDIAN)
        private var bytes = 0
        private var closed = false

        init {
            out.setLength(0)
            out.write(ByteArray(44))
        }

        fun write(data: ShortArray, count: Int) {
            if (closed) return
            runCatching {
                buffer.clear()
                for (i in 0 until count.coerceAtMost(1600)) buffer.putShort(data[i])
                out.write(buffer.array(), 0, buffer.position())
                bytes += buffer.position()
            }
        }

        fun close() {
            if (closed) return
            closed = true
            runCatching {
                val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                header.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + bytes).put("WAVE".toByteArray(Charsets.US_ASCII))
                header.put("fmt ".toByteArray(Charsets.US_ASCII)).putInt(16).putShort(1).putShort(1).putInt(rate).putInt(rate * 2)
                header.putShort(2).putShort(16).put("data".toByteArray(Charsets.US_ASCII)).putInt(bytes)
                out.seek(0)
                out.write(header.array())
            }
            runCatching { out.close() }
        }
    }

    companion object {
        private const val CHANNEL = "sleep_tracking"
        private const val ALARM_CHANNEL = "sleep_alarm"
        private const val NOTIFICATION = 4395
    }
}
