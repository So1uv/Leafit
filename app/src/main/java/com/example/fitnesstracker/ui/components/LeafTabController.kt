package com.example.fitnesstracker.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

val LocalLeafPageActive = compositionLocalOf { true }
val LocalLeafMotionActive = compositionLocalOf { true }

@Stable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class LeafTabController(val pager: PagerState, private val scope: CoroutineScope) {
    private var navigation: Job? = null
    private var dragSamples: Channel<Float>? = null
    private var returnPage = 0
    private var requested by mutableStateOf<Int?>(null)
    var dragging by mutableStateOf(false)
        private set
    val position: Float get() = pager.currentPage + pager.currentPageOffsetFraction
    val selectedPage: Int get() = if (dragging) pager.currentPage else requested ?: pager.currentPage

    fun select(index: Int) {
        val destination = index.coerceIn(0, pager.pageCount - 1)
        if (!dragging && requested == destination && navigation?.isActive == true) return
        dragSamples?.close()
        dragSamples = null
        navigation?.cancel()
        dragging = false
        requested = destination
        navigation = scope.launch {
            try {
                val from = pager.currentPage
                if (kotlin.math.abs(destination - from) > 1) {
                    pager.scrollToPage(if (destination > from) destination - 1 else destination + 1)
                }
                pager.animateScrollToPage(destination, animationSpec = spring(dampingRatio = 1f, stiffness = 520f))
            } finally {
                if (requested == destination && !dragging) requested = null
            }
        }
    }

    fun beginDrag() {
        navigation?.cancel()
        dragSamples?.close()
        returnPage = pager.settledPage
        requested = null
        dragging = true
        val stream = Channel<Float>(Channel.CONFLATED)
        dragSamples = stream
        navigation = scope.launch {
            pager.scroll(MutatePriority.UserInput) {
                for (value in stream) {
                    val target = value.coerceIn(0f, (pager.pageCount - 1).toFloat())
                    val stride = pager.layoutInfo.pageSize + pager.layoutInfo.pageSpacing
                    if (stride > 0) scrollBy((target - position) * stride)
                }
            }
        }
    }

    fun dragTo(slot: Float) {
        if (slot.isFinite()) dragSamples?.trySend(slot)
    }

    fun endDrag(destination: Int, cancelled: Boolean = false) {
        if (dragging) select(if (cancelled) returnPage else destination)
    }
}
