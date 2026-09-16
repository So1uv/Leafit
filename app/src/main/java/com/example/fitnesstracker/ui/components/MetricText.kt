package com.example.fitnesstracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.ui.theme.NumericFontFamily

@Composable
fun MetricText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    weight: FontWeight = FontWeight.Medium
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(
            fontFamily = NumericFontFamily,
            fontWeight = weight,
            letterSpacing = 0.sp,
            fontFeatureSettings = "tnum"
        )
    )
}

@Composable
fun MetricValueWithSuffix(
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.titleLarge,
    suffixStyle: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    suffixColor: Color = color.copy(alpha = 0.72f),
    valueWeight: FontWeight = FontWeight.Medium
) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = color,
                    fontFamily = NumericFontFamily,
                    fontWeight = valueWeight,
                    fontSize = valueStyle.fontSize,
                    letterSpacing = 0.sp,
                    fontFeatureSettings = "tnum"
                )
            ) {
                append(value)
            }
            withStyle(
                SpanStyle(
                    color = suffixColor,
                    fontFamily = suffixStyle.fontFamily ?: NumericFontFamily,
                    fontWeight = suffixStyle.fontWeight ?: FontWeight.Normal,
                    fontSize = suffixStyle.fontSize
                )
            ) {
                append(suffix)
            }
        },
        style = valueStyle.copy(lineHeight = valueStyle.lineHeight)
    )
}
