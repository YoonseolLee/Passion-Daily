package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserConsentManager @Inject constructor() {

    private val _consent = MutableStateFlow(
        UserConsent(
            termsOfService = false,
            privacyPolicy = false
        )
    )
    val consent = _consent.asStateFlow()

    private val _isAgreeAllChecked = MutableStateFlow(false)
    val isAgreeAllChecked = _isAgreeAllChecked.asStateFlow()

    fun toggleAgreeAll() {
        val currentState = !_isAgreeAllChecked.value
        _isAgreeAllChecked.value = currentState
        _consent.value = UserConsent(
            termsOfService = currentState,
            privacyPolicy = currentState
        )
    }

    fun toggleIndividualItem(item: String) {
        val currentConsent = _consent.value
        _consent.value = when (item) {
            "termsOfService" -> currentConsent.copy(
                termsOfService = !currentConsent.termsOfService
            )

            "privacyPolicy" -> currentConsent.copy(
                privacyPolicy = !currentConsent.privacyPolicy
            )

            else -> currentConsent
        }
        updateAgreeAllState()
    }

    private fun updateAgreeAllState() {
        _isAgreeAllChecked.value = _consent.value.isAllAgreed
    }
}