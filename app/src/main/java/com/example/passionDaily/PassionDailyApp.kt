package com.example.passionDaily

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PassionDailyApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
