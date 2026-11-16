package com.example.fitnesstracker.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsLeafState(): State<T> = collectAsLeafState(value)

@Composable
fun <T> Flow<T>.collectAsLeafState(initial: T): State<T> {
    val owner = LocalLifecycleOwner.current
    val visible = LocalLeafPageActive.current
    return produceState(initialValue = initial, this, owner, visible) {
        if (visible) owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            this@collectAsLeafState.collect { next -> value = next }
        }
    }
}
