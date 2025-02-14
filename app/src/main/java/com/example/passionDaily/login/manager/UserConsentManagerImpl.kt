package com.example.passionDaily.login.manager

import com.example.passionDaily.login.domain.model.UserConsent
import com.example.passionDaily.login.stateholder.ConsentStateHolder
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class UserConsentManagerImpl @Inject constructor(
    private val consentStateHolder: ConsentStateHolder
) : UserConsentManager {
    override val consent: StateFlow<UserConsent> = consentStateHolder.consent
    override val isAgreeAllChecked: StateFlow<Boolean> = consentStateHolder.isAgreeAllChecked


    override fun toggleAgreeAll() {
        val currentState = !isAgreeAllChecked.value
        consentStateHolder.updateAgreeAllChecked(currentState)
        consentStateHolder.updateConsent(
            UserConsent(
                termsOfService = currentState,
                privacyPolicy = currentState
            )
        )
    }

    override fun toggleIndividualItem(item: String) {
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

    override suspend fun clearConsent() {
        consentStateHolder.clearConsent()
    }
}