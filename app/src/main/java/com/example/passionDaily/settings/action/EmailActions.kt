package com.example.passionDaily.settings.action

import android.content.Intent
import com.example.passionDaily.settings.base.SettingsViewModelActions

interface EmailActions : SettingsViewModelActions {
    override fun createEmailIntent(): Intent?
}