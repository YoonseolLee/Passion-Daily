package com.example.passionDaily.settings.action

import com.example.passionDaily.settings.base.SettingsViewModelActions

interface AuthenticationActions : SettingsViewModelActions {
    override fun logIn(onLogInSuccess: () -> Unit)
    override fun logOut(onLogoutSuccess: () -> Unit)
    override fun withdrawUser(onWithdrawSuccess: () -> Unit, onReLogInRequired: () -> Unit)
}