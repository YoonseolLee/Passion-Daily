package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.splash.SplashScreen
import com.example.passionDaily.util.Action
import com.example.passionDaily.util.Constants

fun NavGraphBuilder.splashComposable(navigateToQuote: (Action) -> Unit) {
    composable(route = Constants.SPLASH_SCREEN) {
        SplashScreen(navigateToQuote = navigateToQuote)
    }
}
