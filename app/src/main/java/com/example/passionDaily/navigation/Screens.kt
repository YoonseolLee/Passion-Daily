package com.example.passionDaily.navigation

import android.net.Uri
import androidx.navigation.NavHostController
import com.google.gson.Gson

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
            popUpTo("login") { inclusive = true }
        }
    }
}