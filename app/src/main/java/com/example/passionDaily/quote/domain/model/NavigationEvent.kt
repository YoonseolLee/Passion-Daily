package com.example.passionDaily.quote.domain.model

sealed class NavigationEvent {
    object NavigateToQuote : NavigationEvent()
    data class NavigateToTermsConsent(val userProfileJson: String) : NavigationEvent()
}