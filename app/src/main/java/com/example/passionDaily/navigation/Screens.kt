package com.example.passionDaily.navigation

import androidx.navigation.NavHostController
import androidx.navigation.navOptions

class Screens(private val navController: NavHostController) {

    private object Routes {
        const val QUOTE = "quote"
        const val CATEGORY = "category"
        const val FAVORITES = "favorites"
        const val SETTINGS = "settings"
    }

    fun navigateToQuote() {
        navController.navigate(Routes.QUOTE, simpleNavOptions)
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