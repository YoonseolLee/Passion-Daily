package com.example.passionDaily.util

object Constants {
    const val SPLASH_SCREEN = "splash_screen"
    const val QUOTE_SCREEN = "quote_screen"

    fun quoteRoute(action: Action): String {
        return if (action == Action.NO_ACTION) QUOTE_SCREEN
        else "$QUOTE_SCREEN?action=${action.name}"
    }
}
