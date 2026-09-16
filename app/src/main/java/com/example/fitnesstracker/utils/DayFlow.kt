package com.example.fitnesstracker.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

internal fun currentDayFlow() = flow {
    while (true) {
        emit(DateUtils.todayStart())
        delay(15_000)
    }
}.distinctUntilChanged()
