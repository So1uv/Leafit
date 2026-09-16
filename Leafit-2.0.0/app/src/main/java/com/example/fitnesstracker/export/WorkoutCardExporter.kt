package com.example.fitnesstracker.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

data class WorkoutData(
    val name: String,
    val distance: String,
    val pace: String,
    val calories: String,
    val duration: String,
    val date: String,
    val mapImageBitmap: Bitmap? = null
)

class WorkoutCardExporter(private val context: Context) {
    
    fun exportWorkoutCard(
        workoutData: WorkoutData,
        format: ExportFormat = ExportFormat.PNG
    ): File {
        return when (format) {
            ExportFormat.PNG -> createPNGCard(workoutData)
            ExportFormat.JPEG -> createJPEGCard(workoutData)
        }
    }
    
    fun shareWorkoutCard(
        workoutData: WorkoutData,
        platform: SharePlatform = SharePlatform.TELEGRAM
    ) {
        val cardFile = exportWorkoutCard(workoutData)
        val cardUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cardFile
        )
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, cardUri)
            type = "image/png"
            
            when (platform) {
                SharePlatform.TELEGRAM -> {
                    `package` = "org.telegram.messenger"
                }
                SharePlatform.INSTAGRAM_STORIES -> {
                    `package` = "com.instagram.android"
                    putExtra("com.instagram.share.ADD_TO_STORY", true)
                }
                SharePlatform.WHATSAPP -> {
                    `package` = "com.whatsapp"
                }
                SharePlatform.SYSTEM -> {
                    // Use system chooser
                    `package` = null
                }
            }
        }
        
        try {
            context.startActivity(
                Intent.createChooser(shareIntent, "Share Workout")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createPNGCard(workoutData: WorkoutData): File {
        val bitmap = generateCardBitmap(workoutData)
        val file = File(context.cacheDir, "workout_card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
    
    private fun createJPEGCard(workoutData: WorkoutData): File {
        val bitmap = generateCardBitmap(workoutData)
        val file = File(context.cacheDir, "workout_card_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }
    
    private fun generateCardBitmap(workoutData: WorkoutData): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val backgroundColor = Color.parseColor("#1A1A1A")
        val accentColor = Color.parseColor("#4CAF50")
        val textColor = Color.WHITE
        val labelColor = Color.parseColor("#BDBDBD")
        
        canvas.drawColor(backgroundColor)
        
        val roundedCornerRadius = 32f
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2C2C2C")
        }
        
        val marginLeft = 40f
        val marginRight = 40f
        val marginTop = 100f
        val marginBottom = 100f
        
        canvas.drawRoundRect(
            RectF(marginLeft, marginTop, width - marginRight, height - marginBottom),
            roundedCornerRadius,
            roundedCornerRadius,
            cardPaint
        )
        
        var yPos = marginTop + 60f
        
        drawText(canvas, workoutData.name, width / 2f, yPos, 48, textColor, true)
        yPos += 80f
        
        if (workoutData.mapImageBitmap != null) {
            val mapHeight = (height * 0.35).toInt()
            val mapLeft = marginLeft.toInt() + 20
            val mapTop = yPos.toInt()
            val mapWidth = width - mapLeft - 20
            
            val scaledMap = Bitmap.createScaledBitmap(
                workoutData.mapImageBitmap,
                mapWidth,
                mapHeight,
                true
            )
            
            canvas.drawBitmap(scaledMap, mapLeft.toFloat(), mapTop.toFloat(), null)
            yPos += mapHeight + 40f
        }
        
        yPos = height - marginBottom - 320f
        
        drawStatRow(canvas, "Distance", workoutData.distance, width / 2f, yPos, accentColor, labelColor, textColor)
        yPos += 100f
        
        drawStatRow(canvas, "Pace", workoutData.pace, width / 2f, yPos, accentColor, labelColor, textColor)
        yPos += 100f
        
        drawStatRow(canvas, "Calories", workoutData.calories, width / 2f, yPos, accentColor, labelColor, textColor)
        yPos += 100f
        
        drawStatRow(canvas, "Duration", workoutData.duration, width / 2f, yPos, accentColor, labelColor, textColor)
        
        drawText(canvas, workoutData.date, width / 2f, height - marginBottom + 40, 28, labelColor, true)
        
        return bitmap
    }
    
    private fun drawStatRow(
        canvas: Canvas,
        label: String,
        value: String,
        centerX: Float,
        y: Float,
        accentColor: Int,
        labelColor: Int,
        valueColor: Int
    ) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor
            textSize = 32f
            textAlign = Paint.Align.RIGHT
        }
        
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 48f
            textAlign = Paint.Align.LEFT
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        
        canvas.drawText(label, centerX - 20, y, labelPaint)
        canvas.drawText(value, centerX + 20, y, valuePaint)
    }
    
    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Int,
        color: Int,
        bold: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size.toFloat()
            textAlign = Paint.Align.CENTER
            if (bold) {
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
        }
        canvas.drawText(text, x, y, paint)
    }
}

@Composable
fun WorkoutShareButton(
    workoutData: WorkoutData,
    onShare: (SharePlatform) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by androidx.compose.runtime.mutableStateOf(false)
    
    Column(modifier = modifier) {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Share Workout",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (expanded) {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    listOf(
                        SharePlatform.TELEGRAM to "Telegram",
                        SharePlatform.INSTAGRAM_STORIES to "Instagram Stories",
                        SharePlatform.WHATSAPP to "WhatsApp",
                        SharePlatform.SYSTEM to "More Options"
                    ).forEach { (platform, label) ->
                        Button(
                            onClick = {
                                onShare(platform)
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }
    }
}

enum class ExportFormat {
    PNG, JPEG
}

enum class SharePlatform {
    TELEGRAM,
    INSTAGRAM_STORIES,
    WHATSAPP,
    SYSTEM
}
