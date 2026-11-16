package com.example.fitnesstracker.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class RecipeIngredient(
    val name: String, val grams: Float, val calories100: Float,
    val proteins100: Float, val fats100: Float, val carbs100: Float
)

data class LeafRecipe(val id: String = UUID.randomUUID().toString(), val name: String,
    val cookedGrams: Float, val ingredients: List<RecipeIngredient>) {
    fun portion(grams: Float): FloatArray {
        val factor = grams.coerceAtLeast(0f) / cookedGrams.coerceAtLeast(1f)
        return floatArrayOf(
            ingredients.sumOf { (it.calories100 * it.grams / 100f).toDouble() }.toFloat() * factor,
            ingredients.sumOf { (it.proteins100 * it.grams / 100f).toDouble() }.toFloat() * factor,
            ingredients.sumOf { (it.fats100 * it.grams / 100f).toDouble() }.toFloat() * factor,
            ingredients.sumOf { (it.carbs100 * it.grams / 100f).toDouble() }.toFloat() * factor
        )
    }
}

data class PlannedMeal(val id: String = UUID.randomUUID().toString(), val day: Long,
    val recipeId: String, val grams: Float, val prepared: Boolean = false)

data class EquipmentProfile(val id: String = UUID.randomUUID().toString(),
    val name: String, val equipment: Set<String>)

data class ProgressCheck(val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(), val chest: Float? = null,
    val waist: Float? = null, val hips: Float? = null, val bicep: Float? = null,
    val photo: String? = null, val measurements: Map<String, Float> = emptyMap())

data class WorkoutAppointment(val time: Long, val templateId: String, val title: String)

data class UserExercise(val id: String = UUID.randomUUID().toString(), val name: String,
    val mode: String, val muscle: String, val equipment: String, val technique: String, val movement: String = "mobility")

data class ShoppingItem(val id: String = UUID.randomUUID().toString(), val name: String,
    val amount: String = "", val checked: Boolean = false)

data class ChallengeRun(val id: String = UUID.randomUUID().toString(), val kind: String,
    val title: String = "", val metric: String, val start: Long, val days: Int = 7,
    val goal: Int, val threshold: Int = 0, val tone: Int = 0, val manual: Int = 0)

data class FinishedChallenge(val kind: String, val week: Long, val completedAt: Long, val progress: Int,
    val goal: Int, val title: String = "", val runId: String = "")

data class LifeState(
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val challengeRuns: List<ChallengeRun> = emptyList(),
    val challengeFavorites: Set<String> = emptySet(),
    val challengesHidden: Boolean = false,
    val challengesCompact: Boolean = false,
    val completedChallenges: List<FinishedChallenge> = emptyList(),
    val recipes: List<LeafRecipe> = emptyList(),
    val meals: List<PlannedMeal> = emptyList(),
    val shoppingChecked: Set<String> = emptySet(),
    val equipmentProfiles: List<EquipmentProfile> = emptyList(),
    val activeEquipmentId: String? = null,
    val progress: List<ProgressCheck> = emptyList(),
    val appointment: WorkoutAppointment? = null,
    val challengeId: String? = null,
    val challengeStart: Long = 0L,
    val favoriteTemplates: Set<String> = emptySet(),
    val recentTemplates: List<String> = emptyList(),
    val customExercises: List<UserExercise> = emptyList()
)

