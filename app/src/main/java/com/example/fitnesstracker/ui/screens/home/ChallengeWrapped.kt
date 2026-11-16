package com.example.fitnesstracker.ui.screens.home

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.fitnesstracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal object ChallengeWrapped {
    suspend fun share(context: Context, name: String, progress: Int, goal: Int, detail: String, week: Long) {
        val file = withContext(Dispatchers.IO) {
            val bitmap = Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
            try {
                val canvas = Canvas(bitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                val green = Color.rgb(44, 87, 68)
                val mint = Color.rgb(215, 239, 224)
                val lavender = Color.rgb(232, 220, 245)
                canvas.drawColor(green)
                fun panel(rect: RectF, color: Int, radius: Float = 56f) {
                    paint.style = Paint.Style.FILL; paint.color = color
                    canvas.drawRoundRect(rect, radius, radius, paint)
                }
                val font = ResourcesCompat.getFont(context, R.font.roboto_flex_600)
                fun line(value: String, x: Float, y: Float, size: Float, color: Int, width: Float = 840f) {
                    paint.typeface = font; paint.textSize = size; paint.color = color; paint.style = Paint.Style.FILL
                    while (paint.measureText(value) > width && paint.textSize > 18f) paint.textSize -= 1f
                    canvas.drawText(value, x, y, paint)
                }
                fun paragraph(value: String, x: Float, y: Float, size: Float, width: Int, color: Int, lines: Int = 3) {
                    val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = font; textSize = size; this.color = color }
                    val block = StaticLayout.Builder.obtain(value, 0, value.length, text, width)
                        .setIncludePad(false).setMaxLines(lines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
                    canvas.save(); canvas.translate(x, y); block.draw(canvas); canvas.restore()
                }
                line("Leafit Wrapped", 76f, 112f, 42f, mint)
                line(com.example.fitnesstracker.utils.DateUtils.formatDate(week), 76f, 170f, 28f, mint)
                panel(RectF(40f, 218f, 1040f, 1350f), mint, 76f)
                paragraph(name, 96f, 278f, 64f, 876, green, 2)
                val path = Path()
                for (i in 0..240) {
                    val angle = i * 2 * Math.PI / 240
                    val radius = 240 * (1 + .065 * kotlin.math.cos(10 * angle))
                    val x = 540f + (radius * kotlin.math.cos(angle)).toFloat()
                    val y = 724f + (radius * kotlin.math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close(); paint.color = lavender; canvas.drawPath(path, paint)
                val share = (progress.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
                val ring = RectF(540f - 300f, 724f - 300f, 540f + 300f, 724f + 300f)
                paint.style = Paint.Style.STROKE; paint.strokeWidth = 30f; paint.strokeCap = Paint.Cap.ROUND
                paint.color = androidx.core.graphics.ColorUtils.setAlphaComponent(green, 40); canvas.drawOval(ring, paint)
                paint.color = green
                if (share >= .999f) canvas.drawOval(ring, paint) else if (share > 0f) canvas.drawArc(ring, -90f, 360f * share, false, paint)
                paint.style = Paint.Style.FILL
                val value = progress.coerceAtMost(goal).toString()
                paint.textSize = 116f; paint.typeface = font
                while (paint.measureText(value) > 390f) paint.textSize -= 2f
                val numberSize = paint.textSize
                line(value, 540f - paint.measureText(value) / 2, 740f, numberSize, green, 390f)
                val target = context.getString(R.string.challenge_progress, progress.coerceAtMost(goal), goal)
                paint.textSize = 30f
                line(target, 540f - paint.measureText(target) / 2, 804f, 30f, green, 440f)
                val pct = "${(share * 100).toInt()}%"
                panel(RectF(96f, 1062f, 96f + 40f + pct.length * 30f, 1122f), green, 30f)
                line(pct, 116f, 1104f, 34f, mint)
                line(context.getString(if (progress >= goal) R.string.challenge_completed else R.string.challenges_title),
                    96f + 72f + pct.length * 30f, 1106f, 42f, green, 700f)
                paragraph(detail, 96f, 1150f, 32f, 876, green, 2)
                line("LEAFIT  /  " + context.getString(R.string.b5_ch_personal), 76f, 1405f, 24f, mint)
                val dir = File(context.cacheDir, "workout_cards").apply { check(isDirectory || mkdirs()) }
                File.createTempFile("leafit_wrapped_challenge_", ".png", dir).also { output ->
                    output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                }
            } finally { bitmap.recycle() }
        }
        withContext(Dispatchers.Main.immediate) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.workout.cards", file)
            val intent = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                    clipData = ClipData.newUri(context.contentResolver, "Leafit Wrapped", uri)
                }
            context.startActivity(Intent.createChooser(intent, "Leafit Wrapped"))
        }
    }
}
