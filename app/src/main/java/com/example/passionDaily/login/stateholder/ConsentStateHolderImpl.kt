package com.example.passionDaily.login.stateholder

import com.example.passionDaily.login.domain.model.UserConsent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentStateHolderImpl @Inject constructor() : ConsentStateHolder {
    private val _consent = MutableStateFlow(
        UserConsent(
            termsOfService = false,
            privacyPolicy = false
        )
    )
    override val consent = _consent.asStateFlow()

    private val _isAgreeAllChecked = MutableStateFlow(false)
    override val isAgreeAllChecked = _isAgreeAllChecked.asStateFlow()

    override fun updateConsent(newConsent: UserConsent) {
        _consent.value = newConsent
    }

    override fun updateAgreeAllChecked(isChecked: Boolean) {
        _isAgreeAllChecked.value = isChecked
    }
}