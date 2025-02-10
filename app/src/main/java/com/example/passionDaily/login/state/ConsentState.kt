package com.example.passionDaily.login.state

import com.example.passionDaily.login.base.SharedLogInState
import com.example.passionDaily.login.domain.model.UserConsent
import kotlinx.coroutines.flow.StateFlow

interface ConsentState : SharedLogInState {
    override val consent: StateFlow<UserConsent>
    override val isAgreeAllChecked: StateFlow<Boolean>
}
