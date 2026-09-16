package com.example.fitnesstracker.workout

import android.content.Context
import android.content.Intent
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.utils.Haptics
import com.example.fitnesstracker.utils.HapticTexture
import android.os.SystemClock
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.data.repository.*
import com.example.fitnesstracker.utils.StepCounter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject

class WorkoutSessionEngine private constructor(private val app: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val db = AppDatabase.getInstance(app)
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val achievementRepo = AchievementRepository(db.achievementDao())
    private val _liveState = MutableStateFlow(SessionDisplay())
    val liveState = _liveState.asStateFlow()
    private val _finishRequested = MutableStateFlow(false)
    val finishRequested = _finishRequested.asStateFlow()
    private var scheduledRest = 0L
    private val prefs = app.getSharedPreferences("workout_journal", android.content.Context.MODE_PRIVATE)
    private val stepCounter = StepCounter.getInstance(app.applicationContext)
    val history = workoutRepo.getAll().stateIn(scope, SharingStarted.Lazily, emptyList())
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()
    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()
    private val _workoutSteps = MutableStateFlow(0)
    val workoutSteps = _workoutSteps.asStateFlow()
    val stepsSensorAvailable = stepCounter.available
    private val _plan = MutableStateFlow(WorkoutCatalog.initial("general"))
    val plan = _plan.asStateFlow()
    val workoutType = plan.map { it.type }.stateIn(scope, SharingStarted.Eagerly, "general")
    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates = _templates.asStateFlow()
    private val _restRemaining = MutableStateFlow(0)
    val restRemaining = _restRemaining.asStateFlow()
    data class SetTimer(val exerciseId: String, val setId: String)
    private val _setTimer = MutableStateFlow<SetTimer?>(null)
    val setTimer = _setTimer.asStateFlow()
    private val _setSeconds = MutableStateFlow(0L)
    val setSeconds = _setSeconds.asStateFlow()
    private var setStartedMs = 0L
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val _message = MutableStateFlow<Int?>(null)
    val message = _message.asStateFlow()
    private var startedAt = 0L
    private var token = ""
    private var carriedMs = 0L
    private var segmentStart = 0L
    private var restUntil = 0L
    private var lastStepTotal = 0
    private var timer: Job? = null
    private val plans = mutableMapOf<String, WorkoutPlan>()
    private data class Checkpoint(val plan: WorkoutPlan, val plans: Map<String, WorkoutPlan>, val templates: List<WorkoutTemplate>,
        val active: Boolean, val startedAt: Long, val token: String, val elapsedMs: Long, val steps: Int, val rest: Int)
    private val writes = Channel<Checkpoint>(Channel.CONFLATED)

    init {
        scope.launch {
            try {
                val saved = withContext(Dispatchers.IO) { prefs.getString("state", null) }
                val json = withContext(Dispatchers.Default) { saved?.let { runCatching { JSONObject(it) }.getOrNull() } }
                if (json != null) {
                    withContext(Dispatchers.Default) {
                        val storedPlans = json.optJSONObject("plans") ?: JSONObject()
                        storedPlans.keys().forEach { key -> storedPlans.optJSONObject(key)?.let { plans[key] = WorkoutJournal.fromJson(it) } }
                        val templateArray = json.optJSONArray("templates") ?: JSONArray()
                        _templates.value = (0 until templateArray.length()).mapNotNull { i ->
                            val t = templateArray.optJSONObject(i) ?: return@mapNotNull null
                            val p = t.optJSONObject("plan") ?: return@mapNotNull null
                            WorkoutTemplate(t.optString("id").ifBlank { journalId() }, t.optString("name"), WorkoutJournal.fromJson(p))
                        }
                        json.optJSONObject("plan")?.let { _plan.value = WorkoutJournal.fromJson(it) }
                    }
                    token = json.optString("token")
                    if (json.optBoolean("active") && token.length >= 32 && !workoutRepo.hasSession(token)) {
                        startedAt = json.optLong("startedAt")
                        carriedMs = json.optLong("elapsedMs").coerceAtLeast(0)
                        _elapsedSeconds.value = carriedMs / 1000
                        _workoutSteps.value = json.optInt("steps").coerceAtLeast(0)
                        _restRemaining.value = json.optInt("rest").coerceIn(0, 3600)
                        _isRunning.value = true; _isPaused.value = true
                        _message.value = R.string.journal_recovered
                    } else _plan.value = _plan.value.fresh()
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _message.value = R.string.journal_storage_error }
            publishDisplay()
            _ready.value = true
            for (snapshot in writes) {
                try {
                    withContext(Dispatchers.IO) {
                        val state = JSONObject().put("plan", WorkoutJournal.toJson(snapshot.plan))
                            .put("active", snapshot.active).put("startedAt", snapshot.startedAt).put("token", snapshot.token)
                            .put("elapsedMs", snapshot.elapsedMs).put("steps", snapshot.steps).put("rest", snapshot.rest)
                            .put("plans", JSONObject().apply { snapshot.plans.forEach { (key, p) -> put(key, WorkoutJournal.toJson(p)) } })
                            .put("templates", JSONArray().apply { snapshot.templates.forEach { t ->
                                put(JSONObject().put("id", t.id).put("name", t.name).put("plan", WorkoutJournal.toJson(t.plan)))
                            } })
                        check(prefs.edit().putString("state", state.toString()).commit())
                    }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { _message.value = R.string.journal_storage_error }
            }
        }
        scope.launch {
            stepCounter.stepsToday.collect { total ->
                if (_isRunning.value && !_isPaused.value) _workoutSteps.value += (total - lastStepTotal).coerceAtLeast(0)
                lastStepTotal = total
            }
        }
    }

    private fun elapsedMs(): Long = carriedMs + if (_isRunning.value && !_isPaused.value) (SystemClock.elapsedRealtime() - segmentStart).coerceAtLeast(0) else 0
    private fun checkpoint() {
        if (!_ready.value) return
        val active = _setTimer.value
        val snapshotPlan = if (active == null) _plan.value else _plan.value.copy(exercises = _plan.value.exercises.map { e ->
            if (e.id != active.exerciseId) e else e.copy(sets = e.sets.map { row ->
                if (row.id != active.setId) row else row.copy(seconds = ((elapsedMs() - setStartedMs) / 1000).toInt().coerceIn(0, 86400))
            })
        })
        publishDisplay()
        writes.trySend(Checkpoint(snapshotPlan, plans.toMap(), _templates.value, _isRunning.value,
            startedAt, token, elapsedMs(), _workoutSteps.value, _restRemaining.value))
    }
    suspend fun clearJournal() {
        ready.first { it }
        saving.first { !it }
        reset()
        plans.clear(); _templates.value = emptyList(); _plan.value = WorkoutCatalog.initial("general")
        checkpoint()
    }
    fun clearMessage() { _message.value = null }
    fun setWorkoutType(type: String) {
        if (_isRunning.value || !_ready.value) return
        plans[_plan.value.type] = _plan.value
        _plan.value = plans[type] ?: WorkoutCatalog.initial(type)
        checkpoint()
    }
    fun updatePlan(value: WorkoutPlan) {
        if (_saving.value || !_ready.value) return
        _plan.value = value
        plans[value.type] = value.fresh()
        checkpoint()
    }
    fun saveExercise(exercise: JournalExercise) {
        val list = _plan.value.exercises
        updatePlan(_plan.value.copy(exercises = if (list.any { it.id == exercise.id }) list.map { if (it.id == exercise.id) exercise else it } else list + exercise))
    }
    fun deleteExercise(id: String) { updatePlan(_plan.value.copy(exercises = _plan.value.exercises.filterNot { it.id == id })) }
    fun moveExercise(id: String, delta: Int) {
        val list = _plan.value.exercises.toMutableList(); val from = list.indexOfFirst { it.id == id }
        val to = from + delta
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from); list.add(to, item); updatePlan(_plan.value.copy(exercises = list))
    }
    fun startSetTimer(exerciseId: String, setId: String) {
        if (!_isRunning.value || _isPaused.value || _saving.value || _setTimer.value != null) return
        val entry = _plan.value.exercises.firstOrNull { it.id == exerciseId && it.mode == EntryMode.TIME } ?: return
        if (entry.sets.none { it.id == setId && !it.completed }) return
        setStartedMs = elapsedMs(); _setSeconds.value = 0L
        _setTimer.value = SetTimer(exerciseId, setId)
        skipRest(); checkpoint()
    }
    fun finishSetTimer() {
        val active = _setTimer.value ?: return
        val seconds = ((elapsedMs() - setStartedMs) / 1000).toInt().coerceIn(1, 86400)
        _setTimer.value = null; _setSeconds.value = 0L
        val entry = _plan.value.exercises.firstOrNull { it.id == active.exerciseId } ?: return
        saveExercise(entry.copy(sets = entry.sets.map { if (it.id == active.setId) it.copy(seconds = seconds, completed = true) else it }))
        _restRemaining.value = entry.restSeconds
        restUntil = if (entry.restSeconds > 0) SystemClock.elapsedRealtime() + entry.restSeconds * 1000L else 0L
        checkpoint()
    }
    fun toggleSet(exerciseId: String, setId: String) {
        if (!_isRunning.value || _isPaused.value || _saving.value) return
        if (_setTimer.value?.setId == setId) { finishSetTimer(); return }
        if (_setTimer.value != null) return
        val e = _plan.value.exercises.firstOrNull { it.id == exerciseId } ?: return
        val s = e.sets.firstOrNull { it.id == setId } ?: return
        if (!s.completed && !s.hasResult(e.mode)) { _message.value = R.string.journal_result_needed; return }
        saveExercise(e.copy(sets = e.sets.map { if (it.id == setId) it.copy(completed = !it.completed) else it }))
        if (!s.completed) {
            _restRemaining.value = e.restSeconds
            restUntil = if (e.restSeconds > 0) SystemClock.elapsedRealtime() + e.restSeconds * 1000L else 0L
        } else skipRest()
        checkpoint()
    }
    fun skipRest() { _restRemaining.value = 0; restUntil = 0; WorkoutTimerService.clearRestAlert(app); checkpoint() }
    fun saveTemplate(name: String) {
        if (name.isBlank() || _saving.value) return
        _templates.value = _templates.value + WorkoutTemplate(name = name.trim().take(120), plan = _plan.value.fresh())
        checkpoint()
    }
    fun useTemplate(template: WorkoutTemplate) {
        if (_isRunning.value) return
        plans[_plan.value.type] = _plan.value
        _plan.value = template.plan.fresh(); checkpoint()
    }
    fun deleteTemplate(id: String) { _templates.value = _templates.value.filterNot { it.id == id }; checkpoint() }
    fun startWorkout() {
        if (_isRunning.value || !_ready.value || _saving.value) return
        WorkoutTimerService.clearRestAlert(app)
        _plan.value = _plan.value.fresh()
        token = journalId(); startedAt = System.currentTimeMillis(); carriedMs = 0L
        segmentStart = SystemClock.elapsedRealtime(); _workoutSteps.value = 0; _elapsedSeconds.value = 0
        _isPaused.value = false; _isRunning.value = true; _message.value = null
        lastStepTotal = stepCounter.stepsToday.value; stepCounter.start("workout")
        launchTimer(); checkpoint()
        ensureService()
        Haptics.play(app, HapticTexture.START)
    }
    fun pauseWorkout(feedback: Boolean = true) {
        if (!_isRunning.value || _isPaused.value) return
        carriedMs = elapsedMs(); _elapsedSeconds.value = carriedMs / 1000
        if (restUntil > 0) _restRemaining.value = ((restUntil - SystemClock.elapsedRealtime() + 999) / 1000).toInt().coerceAtLeast(0)
        _isPaused.value = true; timer?.cancel(); stepCounter.stop("workout"); checkpoint()
        if (feedback) Haptics.play(app, HapticTexture.PAUSE)
    }
    fun resumeWorkout() {
        if (!_isRunning.value || !_isPaused.value || _saving.value) return
        segmentStart = SystemClock.elapsedRealtime(); restUntil = if (_restRemaining.value > 0) segmentStart + _restRemaining.value * 1000L else 0L
        lastStepTotal = stepCounter.stepsToday.value; _isPaused.value = false; stepCounter.start("workout")
        launchTimer(); checkpoint()
        ensureService()
        Haptics.play(app, HapticTexture.START)
    }
    private fun launchTimer() {
        timer?.cancel()
        timer = scope.launch {
            var lastSaved = -1L
            while (isActive) {
                val seconds = elapsedMs() / 1000
                _elapsedSeconds.value = seconds
                if (_setTimer.value != null) _setSeconds.value = ((elapsedMs() - setStartedMs) / 1000).coerceAtLeast(0)
                if (restUntil > 0) {
                    _restRemaining.value = ((restUntil - SystemClock.elapsedRealtime() + 999) / 1000).toInt().coerceAtLeast(0)
                    if (_restRemaining.value == 0) onRestDeadline(token, restUntil)
                }
                if (seconds / 5 != lastSaved) { lastSaved = seconds / 5; checkpoint() }
                delay(250)
            }
        }
    }
    fun stopAndSave(note: String = "") {
        if (!_isRunning.value || _saving.value) return
        finishSetTimer(); pauseWorkout(feedback = false); _saving.value = true
        val current = _plan.value
        scope.launch {
            try {
                val payload = withContext(Dispatchers.Default) { JSONObject(WorkoutJournal.encode(current)).put("token", token).toString() }
                workoutRepo.insertSessionOnce(Workout(startTime = startedAt, endTime = System.currentTimeMillis(),
                    durationSeconds = carriedMs / 1000, steps = _workoutSteps.value, distanceMeters = current.recordedMeters,
                    type = current.type, note = note.trim(), metrics = payload), token)
                reset()
                Haptics.play(app, HapticTexture.CONFIRM)
                try {
                    achievementRepo.unlock("first_workout")
                    if (workoutRepo.count() >= 10) achievementRepo.unlock("ten_workouts")
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* The workout is already saved; badge failure must not report data loss. */ }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { _message.value = R.string.journal_save_error }
            finally { _saving.value = false }
        }
    }
    fun stopAndDiscard() { if (!_saving.value) reset() }
    private fun reset() {
        timer?.cancel(); stepCounter.stop("workout")
        _isRunning.value = false; _isPaused.value = false; _elapsedSeconds.value = 0; _workoutSteps.value = 0; _message.value = null
        _setTimer.value = null; _setSeconds.value = 0L; _finishRequested.value = false
        _restRemaining.value = 0; carriedMs = 0; restUntil = 0; startedAt = 0; token = ""
        _plan.value = _plan.value.fresh(); plans[_plan.value.type] = _plan.value; checkpoint()
    }
    fun deleteWorkout(workout: Workout) { scope.launch { workoutRepo.delete(workout) } }
    fun updateWorkout(workout: Workout, onResult: (Boolean) -> Unit) {
        if (workout.id <= 0 || workout.endTime < workout.startTime) { onResult(false); return }
        scope.launch {
            val saved = try { workoutRepo.update(workout) == 1 }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { false }
            onResult(saved)
        }
    }
    private fun ensureService() {
        if (!WorkoutTimerService.start(app)) {
            backgroundUnavailable()
        }
    }

    fun backgroundUnavailable() {
        pauseWorkout(feedback = false)
        _message.value = R.string.timer_background_unavailable
    }

    private fun publishDisplay() {
        val active = _isRunning.value
        val paused = _isPaused.value
        val set = _setTimer.value
        val base = if (active && !paused) segmentStart - carriedMs else 0L
        val display = SessionDisplay(active, paused, token, _plan.value.title, _plan.value.type,
            base, if (paused) carriedMs / 1000 else 0L,
            if (active && !paused) restUntil else 0L, if (paused) _restRemaining.value else 0,
            set?.let { _plan.value.exercises.firstOrNull { e -> e.id == it.exerciseId }?.name }.orEmpty(),
            set != null, if (set != null && !paused) base + setStartedMs else 0L,
            if (set != null && paused) ((carriedMs - setStartedMs) / 1000).coerceAtLeast(0) else 0L,
            _plan.value.completedCount, _plan.value.setCount)
        val displayChanged = _liveState.value != display
        _liveState.value = display
        val deadline = if (active && !paused) restUntil else 0L
        if (deadline != scheduledRest) {
            scheduledRest = deadline
            RestTimerAlarm.schedule(app, token, deadline)
        }
        if (!active) {
            app.stopService(Intent(app, WorkoutTimerService::class.java))
            WorkoutTimerService.clearRestAlert(app)
        }
        if (displayChanged) com.example.fitnesstracker.widgets.LeafWidgets.requestUpdate(app)
    }

    fun onRestDeadline(expectedToken: String, deadline: Long) {
        if (!_isRunning.value || _isPaused.value || token != expectedToken || restUntil != deadline || deadline <= 0L) return
        if (SystemClock.elapsedRealtime() < deadline) return
        restUntil = 0L; _restRemaining.value = 0
        checkpoint()
        WorkoutTimerService.restFinished(app)
    }

    fun requestFinish(expectedToken: String) {
        if (_isRunning.value && token == expectedToken && !_saving.value) {
            finishSetTimer(); pauseWorkout(feedback = false); _finishRequested.value = true
        }
    }
    fun consumeFinishRequest() { _finishRequested.value = false }
    fun matchesToken(value: String?) = _isRunning.value && token.isNotBlank() && token == value

    companion object {
        @Volatile private var instance: WorkoutSessionEngine? = null
        fun get(context: Context): WorkoutSessionEngine = instance ?: synchronized(this) {
            instance ?: WorkoutSessionEngine(context.applicationContext).also { instance = it }
        }
        fun current(): WorkoutSessionEngine? = instance
    }
}
