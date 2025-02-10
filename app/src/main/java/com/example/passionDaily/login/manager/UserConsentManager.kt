package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface UserConsentManager {
    val consent: StateFlow<UserConsent>
    val isAgreeAllChecked: StateFlow<Boolean>

    fun toggleAgreeAll()
    fun toggleIndividualItem(item: String)
}