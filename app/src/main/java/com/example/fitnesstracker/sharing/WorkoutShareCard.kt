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
internal enum class WrappedLayout { POSTER, JOURNAL, FOCUS }
internal enum class WrappedStyle { MINT, DAWN, LAVENDER, SKY, SAGE }
internal enum class WrappedHero { AUTO, DURATION, DISTANCE, VOLUME, STEPS }
internal enum class WrappedFormat(val height: Int) { AUTO(0), STORY(1920), POST(1350), SQUARE(1080) }
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

    fun render(context: Context, data: ShareWorkout, mode: RouteCardMode, map: Bitmap?, dark: Boolean,
        style: WrappedStyle, showStats: Boolean = true, showDate: Boolean = true, caption: String = "",
        layoutStyle: WrappedLayout = WrappedLayout.POSTER, showExercises: Boolean = true,
        showEnergy: Boolean = true, customTitle: String = "", outputWidth: Int = WIDTH, heroMetric: WrappedHero = WrappedHero.AUTO,
        insights: List<String> = emptyList(), transparent: Boolean = false): Bitmap {
        val ctx = LanguagePreferences.localizedContext(context)
        val locale = Locale.forLanguageTag(LanguagePreferences.get(ctx).code)
        val ink = Color.parseColor(if (dark) "#F1F2EA" else "#222B25")
        val muted = Color.parseColor(if (dark) "#C0C8BF" else "#535F56")
        val paper = Color.parseColor(if (dark) "#19211D" else "#F9FAF3")
        val tone = toneColor(style, dark)
        val accent = accentColor(style, dark)
        val accentInk = if (dark) paper else Color.WHITE
        val normal = ResourcesCompat.getFont(ctx, R.font.roboto_flex_400) ?: Typeface.DEFAULT
        val medium = ResourcesCompat.getFont(ctx, R.font.roboto_flex_600) ?: Typeface.DEFAULT
        val entries = data.plan?.exercises.orEmpty().filter { e -> e.sets.any { it.completed && it.hasResult(e.mode) } }
        val listed = if (showExercises && layoutStyle != WrappedLayout.FOCUS) entries.take(if (layoutStyle == WrappedLayout.JOURNAL) 6 else 3) else emptyList()
        val routeVisible = mode != RouteCardMode.HIDDEN && !data.route?.points.isNullOrEmpty() && layoutStyle != WrappedLayout.FOCUS
        val distance = data.workout.distanceMeters.takeIf { it.isFinite() && it > 0f } ?: 0f
        val seconds = data.workout.durationSeconds.coerceAtLeast(0)
        val heroChoice = when {
            heroMetric == WrappedHero.DURATION -> WrappedHero.DURATION
            heroMetric == WrappedHero.DISTANCE && distance > 0 -> WrappedHero.DISTANCE
            heroMetric == WrappedHero.VOLUME && (data.plan?.volumeKg ?: 0.0) > 0.0 -> WrappedHero.VOLUME
            heroMetric == WrappedHero.STEPS && data.workout.steps > 0 -> WrappedHero.STEPS
            distance > 0 -> WrappedHero.DISTANCE
            else -> WrappedHero.DURATION
        }
        val metrics = buildList<Pair<String, String>> {
            if (showStats) {
                if (heroChoice != WrappedHero.DURATION) add(DateUtils.formatDuration(seconds) to ctx.getString(R.string.share_duration))
                if (distance > 0f) {
                    if (heroChoice != WrappedHero.DISTANCE) add(String.format(locale, "%.2f", distance / 1000f) to ctx.getString(R.string.share_km))
                    if (seconds > 0) add(DateUtils.formatDuration((seconds * 1000.0 / distance).roundToLong()) to
                        (ctx.getString(R.string.share_pace) + " · " + ctx.getString(R.string.share_pace_unit)))
                }
                data.plan?.let { plan ->
                    if (plan.completedCount > 0) add(plan.completedCount.toString() to ctx.getString(R.string.share_sets))
                    if (entries.isNotEmpty()) add(entries.size.toString() to ctx.getString(R.string.share_exercises))
                    if (plan.volumeKg > 0 && heroChoice != WrappedHero.VOLUME) add(String.format(locale, "%.0f", plan.volumeKg) to ctx.getString(R.string.workout_result_volume))
                }
            }
            if (showEnergy) {
                if (data.workout.steps > 0 && heroChoice != WrappedHero.STEPS) add(data.workout.steps.toString() to ctx.getString(R.string.unit_steps_short))
                if (data.workout.caloriesBurned > 0) add("≈${data.workout.caloriesBurned.toInt()}" to ctx.getString(R.string.unit_kcal))
            }
        }.take(6)
        val title = customTitle.trim().take(120).ifBlank {
            data.plan?.title?.takeIf { it.isNotBlank() } ?: data.route?.title?.takeIf { it.isNotBlank() }
            ?: ctx.getString(WorkoutCatalog.forType(data.workout.type).first().labelRes)
        }
        fun paragraph(value: String, size: Float, width: Int, color: Int, lines: Int = 2): StaticLayout {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; textSize = size; typeface = medium }
            return StaticLayout.Builder.obtain(value, 0, value.length, p, width).setIncludePad(false)
                .setMaxLines(lines).setEllipsize(TextUtils.TruncateAt.END).build()
        }
        val titleBlock = paragraph(title, 64f, 920, ink)
        val captionBlock = paragraph(caption.trim().take(70).ifBlank { ctx.getString(R.string.share_card_footer) }, 34f, 840, ink)
        val heroHeight = if (layoutStyle == WrappedLayout.FOCUS) 360f else 248f
        val pillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = medium; textSize = 30f }
        val pillRows = mutableListOf<MutableList<Pair<String, Float>>>()
        insights.take(4).forEach { label ->
            val width = (pillPaint.measureText(label) + 96f).coerceAtMost(968f)
            val row = pillRows.lastOrNull()
            if (row == null || row.sumOf { it.second.toDouble() } + row.size * 14f + width > 968f) pillRows += mutableListOf(label to width)
            else row += label to width
        }
        val insightsHeight = if (pillRows.isEmpty()) 0f else 52f + pillRows.size * 90f + 10f
        val listHeight = if (listed.isNotEmpty()) 88f + listed.size * 100f + (if (entries.size > listed.size) 44f else 0f) else 0f
        val statsHeight = ((metrics.size + 1) / 2) * 166f + if (metrics.isNotEmpty()) 52f else 0f
        val contentTop = 182f + titleBlock.height + (if (showDate) 54f else 0f)
        val routeHeight = if (routeVisible) 484f else 0f
        val logicalHeight = (contentTop + heroHeight + 24f + insightsHeight + routeHeight + listHeight + statsHeight + captionBlock.height + 168f).toInt()
        val bitmap = Bitmap.createBitmap(outputWidth, (logicalHeight * outputWidth.toFloat() / WIDTH).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { scale(outputWidth.toFloat() / WIDTH, outputWidth.toFloat() / WIDTH) }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
        fun round(rect: RectF, color: Int, radius: Float = 40f) {
            paint.style = Paint.Style.FILL; paint.color = color; canvas.drawRoundRect(rect, radius, radius, paint)
        }
        fun text(value: String, x: Float, baseline: Float, size: Float, color: Int = ink, width: Float = 900f) {
            paint.style = Paint.Style.FILL; paint.typeface = medium; paint.color = color; paint.textSize = size
            while (paint.measureText(value) > width && paint.textSize > 20f) paint.textSize -= 1f
            val fitted = TextUtils.ellipsize(value, TextPaint(paint), width, TextUtils.TruncateAt.END).toString()
            canvas.drawText(fitted, x, baseline, paint)
        }
        fun block(layout: StaticLayout, x: Float, y: Float) {
            canvas.save(); canvas.translate(x, y); layout.draw(canvas); canvas.restore()
        }
        fun flower(cx: Float, cy: Float, radius: Float, color: Int) {
            val path = Path()
            for (i in 0..240) {
                val angle = i * 2 * Math.PI / 240
                val r = radius * (.94 + .06 * cos(8 * angle))
                val x = cx + (r * cos(angle)).toFloat(); val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close(); paint.style = Paint.Style.FILL; paint.color = color; canvas.drawPath(path, paint)
        }
        if (!transparent) canvas.drawColor(tone)
        round(RectF(24f, 24f, 1056f, logicalHeight - 24f), paper, 72f)
        canvas.save()
        canvas.clipRect(24f, 24f, 1056f, 170f)
        flower(980f, 54f, 112f, tone)
        canvas.restore()
        flower(104f, 104f, 34f, accent)
        text("Leafit Wrapped", 162f, 118f, 40f)
        text("✦", 946f, 114f, 32f, accent, 50f)
        var y = 178f
        block(titleBlock, 80f, y); y += titleBlock.height + 16f
        if (showDate) {
            text(DateFormat.getDateInstance(DateFormat.LONG, locale).format(Date(data.workout.startTime)), 80f, y + 25f, 28f, muted)
            y += 54f
        }
        val hero = RectF(56f, y, 1024f, y + heroHeight)
        round(hero, accent, if (layoutStyle == WrappedLayout.FOCUS) 120f else 56f)
        val main = when (heroChoice) {
            WrappedHero.DISTANCE -> String.format(locale, "%.2f", distance / 1000f)
            WrappedHero.VOLUME -> String.format(locale, "%.0f", data.plan?.volumeKg ?: 0.0)
            WrappedHero.STEPS -> data.workout.steps.toString()
            else -> DateUtils.formatDuration(seconds)
        }
        val mainLabel = ctx.getString(when (heroChoice) {
            WrappedHero.DISTANCE -> R.string.share_km
            WrappedHero.VOLUME -> R.string.workout_result_volume
            WrappedHero.STEPS -> R.string.unit_steps_short
            else -> R.string.share_duration
        })
        flower(924f, y + heroHeight / 2, if (layoutStyle == WrappedLayout.FOCUS) 70f else 54f, tone)
        text("✦", 903f, y + heroHeight / 2 + 16f, 48f, accent, 62f)
        text(main, 96f, y + if (layoutStyle == WrappedLayout.FOCUS) 190f else 134f,
            if (layoutStyle == WrappedLayout.FOCUS) 154f else 120f, accentInk, 710f)
        text(mainLabel, 100f, y + heroHeight - 48f, 32f, accentInk, 710f)
        y += heroHeight + 24f
        if (pillRows.isNotEmpty()) {
            text(ctx.getString(R.string.b9_wrap_context), 80f, y + 30f, 28f, accent)
            y += 52f
            pillRows.forEach { row ->
                var x = 56f
                row.forEachIndexed { index, (label, width) ->
                    val rect = RectF(x, y, x + width, y + 76f)
                    round(rect, if (index == 0 && row === pillRows.first()) accent else tone, 38f)
                    val first = index == 0 && row === pillRows.first()
                    flower(x + 40f, y + 38f, 16f, if (first) accentInk else accent)
                    text(label, x + 70f, y + 49f, 30f, if (first) accentInk else ink, width - 92f)
                    x += width + 14f
                }
                y += 90f
            }
            y += 10f
        }
        if (routeVisible) {
            val rect = RectF(56f, y, 1024f, y + 460f)
            round(rect, tone, 48f)
            if (mode == RouteCardMode.MAP && map != null) {
                canvas.save(); canvas.clipPath(Path().apply { addRoundRect(rect, 48f, 48f, Path.Direction.CW) })
                paint.style = Paint.Style.FILL; paint.color = Color.WHITE
                canvas.drawBitmap(map, null, rect, paint); canvas.restore()
                round(RectF(80f, y + 402f, 1000f, y + 446f), paper, 22f)
                text("© OpenStreetMap contributors · openstreetmap.org/copyright", 102f, y + 432f, 22f, muted, 876f)
            } else {
                drawRoute(canvas, data.route!!, RectF(116f, y + 44f, 964f, y + 364f), accent, paper)
                text(ctx.getString(R.string.share_route_diagram), 96f, y + 420f, 26f, muted)
            }
            y += routeHeight
        }
        if (listed.isNotEmpty()) {
            round(RectF(56f, y, 1024f, y + listHeight - 24f), tone, 40f)
            text(ctx.getString(R.string.workout_result_exercises), 96f, y + 48f, 28f, accent)
            listed.forEachIndexed { index, entry ->
                val row = y + 90f + index * 100f
                val sets = entry.sets.filter { it.completed && it.hasResult(entry.mode) }
                text(String.format(locale, "%02d", index + 1), 96f, row + 24f, 34f, accent, 72f)
                text(entry.name, 190f, row + 22f, 34f, ink, 754f)
                val result = when (entry.mode) {
                    com.example.fitnesstracker.workout.EntryMode.REPS -> listOfNotNull(
                        sets.sumOf { it.reps ?: 0 }.toString() + " " + ctx.getString(R.string.journal_reps_unit),
                        sets.maxOf { it.weightKg ?: 0f }.takeIf { it > 0 }?.let { String.format(locale, "%.1f", it) + " " + ctx.getString(R.string.unit_kg) }
                    ).joinToString(" · ")
                    com.example.fitnesstracker.workout.EntryMode.TIME -> DateUtils.formatDuration(sets.sumOf { (it.seconds ?: 0).toLong() })
                    com.example.fitnesstracker.workout.EntryMode.DISTANCE -> String.format(locale, "%.2f", sets.sumOf { (it.distanceMeters ?: 0f).toDouble() } / 1000) + " " + ctx.getString(R.string.live_km)
                    com.example.fitnesstracker.workout.EntryMode.SCORE -> sets.maxOf { it.score ?: 0 }.toString() + " " + ctx.getString(R.string.journal_score)
                }
                text("${sets.size} " + ctx.getString(R.string.share_sets) + " · " + result, 190f, row + 60f, 24f, muted, 754f)
                round(RectF(190f, row + 74f, 944f, row + 80f), paper, 3f)
                round(RectF(190f, row + 74f, 190f + 754f * sets.size / entry.sets.size.coerceAtLeast(1), row + 80f), accent, 3f)
            }
            if (entries.size > listed.size) text("+${entries.size - listed.size}", 96f, y + listHeight - 44f, 26f, muted)
            y += listHeight
        }
        if (metrics.isNotEmpty()) {
            text(ctx.getString(R.string.b5_wrapped_details), 80f, y + 30f, 28f, accent)
            y += 52f
        }
        metrics.forEachIndexed { index, metric ->
            val x = if (index % 2 == 0) 56f else 552f
            val rowY = y + index / 2 * 166f
            val right = if (index == metrics.lastIndex && metrics.size % 2 == 1) 1024f else x + 472f
            round(RectF(x, rowY, right, rowY + 142f), tone, 32f)
            text(metric.first, x + 28f, rowY + 65f, 52f, ink, right - x - 56f)
            text(metric.second, x + 28f, rowY + 111f, 25f, muted, right - x - 56f)
        }
        y += ((metrics.size + 1) / 2) * 166f
        block(captionBlock, 96f, y + 18f)
        text("LEAFIT  /  " + ctx.getString(R.string.share_session_saved), 96f, y + captionBlock.height + 92f, 22f, accent, 888f)
        return bitmap
    }

    fun toneColor(style: WrappedStyle, dark: Boolean): Int = Color.parseColor(when (style) {
        WrappedStyle.MINT -> if (dark) "#304F40" else "#D3ECD9"
        WrappedStyle.DAWN -> if (dark) "#593F30" else "#FFDBC5"
        WrappedStyle.LAVENDER -> if (dark) "#493D60" else "#E9DDFE"
        WrappedStyle.SKY -> if (dark) "#294B59" else "#D6EAF5"
        WrappedStyle.SAGE -> if (dark) "#424D32" else "#E4EDCD"
    })

    fun accentColor(style: WrappedStyle, dark: Boolean): Int = Color.parseColor(when (style) {
        WrappedStyle.MINT -> if (dark) "#B5E7C3" else "#28583F"
        WrappedStyle.DAWN -> if (dark) "#FFD0A8" else "#7D4527"
        WrappedStyle.LAVENDER -> if (dark) "#D9C5FF" else "#61478A"
        WrappedStyle.SKY -> if (dark) "#B1DEEF" else "#315F73"
        WrappedStyle.SAGE -> if (dark) "#D1E5AC" else "#4D6235"
    })

    fun frame(card: Bitmap, format: WrappedFormat, background: Int?): Bitmap {
        if (format == WrappedFormat.AUTO) return card
        val width = card.width
        val height = format.height * width / WIDTH
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        if (background != null) canvas.drawColor(background)
        val scale = min(1f, height.toFloat() / card.height)
        val w = card.width * scale; val h = card.height * scale
        val left = (width - w) / 2f; val top = (height - h) / 2f
        canvas.drawBitmap(card, null, RectF(left, top, left + w, top + h), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        return out
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
