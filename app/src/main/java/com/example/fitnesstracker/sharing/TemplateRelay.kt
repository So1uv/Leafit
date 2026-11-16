package com.example.fitnesstracker.sharing

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.fitnesstracker.workout.WorkoutJournal
import com.example.fitnesstracker.workout.WorkoutTemplate
import com.example.fitnesstracker.workout.equipmentTags
import org.json.JSONObject
import java.io.File

object TemplateRelay {
    fun share(context: Context, template: WorkoutTemplate) {
        val folder = File(context.cacheDir, "workout_cards").apply { mkdirs() }
        val file = File(folder, "leafit-relay-${template.id.take(20).replace(Regex("[^a-zA-Z0-9-]"), "")}.json")
        file.writeText(JSONObject().put("format", "leafit-relay-1").put("name", template.name)
            .put("equipment", org.json.JSONArray(template.equipment.ifEmpty { template.plan.equipmentTags(context) }))
            .put("plan", WorkoutJournal.toJson(template.plan)).toString())
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.workout.cards", file)
        val send = Intent(Intent.ACTION_SEND).setType("application/json")
            .putExtra(Intent.EXTRA_STREAM, uri).apply {
                clipData = ClipData.newUri(context.contentResolver, "Leafit Relay", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(send, "Leafit Relay"))
    }

    fun read(context: Context, uri: Uri): WorkoutTemplate? {
        return runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    if (output.size() + count > 512_000) return null
                    output.write(buffer, 0, count)
                }
                output.toString("UTF-8")
            } ?: return null
            val json = JSONObject(raw)
            if (json.optString("format") != "leafit-relay-1") return null
            val plan = WorkoutJournal.fromJson(json.getJSONObject("plan"))
            if (plan.exercises.isEmpty() || plan.exercises.any { it.name.isBlank() || it.sets.isEmpty() }) return null
            val tags = json.optJSONArray("equipment")
            val equipment = if (tags == null) emptyList() else (0 until tags.length().coerceAtMost(30))
                .map { tags.optString(it) }.filter { it in com.example.fitnesstracker.workout.ExerciseLibrary.equipment }.distinct()
            WorkoutTemplate(equipment = equipment, name = json.optString("name").take(120).ifBlank { "Leafit Relay" }, plan = plan.fresh())
        }.getOrNull()
    }
}
