package com.example.passionDaily.util

import androidx.navigation.NavAction

object Constants {
    const val SPLASH_SCREEN = "splash_screen"
    const val LOGIN_SCREEN = "login_screen"
    const val QUOTE_SCREEN = "quote_screen"

    fun navRoute(navAction: NavAction): String {
        return when (navAction) {
            NavAction.NAVIGATE_TO_LOGIN -> LOGIN_SCREEN
            NavAction.NAVIGATE_TO_QUOTE -> QUOTE_SCREEN
        }
    }
}
