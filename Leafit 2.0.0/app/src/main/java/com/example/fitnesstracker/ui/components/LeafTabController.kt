package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

val LocalLeafPageActive = androidx.compose.runtime.compositionLocalOf { true }

/** One pager owns both the visible page and the drag preview. */
@Stable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class LeafTabController(val pager: PagerState, private val scope: CoroutineScope) {
    private var motion: Job? = null
    private var samples: Channel<Float>? = null
    private var dragOrigin = 0
    var dragging by mutableStateOf(false)
        private set
    val position: Float get() = pager.currentPage + pager.currentPageOffsetFraction

    fun select(index: Int) {
        samples?.close(); samples = null
        motion?.cancel()
        dragging = false
        motion = scope.launch {
            pager.animateScrollToPage(index.coerceIn(0, pager.pageCount - 1),
                animationSpec = spring(dampingRatio = .92f, stiffness = 420f))
        }
    }

    fun beginDrag() {
        samples?.close()
        motion?.cancel()
        dragOrigin = pager.settledPage
        dragging = true
        val channel = Channel<Float>(Channel.CONFLATED)
        samples = channel
        motion = scope.launch {
            pager.scroll(MutatePriority.UserInput) {
                for (slot in channel) {
                    val width = pager.layoutInfo.pageSize
                    if (width > 0) scrollBy((slot.coerceIn(0f, (pager.pageCount - 1).toFloat()) - position) * width)
                }
            }
        }
    }

    fun dragTo(slot: Float) { samples?.trySend(slot) }

    fun endDrag(destination: Int, cancelled: Boolean = false) {
        select(if (cancelled) dragOrigin else destination)
    }
}
