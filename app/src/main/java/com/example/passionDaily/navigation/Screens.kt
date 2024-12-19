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

    val navigateToGenderAgeSelection: (String) -> Unit = { userProfileJsonV2 ->
        navController.navigate("genderAgeSelection/${userProfileJsonV2}")
    }

    val navigateToQuote: () -> Unit = {
        navController.navigate("quote") {
        }
    }

    val navigateToCategory: () -> Unit = {
        navController.navigate("category") {
        }
    }
}