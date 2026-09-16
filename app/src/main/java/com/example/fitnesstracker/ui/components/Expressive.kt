package com.example.fitnesstracker.ui.components

import com.example.fitnesstracker.ui.theme.LeafSurface as Surface
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ExpressiveDialog(
    onDismissRequest: () -> Unit,
    title: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow),
        label = "dlgScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(180, easing = EaseOutCubic),
        label = "dlgAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (appeared) 0f else 16f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "dlgTranslate"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    translationY = translateY
                    transformOrigin = TransformOrigin(0.5f, 0.85f)
                },
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                if (title != null) {
                    ProvideTextStyle(
                        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    ) { title() }
                    Spacer(Modifier.height(16.dp))
                }
                if (content != null) {
                    Column(
                        Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) { content() }
                    Spacer(Modifier.height(20.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) dismissButton()
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun GlowProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp
) {
    val anim by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "wavyBar"
    )
    val phase by if (LocalLeafPageActive.current && progress > .01f) {
        rememberInfiniteTransition(label = "wavyPhase").animateFloat(0f, 1f,
            infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "phase")
    } else androidx.compose.runtime.rememberUpdatedState(0f)
    Canvas(modifier.fillMaxWidth().height(height * 2f)) {
        val stroke = height.toPx() * 0.58f
        val midY = size.height / 2f
        val dotR = stroke * 0.55f
        val w = (size.width - dotR * 2f) * anim
        val amp = (size.height - stroke) / 2f * 0.8f
        val wl = 24.dp.toPx()
        val gap = 5.dp.toPx()

        if (w + gap < size.width - dotR * 3f) {
            drawLine(
                color = color.copy(alpha = 0.22f),
                start = Offset(w + gap, midY),
                end = Offset(size.width - dotR * 3f, midY),
                strokeWidth = stroke * 0.62f,
                cap = StrokeCap.Round
            )
        }

        drawCircle(color.copy(alpha = 0.55f), radius = dotR, center = Offset(size.width - dotR, midY))

        if (w > stroke) {
            val path = Path()
            var x = 0f
            val step = 2.5.dp.toPx()

            while (x <= w) {
                val ramp = (x / (wl * 0.8f)).coerceIn(0f, 1f)
                val y = midY + amp * ramp * sin((x / wl + phase) * 2f * PI).toFloat()
                if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
                x += step
            }
            drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else if (w > 0f) {
            drawCircle(color, radius = stroke / 2f, center = Offset(stroke / 2f, midY))
        }
    }
}

@Composable
fun LargeScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier.padding(start = 2.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        LeafTitle(
            text,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HistoryDateButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = { Icon(LeafIcons.CalendarMonth, null, Modifier.size(20.dp)) },
        shape = RoundedCornerShape(50),
        border = null,
        modifier = modifier.heightIn(min = 48.dp),
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer))
}

@androidx.compose.runtime.Composable
fun bmiCategoryString(key: String): String = when (key) {
    "underweight" -> androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.bmi_underweight)
    "normal"      -> androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.bmi_normal)
    "overweight"  -> androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.bmi_overweight)
    "obese"       -> androidx.compose.ui.res.stringResource(com.example.fitnesstracker.R.string.bmi_obese)
    else          -> key
}
