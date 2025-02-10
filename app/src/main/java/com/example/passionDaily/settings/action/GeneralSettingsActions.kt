package com.example.passionDaily.settings.action

import com.example.passionDaily.settings.base.SettingsViewModelActions

interface GeneralSettingsActions : SettingsViewModelActions {
    override fun loadUserSettings()
    override fun updateShowWithdrawalDialog(show: Boolean)
}