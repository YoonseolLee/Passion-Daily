package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.stateholder.ConsentStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserConsentManager @Inject constructor(
    private val consentStateHolder: ConsentStateHolder
) {
    val consent = consentStateHolder.consent
    val isAgreeAllChecked = consentStateHolder.isAgreeAllChecked

    fun toggleAgreeAll() {
        val currentState = !isAgreeAllChecked.value
        consentStateHolder.updateAgreeAllChecked(currentState)
        consentStateHolder.updateConsent(
            UserConsent(
                termsOfService = currentState,
                privacyPolicy = currentState
            )
        )
    }

    fun toggleIndividualItem(item: String) {
        val currentConsent = consent.value
        val newConsent = when (item) {
            UserConsent::termsOfService.name -> currentConsent.copy(
                termsOfService = !currentConsent.termsOfService
            )
            UserConsent::privacyPolicy.name -> currentConsent.copy(
                privacyPolicy = !currentConsent.privacyPolicy
            )
            else -> currentConsent
        }
        consentStateHolder.updateConsent(newConsent)
        updateAgreeAllState()
    }

    private fun updateAgreeAllState() {
        consentStateHolder.updateAgreeAllChecked(consent.value.isAllAgreed)
    }
}