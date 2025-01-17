package com.example.passionDaily.util

data class UserConsent(
    val termsOfService: Boolean,
    val privacyPolicy: Boolean
) {
    val isAllAgreed: Boolean
        get() = termsOfService && privacyPolicy
}