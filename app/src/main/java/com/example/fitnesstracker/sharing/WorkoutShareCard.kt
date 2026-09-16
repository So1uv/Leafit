package com.example.fitnesstracker.sharing

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import com.example.fitnesstracker.R
import com.example.fitnesstracker.data.entities.Workout
import com.example.fitnesstracker.tracking.LiveSession
import com.example.fitnesstracker.tracking.LiveWorkoutStore
import com.example.fitnesstracker.utils.DateUtils
import com.example.fitnesstracker.utils.LanguagePreferences
import com.example.fitnesstracker.workout.WorkoutCatalog
import com.example.fitnesstracker.workout.WorkoutJournal
import com.example.fitnesstracker.workout.WorkoutPlan
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

internal enum class RouteCardMode { MAP, ROUTE, HIDDEN }
internal data class ShareWorkout(val workout: Workout, val plan: WorkoutPlan?, val route: LiveSession?) {
    companion object {
        fun from(workout: Workout) = ShareWorkout(workout, WorkoutJournal.decode(workout.metrics), LiveWorkoutStore.fromMetrics(workout.metrics))
    }
}

internal object WorkoutShareCard {
    const val WIDTH = 1080
    const val HEIGHT = 1920
    const val MAP_WIDTH = 936f
    const val MAP_HEIGHT = 724f

