package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.ui.theme.LeafSurface

enum class LeafIconSize(val container: Dp, val glyph: Dp, val corner: Dp) {
    Small(36.dp, 20.dp, 12.dp),
    Medium(44.dp, 24.dp, 14.dp),
    Large(56.dp, 28.dp, 18.dp),
    Hero(72.dp, 36.dp, 24.dp)
}

enum class LeafTone { Primary, Secondary, Tertiary, Error, Neutral, Solid }

@Immutable
data class LeafToneColors(val container: Color, val content: Color)

@Composable
fun leafToneColors(tone: LeafTone): LeafToneColors {
    val c = MaterialTheme.colorScheme
    return when (tone) {
        LeafTone.Primary -> LeafToneColors(c.primaryContainer, c.onPrimaryContainer)
        LeafTone.Secondary -> LeafToneColors(c.secondaryContainer, c.onSecondaryContainer)
        LeafTone.Tertiary -> LeafToneColors(c.tertiaryContainer, c.onTertiaryContainer)
        LeafTone.Error -> LeafToneColors(c.errorContainer, c.onErrorContainer)
        LeafTone.Neutral -> LeafToneColors(c.surfaceContainerHighest, c.onSurfaceVariant)
        LeafTone.Solid -> LeafToneColors(c.primary, c.onPrimary)
    }
}

@Composable
fun LeafIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: LeafIconSize = LeafIconSize.Medium,
    tone: LeafTone = LeafTone.Primary,
    container: Color? = null,
    content: Color? = null,
    shape: Shape? = null,
    contentDescription: String? = null,
    filled: Boolean = true
) {
    val toneColors = leafToneColors(tone)
    val glyph = if (filled) LeafIcons.filled(icon) else icon
    LeafSurface(modifier, shape = shape ?: RoundedCornerShape(size.corner), color = container ?: toneColors.container,
        contentColor = content ?: toneColors.content) {
        Box(Modifier.size(size.container), contentAlignment = Alignment.Center) {
            Icon(glyph, contentDescription, Modifier.size(size.glyph), tint = content ?: toneColors.content)
        }
    }
}

@Composable
fun LeafCardHeader(
    icon: ImageVector?,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tone: LeafTone = LeafTone.Primary,
    iconSize: LeafIconSize = LeafIconSize.Medium,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (icon != null) LeafIconBadge(icon, size = iconSize, tone = tone)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        trailing?.invoke(this)
    }
}

object LeafSpacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val card = 20.dp
    val section = 16.dp
}

object LeafCorners {
    val card = 28.dp
    val nested = 20.dp
    val chip = 16.dp
}

@Composable
fun LeafFitText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    minFontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)
) {
    var scaled by remember(text, style) { mutableStateOf(style) }
    var ready by remember(text, style) { mutableStateOf(false) }
    Text(text, modifier.drawWithContentIf { ready }, color = color, style = scaled, maxLines = 1, softWrap = false,
        overflow = TextOverflow.Ellipsis, textAlign = textAlign,
        onTextLayout = { result ->
            val size = scaled.fontSize
            if (result.didOverflowWidth && size.isSp && size.value > minFontSize.value) {
                val next = (size.value * .92f).coerceAtLeast(minFontSize.value)
                scaled = scaled.copy(fontSize = androidx.compose.ui.unit.TextUnit(next, androidx.compose.ui.unit.TextUnitType.Sp),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0f, androidx.compose.ui.unit.TextUnitType.Sp))
            } else ready = true
        })
}

private fun Modifier.drawWithContentIf(show: () -> Boolean): Modifier =
    this.drawWithContent { if (show()) drawContent() }
