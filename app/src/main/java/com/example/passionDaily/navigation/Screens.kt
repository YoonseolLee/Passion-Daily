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

    val navigateToQuote: () -> Unit = {
        navController.navigate("quote") {
            popUpTo("login") { inclusive = true }
        }
    }
}