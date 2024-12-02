package com.example.passionDaily.navigation

import android.net.Uri
import androidx.navigation.NavHostController
import com.google.gson.Gson

class Screens(private val navController: NavHostController) {

    val navigateToLogin: () -> Unit = {
        navController.navigate("login") {
            // pop up to splash screen
        }
    }

    val navigateToGenderAndAgeSelection: () -> Unit = {
        navController.navigate("select_gender_age") {
            popUpTo("login") { inclusive = true }
        }
    }

    val navigateToGenderAndAgeSelectionWithUserMap: (Map<String, Any>) -> Unit = { pendingUserMap ->
        val pendingUserMapJson = Uri.encode(Gson().toJson(pendingUserMap))
        navController.navigate("select_gender_age?pendingUserMap=$pendingUserMapJson") {
            popUpTo("login") { inclusive = true }
        }
    }

    val navigateToQuote: () -> Unit = {
        navController.navigate("quote") {
            popUpTo("login") { inclusive = true }
        }
    }

    val navigateToSignUpComplete: () -> Unit = {
        navController.navigate("signup_complete") {
            popUpTo("select_gender_age") { inclusive = true }
        }
    }
}