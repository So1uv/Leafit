package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import com.example.fitnesstracker.ui.components.LeafTonalButton
import com.example.fitnesstracker.ui.components.LeafButton
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.fitnesstracker.R
import kotlin.math.max
import kotlin.math.min

@Composable
fun ImageCropDialog(
    sourceUri: Uri,
    onCancel: () -> Unit,
    onCropped: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(sourceUri) { loadBitmap(context, sourceUri) }

    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var boxSizePx by remember { mutableStateOf(0) }

    val primary = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    stringResource(R.string.crop_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .onSizeChanged { boxSizePx = min(it.width, it.height) }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            }
                            .drawWithContent {
                                drawContent()
                                val r = size.width / 2f
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    radius = r * 1.5f,
                                    center = Offset(size.width / 2, size.height / 2),
                                    style = Stroke(width = r)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.9f),
                                    radius = r - 1.dp.toPx(),
                                    center = Offset(size.width / 2, size.height / 2),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offsetX
                                    translationY = offsetY
                                }
                        )
                    }

                    Text(
                        stringResource(R.string.crop_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LeafTonalButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.common_cancel))
                    }
                    LeafButton(
                        onClick = {
                            if (bitmap != null && boxSizePx > 0) {
                                val cropped = cropCircle(bitmap, scale, offsetX, offsetY, boxSizePx)
                                onCropped(cropped)
                            }
                        },
                        enabled = bitmap != null,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(com.example.fitnesstracker.ui.components.LeafIcons.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.common_save))
                    }
                }
            }
        }
    }
}

private fun loadBitmap(context: Context, uri: Uri): Bitmap? = runCatching {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        BitmapFactory.decodeStream(stream, null, opts)
    }
}.getOrNull()

private fun cropCircle(src: Bitmap, scale: Float, offsetX: Float, offsetY: Float, viewSize: Int): Bitmap {
    val out = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(out)

    val viewScale = 512f / viewSize
    val matrix = Matrix()

    val imgFit = max(512f / src.width, 512f / src.height)
    matrix.postScale(imgFit, imgFit)
    val scaledW = src.width * imgFit
    val scaledH = src.height * imgFit
    matrix.postTranslate((512 - scaledW) / 2f, (512 - scaledH) / 2f)
    matrix.postScale(scale, scale, 256f, 256f)
    matrix.postTranslate(offsetX * viewScale, offsetY * viewScale)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }

    val path = android.graphics.Path().apply {
        addCircle(256f, 256f, 256f, android.graphics.Path.Direction.CW)
    }
    canvas.clipPath(path)
    canvas.drawBitmap(src, matrix, paint)
    return out
}
