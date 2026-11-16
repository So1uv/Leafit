package com.example.fitnesstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.fitnesstracker.ui.theme.animatedLeafBackground
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged

@Stable
class LeafHeaderState(private val travel: Float) {
    var hidden by mutableFloatStateOf(0f)
        private set
    val fraction: Float get() = (hidden / travel).coerceIn(0f, 1f)
    val connection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            val delta = if (consumed.y != 0f) consumed.y else available.y.coerceAtLeast(0f)
            hidden = (hidden - delta).coerceIn(0f, travel)
            return Offset.Zero
        }
    }
}

@Composable
fun rememberLeafHeaderState(): LeafHeaderState {
    val travel = with(LocalDensity.current) { 56.dp.toPx() }
    return remember(travel) { LeafHeaderState(travel) }
}

fun Modifier.leafHeaderMotion(state: LeafHeaderState?): Modifier = graphicsLayer {
    val fraction = state?.fraction ?: 0f
    alpha = 1f - fraction
    translationY = -fraction * 20.dp.toPx()
    scaleX = 1f - fraction * .025f
    scaleY = scaleX
}

fun Modifier.leafHeaderScrim(color: Color, state: LeafHeaderState?): Modifier = drawWithCache {
    val brush = Brush.verticalGradient(0f to color.copy(alpha = .98f), .72f to color.copy(alpha = .96f), 1f to Color.Transparent)
    onDrawBehind { drawRect(brush, alpha = 1f - (state?.fraction ?: 0f)) }
}

@Composable
fun leafHeaderInset() = appHeaderHeight() + 16.dp +
    with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }

@Composable
fun LeafScreenScaffold(headerState: LeafHeaderState,
    topBar: @Composable () -> Unit, containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    content: @Composable (PaddingValues) -> Unit) {
    val fallbackInset = leafHeaderInset()
    val density = LocalDensity.current
    var headerHeight by remember { mutableIntStateOf(0) }
    val inset = if (headerHeight > 0) with(density) { headerHeight.toDp() } else fallbackInset
    Box(Modifier.fillMaxSize().animatedLeafBackground(containerColor).nestedScroll(headerState.connection)) {
        content(PaddingValues(top = inset))
        Box(Modifier.fillMaxWidth().onSizeChanged { headerHeight = it.height }) { topBar() }
    }
}
