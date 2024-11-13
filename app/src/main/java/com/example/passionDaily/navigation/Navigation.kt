package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.passionDaily.navigation.destinations.splashComposable
import com.example.passionDaily.util.Constants

// SetupNavigation.kt
@Composable
fun SetupNavigation(navController: NavHostController) {
    val screens = Screens(navController = navController)

    NavHost(
        navController = navController,
        startDestination = Constants.SPLASH_SCREEN,
    ) {
        splashComposable(
            navigateToQuote = screens.navigateToQuote,
        )
        // 추후 다른 화면들 추가
    }
}
