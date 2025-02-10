package com.example.passionDaily.settings.state

import com.example.passionDaily.settings.base.SettingsViewModelState
import kotlinx.coroutines.flow.StateFlow

interface DialogState : SettingsViewModelState {
    override val showWithdrawalDialog: StateFlow<Boolean>
}