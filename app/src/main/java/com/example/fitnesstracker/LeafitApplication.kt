package com.example.fitnesstracker

import android.app.Application
import android.content.res.Configuration
import com.example.fitnesstracker.widgets.LeafWidgets

class LeafitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LeafWidgets.initialize(this)
        runCatching { com.example.fitnesstracker.sleep.SleepAlarms.schedule(this) }
        Thread({
            runCatching { com.example.fitnesstracker.ui.components.LeafIcons.warmUp() }
        }, "leaf-icons").apply { priority = Thread.MIN_PRIORITY + 1 }.start()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LeafWidgets.requestUpdate(this)
    }
}
