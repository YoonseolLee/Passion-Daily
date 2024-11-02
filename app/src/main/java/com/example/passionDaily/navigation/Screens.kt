package com.example.passionDaily.navigation

import androidx.navigation.NavHostController
import com.example.passionDaily.util.Action
import com.example.passionDaily.util.Constants
import com.example.passionDaily.util.Constants.SPLASH_SCREEN

class Screens(
    private val navController: NavHostController,
) {
    val navigateToQuote: (Action) -> Unit = { action ->
        navController.navigate(Constants.quoteRoute(action)) {
            popUpTo(Constants.SPLASH_SCREEN) { inclusive = true }
        }
    }
}
