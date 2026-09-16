package com.example.fitnesstracker.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.fitnesstracker.data.database.AppDatabase
import com.example.fitnesstracker.data.entities.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    const val SNAPSHOT_VERSION = 1

    suspend fun exportJson(context: Context, uri: Uri): Boolean = try {
        val json = buildSnapshot(context)
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        true
    } catch (e: Exception) { false }

    suspend fun buildSnapshot(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val root = JSONObject()
        root.put("snapshotVersion", SNAPSHOT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        db.userProfileDao().getProfile().first()?.let { p ->
            root.put("profile", JSONObject().apply {
                put("name", p.name); put("gender", p.gender); put("age", p.age)
                put("heightCm", p.heightCm.toDouble()); put("weightKg", p.weightKg.toDouble())
                p.chestCm?.let { put("chestCm", it.toDouble()) }
                p.waistCm?.let { put("waistCm", it.toDouble()) }
                p.hipsCm?.let { put("hipsCm", it.toDouble()) }
                p.bicepCm?.let { put("bicepCm", it.toDouble()) }
                p.avatarUri?.let { path ->
                    runCatching {
                        val f = File(path)
                        if (f.exists()) put("avatarBase64", Base64.encodeToString(f.readBytes(), Base64.NO_WRAP))
                    }
                }
            })
        }

        root.put("workouts", JSONArray().apply {
            db.workoutDao().getAll().first().forEach { w ->
                put(JSONObject().apply {
                    put("startTime", w.startTime); put("endTime", w.endTime)
                    put("durationSeconds", w.durationSeconds); put("steps", w.steps); put("distanceMeters", w.distanceMeters.toDouble())
                    put("caloriesBurned", w.caloriesBurned.toDouble())
                    put("note", w.note); put("type", w.type); put("metrics", w.metrics)
                })
            }
        })
        root.put("meals", JSONArray().apply {
            db.mealDao().getAllMealsOnce().forEach { m ->
                put(JSONObject().apply {
                    put("date", m.date); put("mealType", m.mealType); put("name", m.name)
                    put("calories", m.calories.toDouble()); put("proteins", m.proteins.toDouble())
                    put("fats", m.fats.toDouble()); put("carbs", m.carbs.toDouble())
                })
            }
        })
        root.put("savedMeals", JSONArray().apply {
            db.mealDao().getSavedMeals().first().forEach { m ->
                put(JSONObject().apply {
                    put("name", m.name); put("calories", m.calories.toDouble())
                    put("proteins", m.proteins.toDouble()); put("fats", m.fats.toDouble()); put("carbs", m.carbs.toDouble())
                })
            }
        })
        root.put("sleep", JSONArray().apply {
            db.sleepDao().getAll().first().forEach { r ->
                put(JSONObject().apply {
                    put("bedTime", r.bedTime); put("wakeTime", r.wakeTime)
                    put("qualityScore", r.qualityScore); put("tags", r.tags)
                })
            }
        })
        root.put("steps", JSONArray().apply {
            db.stepDao().getAll().first().forEach { r ->
                put(JSONObject().apply { put("dayStart", r.dayStart); put("steps", r.steps) })
            }
        })
        root.put("water", JSONArray().apply {
            db.waterDao().getAllWaterOnce().forEach { r ->
                put(JSONObject().apply { put("timestamp", r.timestamp); put("amountMl", r.amountMl) })
            }
        })
        root.put("weights", JSONArray().apply {
            db.extrasDao().getWeightHistory().first().forEach { r ->
                put(JSONObject().apply { put("date", r.date); put("weightKg", r.weightKg.toDouble()) })
            }
        })
        root.put("notes", JSONArray().apply {
            db.extrasDao().getAllNotesOnce().forEach { n ->
                put(JSONObject().apply { put("dayStart", n.dayStart); put("text", n.text); put("isPinned", n.isPinned) })
            }
        })
        root.put("products", JSONArray().apply {
            db.extrasDao().getProducts().first().forEach { p ->
                put(JSONObject().apply {
                    put("name", p.name); put("caloriesPer100", p.caloriesPer100.toDouble())
                    put("proteinsPer100", p.proteinsPer100.toDouble())
                    put("fatsPer100", p.fatsPer100.toDouble()); put("carbsPer100", p.carbsPer100.toDouble())
                })
            }
        })
        return root.toString(2)
    }

    suspend fun importJson(context: Context, uri: Uri, replaceProfile: Boolean = false): Boolean = try {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return false
        applySnapshot(context, text, replaceProfile)
    } catch (e: Exception) { false }

    fun isValidSnapshot(text: String): Boolean = try {
        val o = JSONObject(text)
        o.has("snapshotVersion") && (o.has("profile") || o.has("workouts") || o.has("meals"))
    } catch (e: Exception) { false }

    suspend fun applySnapshot(context: Context, text: String, replaceProfile: Boolean): Boolean = try {
        val root = JSONObject(text)
        val db = AppDatabase.getInstance(context)

        if (replaceProfile) {
            root.optJSONObject("profile")?.let { o ->
                var avatarPath: String? = null
                o.optString("avatarBase64").takeIf { it.isNotBlank() }?.let { b64 ->
                    runCatching {
                        val bytes = Base64.decode(b64, Base64.NO_WRAP)
                        val f = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
                        f.writeBytes(bytes); avatarPath = f.absolutePath
                    }
                }
                db.userProfileDao().upsert(UserProfile(
                    id = 1,
                    name = o.optString("name"), gender = o.optString("gender", "other"),
                    age = o.optInt("age"), heightCm = o.optDouble("heightCm", 170.0).toFloat(),
                    weightKg = o.optDouble("weightKg", 70.0).toFloat(),
                    avatarUri = avatarPath,
                    chestCm = if (o.has("chestCm")) o.getDouble("chestCm").toFloat() else null,
                    waistCm = if (o.has("waistCm")) o.getDouble("waistCm").toFloat() else null,
                    hipsCm = if (o.has("hipsCm")) o.getDouble("hipsCm").toFloat() else null,
                    bicepCm = if (o.has("bicepCm")) o.getDouble("bicepCm").toFloat() else null
                ))
            }
        }

        root.optJSONArray("workouts")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.workoutDao().insert(Workout(
                    startTime = o.getLong("startTime"), endTime = o.getLong("endTime"),
                    durationSeconds = o.getLong("durationSeconds"), steps = o.optInt("steps"),
                    distanceMeters = o.optDouble("distanceMeters", 0.0).toFloat(),
                    caloriesBurned = o.optDouble("caloriesBurned", 0.0).toFloat(),
                    note = o.optString("note"), type = o.optString("type", "general"), metrics = o.optString("metrics")
                )) }
        }
        root.optJSONArray("meals")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.mealDao().insert(Meal(
                    date = o.getLong("date"), mealType = o.optString("mealType","breakfast"), name = o.optString("name"),
                    calories = o.optDouble("calories",0.0).toFloat(), proteins = o.optDouble("proteins",0.0).toFloat(),
                    fats = o.optDouble("fats",0.0).toFloat(), carbs = o.optDouble("carbs",0.0).toFloat()
                )) }
        }
        root.optJSONArray("savedMeals")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.mealDao().insertSaved(SavedMeal(
                    name = o.getString("name"), calories = o.optDouble("calories",0.0).toFloat(),
                    proteins = o.optDouble("proteins",0.0).toFloat(), fats = o.optDouble("fats",0.0).toFloat(),
                    carbs = o.optDouble("carbs",0.0).toFloat()
                )) }
        }
        root.optJSONArray("sleep")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.sleepDao().insert(SleepRecord(
                    bedTime = o.getLong("bedTime"), wakeTime = o.getLong("wakeTime"),
                    qualityScore = o.optInt("qualityScore",3), tags = o.optString("tags")
                )) }
        }
        root.optJSONArray("steps")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.stepDao().upsert(StepRecord(dayStart = o.getLong("dayStart"), steps = o.optInt("steps"))) }
        }
        root.optJSONArray("water")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.waterDao().insert(WaterRecord(timestamp = o.getLong("timestamp"), amountMl = o.optInt("amountMl",250))) }
        }
        root.optJSONArray("weights")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.extrasDao().insertWeight(WeightRecord(date = o.getLong("date"), weightKg = o.optDouble("weightKg",0.0).toFloat())) }
        }
        root.optJSONArray("notes")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.extrasDao().upsertNote(DayNote(dayStart = o.getLong("dayStart"), text = o.optString("text"), isPinned = o.optBoolean("isPinned", false))) }
        }
        root.optJSONArray("products")?.let { arr ->
            for (i in 0 until arr.length()) { val o = arr.getJSONObject(i)
                db.extrasDao().insertProduct(FoodProduct(
                    name = o.getString("name"), caloriesPer100 = o.optDouble("caloriesPer100",0.0).toFloat(),
                    proteinsPer100 = o.optDouble("proteinsPer100",0.0).toFloat(),
                    fatsPer100 = o.optDouble("fatsPer100",0.0).toFloat(), carbsPer100 = o.optDouble("carbsPer100",0.0).toFloat()
                )) }
        }
        true
    } catch (e: Exception) { false }
}
