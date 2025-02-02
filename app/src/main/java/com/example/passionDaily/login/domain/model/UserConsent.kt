package com.example.passionDaily.login.domain.model

data class UserConsent(
    val termsOfService: Boolean,
    val privacyPolicy: Boolean
) {
    val isAllAgreed: Boolean
        get() = termsOfService && privacyPolicy
}