class LifeStore private constructor(private val context: Context) {
    private val prefs = context.getSharedPreferences("leafit_life_v1", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(decode(prefs.getString("state", null)))
    val state = mutable.asStateFlow()

    private val pendingPhotoRemoval = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val writes = kotlinx.coroutines.channels.Channel<LifeState>(kotlinx.coroutines.channels.Channel.CONFLATED)
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    private val mutableError = MutableStateFlow(false)
    val storageError = mutableError.asStateFlow()

    init {
        scope.launchWriter()
    }

    private fun kotlinx.coroutines.CoroutineScope.launchWriter() = launch {
        for (next in writes) {
            mutableError.value = !runCatching {
                prefs.edit().putString("state", encode(next).toString()).commit()
            }.getOrDefault(false)
            if (!mutableError.value) {
                val candidates = synchronized(pendingPhotoRemoval) { pendingPhotoRemoval.toList() }
                candidates.forEach { path ->
                    if (next.progress.none { it.photo == path } && mutable.value.progress.none { it.photo == path }) {
                        val file = File(path)
                        if (file.parentFile == File(context.filesDir, "progress_photos")) file.delete()
                        pendingPhotoRemoval.remove(path)
                    }
                }
            }
        }
    }

    @Synchronized
    fun update(block: (LifeState) -> LifeState) {
        val next = block(mutable.value)
        if (next == mutable.value) return
        mutable.value = next
        writes.trySend(next)
    }

    fun importPhoto(uri: Uri): String? = runCatching {
        val dir = File(context.filesDir, "progress_photos").apply { check(isDirectory || mkdirs()) }
        val file = File(dir, UUID.randomUUID().toString() + ".jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        check(total <= 12_000_000L)
                        output.write(buffer, 0, count)
                    }
                }
            } ?: return@runCatching null
            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
            check(bounds.outWidth > 0 && bounds.outHeight > 0)
            file.absolutePath
        } catch (failure: Exception) {
            file.delete()
            throw failure
        }
    }.getOrNull()

    fun removeProgress(id: String) {
        mutable.value.progress.firstOrNull { it.id == id }?.photo?.let { pendingPhotoRemoval.add(it) }
        update { it.copy(progress = it.progress.filterNot { row -> row.id == id }) }
    }

    fun discardPhoto(path: String?) {
        if (path == null || mutable.value.progress.any { it.photo == path }) return
        val file = File(path)
        if (file.parentFile == File(context.filesDir, "progress_photos")) file.delete()
    }

    fun snapshot(): JSONObject = encode(mutable.value)

    fun restore(json: JSONObject) { update { decode(json.toString()) } }

    fun clear() {
        mutable.value.progress.mapNotNull { it.photo }.forEach { pendingPhotoRemoval.add(it) }
        update { LifeState() }
    }

    private fun encode(s: LifeState): JSONObject = JSONObject().apply {
        put("manualShopping", JSONArray().apply { s.shoppingItems.forEach { item ->
            put(JSONObject().put("id", item.id).put("name", item.name).put("amount", item.amount).put("checked", item.checked))
        } })
        put("challengeRuns", JSONArray().apply { s.challengeRuns.forEach { run ->
            put(JSONObject().put("id", run.id).put("kind", run.kind).put("title", run.title).put("metric", run.metric)
                .put("start", run.start).put("days", run.days).put("goal", run.goal).put("threshold", run.threshold)
                .put("tone", run.tone).put("manual", run.manual))
        } })
        put("challengeFavorites", JSONArray(s.challengeFavorites.toList()))
        put("challengesHidden", s.challengesHidden)
        put("challengesCompact", s.challengesCompact)
        put("recipes", JSONArray().apply { s.recipes.forEach { r ->
            put(JSONObject().put("id", r.id).put("name", r.name).put("cooked", r.cookedGrams)
                .put("ingredients", JSONArray().apply { r.ingredients.forEach { i ->
                    put(JSONObject().put("name", i.name).put("grams", i.grams)
                        .put("cal", i.calories100).put("protein", i.proteins100)
                        .put("fat", i.fats100).put("carb", i.carbs100))
                } }))
        } })
        put("meals", JSONArray().apply { s.meals.forEach { m ->
            put(JSONObject().put("id", m.id).put("day", m.day).put("recipe", m.recipeId)
                .put("grams", m.grams).put("prepared", m.prepared))
        } })
        put("shopping", JSONArray(s.shoppingChecked.toList()))
        put("equipment", JSONArray().apply { s.equipmentProfiles.forEach { p ->
            put(JSONObject().put("id", p.id).put("name", p.name).put("items", JSONArray(p.equipment.toList())))
        } })
        put("activeEquipment", s.activeEquipmentId)
        put("progress", JSONArray().apply { s.progress.forEach { p ->
            put(JSONObject().put("id", p.id).put("date", p.date).put("chest", p.chest)
                .put("waist", p.waist).put("hips", p.hips).put("bicep", p.bicep).put("photo", p.photo)
                .put("measurements", JSONObject(p.measurements.mapValues { it.value.toDouble() })))
        } })
        s.appointment?.let { put("appointment", JSONObject().put("time", it.time)
            .put("template", it.templateId).put("title", it.title)) }
        put("challenge", s.challengeId)
        put("challengeStart", s.challengeStart)
        put("completedChallenges", JSONArray().apply { s.completedChallenges.forEach { c ->
            put(JSONObject().put("kind", c.kind).put("week", c.week).put("at", c.completedAt)
                .put("progress", c.progress).put("goal", c.goal).put("title", c.title).put("runId", c.runId))
        } })
        put("favorites", JSONArray(s.favoriteTemplates.toList()))
        put("recent", JSONArray(s.recentTemplates))
        put("customExercises", JSONArray().apply { s.customExercises.forEach { e ->
            put(JSONObject().put("id", e.id).put("name", e.name).put("mode", e.mode)
                .put("muscle", e.muscle).put("equipment", e.equipment).put("technique", e.technique).put("movement", e.movement))
        } })
    }

    private fun decode(raw: String?): LifeState = runCatching {
        val o = JSONObject(raw ?: "{}")
        fun JSONArray?.objects(limit: Int = Int.MAX_VALUE): List<JSONObject> = this?.let { arr ->
            (0 until arr.length().coerceAtMost(limit)).mapNotNull(arr::optJSONObject)
        }.orEmpty()
        fun JSONArray?.strings(limit: Int = Int.MAX_VALUE): List<String> = this?.let { arr ->
            (0 until arr.length().coerceAtMost(limit)).map { arr.optString(it) }.filter { it.isNotBlank() }
        }.orEmpty()
        fun JSONObject.number(key: String): Float = optDouble(key, 0.0).toFloat().takeIf { it.isFinite() && it >= 0f } ?: 0f
        fun JSONObject.optional(key: String): Float? = if (isNull(key)) null else number(key).takeIf { it > 0f }
        LifeState(
            shoppingItems = o.optJSONArray("manualShopping").objects(2000).map { item ->
                ShoppingItem(item.optString("id").ifBlank { UUID.randomUUID().toString() }, item.optString("name").take(120),
                    item.optString("amount").take(60), item.optBoolean("checked"))
            },
            challengeRuns = o.optJSONArray("challengeRuns").objects(100).map { run ->
                ChallengeRun(id = run.optString("id").ifBlank { UUID.randomUUID().toString() }, kind = run.optString("kind"),
                    title = run.optString("title").take(80), metric = run.optString("metric"), start = run.optLong("start"),
                    days = run.optInt("days", 7).coerceIn(1, 90), goal = run.optInt("goal", 1).coerceIn(1, 1000000),
                    threshold = run.optInt("threshold").coerceIn(0, 100000), tone = run.optInt("tone").coerceIn(0, 2),
                    manual = run.optInt("manual").coerceAtLeast(0))
            },
            challengeFavorites = o.optJSONArray("challengeFavorites").strings(100).toSet(),
            challengesHidden = o.optBoolean("challengesHidden"),
            challengesCompact = o.optBoolean("challengesCompact"),
            recipes = o.optJSONArray("recipes").objects().map { r ->
                LeafRecipe(r.optString("id").ifBlank { UUID.randomUUID().toString() },
                    r.optString("name").take(120), r.number("cooked").coerceAtLeast(1f),
                    r.optJSONArray("ingredients").objects().map { i ->
                        RecipeIngredient(i.optString("name").take(120), i.number("grams"),
                            i.number("cal"), i.number("protein"), i.number("fat"), i.number("carb"))
                    })
            },
            meals = o.optJSONArray("meals").objects().map { m ->
                PlannedMeal(m.optString("id").ifBlank { UUID.randomUUID().toString() },
                    m.optLong("day"), m.optString("recipe"), m.number("grams"), m.optBoolean("prepared"))
            },
            shoppingChecked = o.optJSONArray("shopping").strings().toSet(),
            equipmentProfiles = o.optJSONArray("equipment").objects().map { p ->
                EquipmentProfile(p.optString("id").ifBlank { UUID.randomUUID().toString() },
                    p.optString("name").take(80), p.optJSONArray("items").strings(30).toSet())
            },
            activeEquipmentId = o.optString("activeEquipment").takeIf { it.isNotBlank() && it != "null" },
            progress = o.optJSONArray("progress").objects().map { p ->
                ProgressCheck(p.optString("id").ifBlank { UUID.randomUUID().toString() },
                    p.optLong("date"), p.optional("chest"), p.optional("waist"), p.optional("hips"),
                    p.optional("bicep"), p.optString("photo").takeIf { path ->
                        path.isNotBlank() && path != "null" &&
                            File(path).canonicalFile.parentFile == File(context.filesDir, "progress_photos").canonicalFile
                    }, p.optJSONObject("measurements")?.let { values ->
                        values.keys().asSequence().mapNotNull { key ->
                            values.optional(key)?.let { key.take(80) to it }
                        }.toMap()
                    }.orEmpty())
            },
            appointment = o.optJSONObject("appointment")?.let { a ->
                WorkoutAppointment(a.optLong("time"), a.optString("template"), a.optString("title"))
            },
            challengeId = o.optString("challenge").takeIf { it.isNotBlank() && it != "null" },
            challengeStart = o.optLong("challengeStart"),
            completedChallenges = o.optJSONArray("completedChallenges").objects().map { c ->
                FinishedChallenge(c.optString("kind"), c.optLong("week"), c.optLong("at"),
                    c.optInt("progress"), c.optInt("goal").coerceAtLeast(1), c.optString("title"), c.optString("runId"))
            },
            favoriteTemplates = o.optJSONArray("favorites").strings().toSet(),
            recentTemplates = o.optJSONArray("recent").strings(20),
            customExercises = o.optJSONArray("customExercises").objects().map { e ->
                UserExercise(e.optString("id").ifBlank { UUID.randomUUID().toString() },
                    e.optString("name").take(120), e.optString("mode"),
                    e.optString("muscle"), e.optString("equipment"),
                    e.optString("technique").take(500), e.optString("movement", "mobility"))
            }
        )
    }.getOrDefault(LifeState())

    companion object {
        @Volatile private var instance: LifeStore? = null
        fun get(context: Context): LifeStore = instance ?: synchronized(this) {
            instance ?: LifeStore(context.applicationContext).also { instance = it }
        }
    }
}
