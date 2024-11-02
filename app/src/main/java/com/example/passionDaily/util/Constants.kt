package com.example.passionDaily.util

object Constants {
    const val SPLASH_SCREEN = "splash"
    const val QUOTE_SCREEN = "quote"

    // Route Builder 함수
    fun quoteRoute(action: Action) = "$QUOTE_SCREEN/${action.name}"
}
