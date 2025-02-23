package com.example.passionDaily.settings.base

import android.content.Intent
import java.time.LocalTime

interface SettingsViewModelActions {
    fun createEmailIntent(): Intent?
}