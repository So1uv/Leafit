package com.example.fitnesstracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.ui.navigation.bottomNavItems
import com.example.fitnesstracker.ui.theme.LeafSurface
import kotlin.math.floor
import kotlin.math.roundToInt

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ExpressiveBottomBar(controller: LeafTabController, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val selected by remember(controller) { derivedStateOf { controller.pager.currentPage } }
    val count = bottomNavItems.size
    val expansion = List(count) { index ->
        animateFloatAsState(if (selected == index) 1f else 0f,
            spring(dampingRatio = .88f, stiffness = 550f), label = "tabWidth$index")
    }
    val minimum = with(density) { 52.dp.toPx() }
    val height = 60.dp + 12.dp * (density.fontScale - 1f).coerceAtLeast(0f)

    fun widths(width: Float): List<Float> {
        val base = minimum.coerceAtMost(width / count)
        val extra = (width - base * count).coerceAtLeast(0f)
        val sum = expansion.sumOf { it.value.coerceAtLeast(0f).toDouble() }.toFloat().coerceAtLeast(.001f)
        return expansion.map { base + extra * it.value.coerceAtLeast(0f) / sum }
    }

    LeafSurface(modifier, shape = CircleShape, color = colors.surfaceContainerLow) {
        Layout(modifier = Modifier.fillMaxWidth().padding(4.dp).height(height).selectableGroup()
            .drawBehind {
                val cells = widths(size.width)
                val position = controller.position.coerceIn(0f, (count - 1).toFloat())
                val from = floor(position).toInt()
                val to = (from + 1).coerceAtMost(count - 1)
                val fraction = position - from
                val start = cells.take(from).sum() + if (from != to) cells[from] * fraction else 0f
                val width = cells[from] + (cells[to] - cells[from]) * fraction
                val x = if (rtl) size.width - start - width else start
                drawRoundRect(colors.secondaryContainer,
                    topLeft = Offset(x + 2.dp.toPx(), 2.dp.toPx()),
                    size = Size((width - 4.dp.toPx()).coerceAtLeast(0f), size.height - 4.dp.toPx()),
                    cornerRadius = CornerRadius(size.height / 2f))
            }
            .pointerInput(controller, rtl, minimum) {
                var anchors = emptyList<Float>()
                var hover = selected
                fun slotAt(x: Float): Float {
                    val px = if (rtl) size.width - x else x
                    if (px <= anchors.first()) return 0f
                    if (px >= anchors.last()) return (count - 1).toFloat()
                    val left = (0 until count - 1).first { px <= anchors[it + 1] }
                    return left + (px - anchors[left]) / (anchors[left + 1] - anchors[left])
                }
                fun move(x: Float) {
                    val slot = slotAt(x)
                    controller.dragTo(slot)
                    val next = slot.roundToInt().coerceIn(0, count - 1)
                    if (next != hover) {
                        hover = next
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                detectHorizontalDragGestures(
                    onDragStart = { point ->
                        val cells = widths(size.width.toFloat())
                        anchors = cells.indices.map { cells.take(it).sum() + cells[it] / 2f }
                        hover = controller.pager.currentPage
                        controller.beginDrag()
                        move(point.x)
                    },
                    onHorizontalDrag = { change, _ -> change.consume(); move(change.position.x) },
                    onDragEnd = { controller.endDrag(hover) },
                    onDragCancel = { controller.endDrag(hover, cancelled = true) }
                )
            }, content = {
                bottomNavItems.forEachIndexed { index, item ->
                    val active = selected == index
                    val label = stringResource(item.labelRes)
                    val tint = if (active) colors.onSecondaryContainer else colors.onSurfaceVariant
                    Row(Modifier.fillMaxHeight().clip(CircleShape)
                        .selectable(active, role = Role.Tab, indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                if (!active) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    controller.select(index)
                                }
                            }).semantics { contentDescription = label }.padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (active) item.selectedIcon else item.icon, null, Modifier.size(24.dp), tint)
                        AnimatedVisibility(active,
                            enter = expandHorizontally(spring(.9f, 550f), expandFrom = Alignment.Start) + fadeIn(tween(140)),
                            exit = shrinkHorizontally(spring(1f, 650f), shrinkTowards = Alignment.Start) + fadeOut(tween(80))) {
                            Text(label, Modifier.padding(start = 8.dp).clearAndSetSemantics { },
                                style = MaterialTheme.typography.labelLarge, color = tint,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }) { measurables, constraints ->
            val cells = widths(constraints.maxWidth.toFloat())
            val boundaries = List(count + 1) { i -> cells.take(i).sum().roundToInt() }
            val placeables = measurables.mapIndexed { index, measurable ->
                measurable.measure(Constraints.fixed((boundaries[index + 1] - boundaries[index]).coerceAtLeast(0), constraints.maxHeight))
            }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable -> placeable.placeRelative(boundaries[index], 0) }
            }
        }
    }
}
