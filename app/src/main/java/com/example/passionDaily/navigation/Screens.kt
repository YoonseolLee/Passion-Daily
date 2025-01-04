package com.example.passionDaily.navigation

import androidx.navigation.NavHostController

class Screens(private val navController: NavHostController) {

    val navigateToLogin: () -> Unit = {
        navController.navigate("login") {
        }
    }

    val navigateToTermsConsent: (String) -> Unit = { userProfileJson ->
        navController.navigate("termsConsent/${userProfileJson}")
    }

    val navigateToQuote: () -> Unit = {
        navController.navigate("quote") {
        }
    }

    val navigateToCategory: () -> Unit = {
        navController.navigate("category") {
        }
    }

    val navigateToQuoteFromNavBar: () -> Unit = {
        navController.navigate("quote") {
            popUpTo("quote") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToFavorites: () -> Unit = {
        navController.navigate("favorites") {
            popUpTo("quote") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate("settings") {
            popUpTo("quote") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}