package com.example.passionDaily.navigation

import androidx.navigation.NavHostController
import androidx.navigation.navOptions

class Screens(private val navController: NavHostController) {

    private object Routes {
        const val QUOTE = "quote"
        const val LOGIN = "login"
        const val TERMS_CONSENT = "termsConsent"
        const val CATEGORY = "category"
        const val FAVORITES = "favorites"
        const val SETTINGS = "settings"
    }

    fun navigateToQuote() {
        navController.navigate(Routes.QUOTE) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    fun navigateToLogin() {
        navController.navigate(Routes.LOGIN, simpleNavOptions)
    }

    fun navigateToTermsConsent(userProfileJson: String) {
        navController.navigate("${Routes.TERMS_CONSENT}/$userProfileJson", simpleNavOptions)
    }

    fun navigateToCategory() {
        navController.navigate(Routes.CATEGORY, simpleNavOptions)
    }

    fun navigateToQuoteFromNavBar() {
        navController.navigate(Routes.QUOTE, statePreservingNavOptions)
    }

    fun navigateToFavoritesFromNavBar() {
        navController.navigate(Routes.FAVORITES, statePreservingNavOptions)
    }

    fun navigateToSettingsFromNavBar() {
        navController.navigate(Routes.SETTINGS, simpleNavOptions)
    }

    private val statePreservingNavOptions = navOptions {
        launchSingleTop = true
        restoreState = true
    }

    private val simpleNavOptions = navOptions {
        launchSingleTop = true
    }
}
