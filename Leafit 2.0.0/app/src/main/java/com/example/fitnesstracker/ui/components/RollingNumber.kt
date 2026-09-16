package com.example.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.ui.theme.NumericFontFamily

@Composable
fun RollingNumber(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    weight: FontWeight = FontWeight.Medium
) {
    val digits = value.coerceAtLeast(0).toString()

    val textStyle = style.copy(
        fontFamily = NumericFontFamily,
        fontWeight = weight,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum",
        color = color
    )

    Row(modifier = modifier.clearAndSetSemantics { text = AnnotatedString(digits) }) {
        digits.forEachIndexed { index, ch ->
            val place = digits.length - 1 - index
            androidx.compose.runtime.key(place) {
            AnimatedContent(
                targetState = ch,
                modifier = Modifier.clipToBounds(),
                transitionSpec = {
                    (slideInVertically { h -> h } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically { h -> -h } + fadeOut(tween(180)))
                },
                label = "digit_place_$place"
            ) { c ->
                Text(text = c.toString(), style = textStyle)
            }
            }
        }
    }
}