    fun render(context: Context, data: ShareWorkout, mode: RouteCardMode, map: Bitmap?, dark: Boolean): Bitmap {
        val ctx = LanguagePreferences.localizedContext(context)
        val locale = Locale.forLanguageTag(LanguagePreferences.get(ctx).code)
        val ink = Color.parseColor(if (dark) "#E0EBE2" else "#203E2B")
        val muted = Color.parseColor(if (dark) "#B3C4B8" else "#58665E")
        val paper = Color.parseColor(if (dark) "#18251F" else "#FAFDF8")
        val tone = Color.parseColor(if (dark) "#314F41" else "#D2EADF")
        val accent = Color.parseColor(if (dark) "#A0CEB7" else "#376B57")
        val lavender = Color.parseColor(if (dark) "#494052" else "#E9E1EF")
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        val normal = ResourcesCompat.getFont(ctx, R.font.roboto_flex_400) ?: Typeface.DEFAULT
        val medium = ResourcesCompat.getFont(ctx, R.font.roboto_flex_600) ?: Typeface.DEFAULT
        fun round(rect: RectF, color: Int, radius: Float = 44f) {
            paint.style = Paint.Style.FILL; paint.color = color; canvas.drawRoundRect(rect, radius, radius, paint)
        }
        fun text(value: String, x: Float, y: Float, size: Float, color: Int = ink, bold: Boolean = false, width: Float = 896f) {
            paint.style = Paint.Style.FILL; paint.typeface = if (bold) medium else normal; paint.color = color; paint.textSize = size
            while (paint.measureText(value) > width && paint.textSize > 24f) paint.textSize -= 2f
            canvas.drawText(value, x, y, paint)
        }
        fun rosette(cx: Float, cy: Float, radius: Float, color: Int) {
            val path = Path()
            for (i in 0..240) {
                val a = i * 2 * Math.PI / 240
                val r = radius * (.94 + .06 * cos(8 * a))
                val x = cx + (r * cos(a)).toFloat(); val y = cy + (r * sin(a)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close(); paint.color = color; paint.style = Paint.Style.FILL; canvas.drawPath(path, paint)
        }
        fun exercise(cx: Float, cy: Float, size: Float, color: Int) {
            ctx.getDrawable(R.drawable.widget_exercise)?.mutate()?.apply {
                setTint(color); setBounds((cx - size / 2).toInt(), (cy - size / 2).toInt(), (cx + size / 2).toInt(), (cy + size / 2).toInt()); draw(canvas)
            }
        }
        canvas.drawColor(if (dark) Color.parseColor("#34483A") else Color.parseColor("#DCE8D7"))
        round(RectF(24f, 24f, 1056f, 1896f), paper, 80f)
        rosette(126f, 132f, 46f, tone); exercise(126f, 132f, 48f, accent)
        text("Leafit", 194f, 148f, 46f, bold = true)
        text(ctx.getString(R.string.share_card_brand), 640f, 144f, 24f, muted, width = 352f)
        val date = DateFormat.getDateInstance(DateFormat.LONG, locale).format(Date(data.workout.startTime))
        text(date, 92f, 238f, 30f, muted)
        val title = data.plan?.title?.takeIf { it.isNotBlank() } ?: data.route?.title?.takeIf { it.isNotBlank() }
            ?: ctx.getString(WorkoutCatalog.forType(data.workout.type).first().labelRes)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; textSize = 64f; typeface = medium }
        val layout = StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, 896)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false).setMaxLines(2).setEllipsize(TextUtils.TruncateAt.END).build()
        canvas.save(); canvas.translate(92f, 280f); layout.draw(canvas); canvas.restore()
        val distance = data.workout.distanceMeters.takeIf { it.isFinite() && it > 0f } ?: 0f
        val seconds = data.workout.durationSeconds.coerceAtLeast(0)
        val distanceWorkout = distance > 0
        text(if (distanceWorkout) String.format(locale, "%.2f", distance / 1000) else DateUtils.formatDuration(seconds),
            92f, 562f, if (distanceWorkout) 130f else 106f, bold = true)
        text(ctx.getString(if (distanceWorkout) R.string.share_km else R.string.share_duration), 92f, 610f, 28f, muted)
        val mapRect = RectF(72f, 632f, 1008f, 1356f)
        round(mapRect, tone, 52f)
        if (mode == RouteCardMode.MAP && map != null) {
            val clip = Path().apply { addRoundRect(mapRect, 52f, 52f, Path.Direction.CW) }
            canvas.save(); canvas.clipPath(clip)
            paint.color = Color.WHITE; paint.style = Paint.Style.FILL
            canvas.drawBitmap(map, null, mapRect, paint)
            canvas.restore()
            round(RectF(240f, 1296f, 984f, 1336f), paper, 20f)
            text("© OpenStreetMap contributors · openstreetmap.org/copyright", 262f, 1324f, 20f, muted, width = 700f)
        } else if (mode == RouteCardMode.ROUTE && !data.route?.points.isNullOrEmpty()) {
            drawRoute(canvas, data.route!!, RectF(132f, 692f, 948f, 1236f), accent, paper)
            text(ctx.getString(R.string.share_route_diagram), 112f, 1310f, 26f, muted)
        } else {
            rosette(540f, 952f, 206f, lavender)
            exercise(540f, 952f, 188f, ink)
            text(ctx.getString(R.string.share_session_saved), 160f, 1258f, 42f, bold = true, width = 760f)
        }
        val metrics = if (distanceWorkout) {
            val pace = if (seconds > 0) DateUtils.formatDuration((seconds * 1000.0 / distance).roundToLong()) else "—"
            val speed = if (seconds > 0) String.format(locale, "%.1f", distance * 3.6 / seconds) else "—"
            listOf(Triple(DateUtils.formatDuration(seconds), R.string.share_duration, ""), Triple(pace, R.string.share_pace, ctx.getString(R.string.share_pace_unit)),
                Triple(speed, R.string.share_speed, ctx.getString(R.string.share_speed_unit)))
        } else listOf(Triple((data.plan?.completedCount?.toString() ?: "—"), R.string.share_sets, ""),
            Triple((data.plan?.exercises?.count { e -> e.sets.any { it.completed } }?.toString() ?: "—"), R.string.share_exercises, ""))
        val cellWidth = (936f - 16f * (metrics.size - 1)) / metrics.size
        metrics.forEachIndexed { i, metric ->
            val x = 72f + i * (cellWidth + 16f)
            round(RectF(x, 1404f, x + cellWidth, 1632f), if (i == 1) lavender else tone, 36f)
            text(metric.first, x + 22f, 1482f, 48f, bold = true, width = cellWidth - 44)
            text(ctx.getString(metric.second), x + 22f, 1540f, 27f, muted, width = cellWidth - 44)
            if (metric.third.isNotBlank()) text(metric.third, x + 22f, 1585f, 23f, muted, width = cellWidth - 44)
        }
        round(RectF(72f, 1680f, 1008f, 1784f), lavender, 52f)
        text(ctx.getString(R.string.share_card_footer), 112f, 1745f, 32f, ink, width = 856f)
        text("Leafit · 2.0.0", 92f, 1842f, 24f, muted)
        return bitmap
    }

    private fun drawRoute(canvas: Canvas, session: LiveSession, rect: RectF, color: Int, outline: Int) {
        val points = session.points
        if (points.isEmpty()) return
        val projected = ArrayList<Pair<Double, Double>>(points.size)
        var previousLon = points.first().lon
        points.forEach { p ->
            val lon = previousLon + ((p.lon - previousLon + 540) % 360 - 180)
            previousLon = lon
            val lat = Math.toRadians(p.lat.coerceIn(-85.0, 85.0))
            projected += Math.toRadians(lon) to -ln(tan(Math.PI / 4 + lat / 2))
        }
        val minX = projected.minOf { it.first }; val maxX = projected.maxOf { it.first }
        val minY = projected.minOf { it.second }; val maxY = projected.maxOf { it.second }
        val scale = min(rect.width() / (maxX - minX).coerceAtLeast(1e-7), rect.height() / (maxY - minY).coerceAtLeast(1e-7))
        fun at(i: Int) = PointF((rect.centerX() + (projected[i].first - (maxX + minX) / 2) * scale).toFloat(),
            (rect.centerY() + (projected[i].second - (maxY + minY) / 2) * scale).toFloat())
        val path = Path()
        points.indices.forEach { i ->
            val p = at(i)
            if (i == 0 || points[i].segment != points[i - 1].segment) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; this.color = color; strokeWidth = 14f }
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        val start = at(0); val end = at(points.lastIndex)
        paint.color = outline; canvas.drawCircle(start.x, start.y, 18f, paint); canvas.drawCircle(end.x, end.y, 22f, paint)
        paint.color = color; canvas.drawCircle(start.x, start.y, 10f, paint); canvas.drawCircle(end.x, end.y, 14f, paint)
    }
}
