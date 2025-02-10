package com.example.passionDaily.login.state

import com.example.passionDaily.login.base.SharedLogInState
import kotlinx.coroutines.flow.StateFlow

interface LoginState : SharedLogInState {
    override val authState: StateFlow<AuthState>
    override val userProfileJson: StateFlow<String?>
    override val userProfileJsonV2: StateFlow<String?>
    override val isLoading: StateFlow<Boolean>
}