package com.example.passionDaily.login.base

import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.state.AuthState
import kotlinx.coroutines.flow.StateFlow

interface SharedLogInState {
    val authState: StateFlow<AuthState>
    val userProfileJson: StateFlow<String?>
    val userProfileJsonV2: StateFlow<String?>
    val isLoading: StateFlow<Boolean>
    val consent: StateFlow<UserConsent>
    val isAgreeAllChecked: StateFlow<Boolean>
}