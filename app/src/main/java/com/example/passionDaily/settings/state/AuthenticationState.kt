package com.example.passionDaily.settings.state

import com.example.passionDaily.settings.base.SettingsViewModelState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationState : SettingsViewModelState {
    override val currentUser: StateFlow<FirebaseUser?>
    override val isLoading: StateFlow<Boolean>
}