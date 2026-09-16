package com.example.fitnesstracker

import android.app.Application
import android.content.res.Configuration
import com.example.fitnesstracker.widgets.LeafWidgets

class LeafitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LeafWidgets.initialize(this)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LeafWidgets.requestUpdate(this)
    }
}
