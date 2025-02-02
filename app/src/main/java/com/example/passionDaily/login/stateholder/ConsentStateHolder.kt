package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.domain.model.UserConsent
import kotlinx.coroutines.flow.StateFlow

interface ConsentStateHolder {
    val consent: StateFlow<UserConsent>
    val isAgreeAllChecked: StateFlow<Boolean>

    fun updateConsent(newConsent: UserConsent)
    fun updateAgreeAllChecked(isChecked: Boolean)
}