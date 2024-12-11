package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.passionDaily.navigation.destinations.loginComposable
import com.example.passionDaily.navigation.destinations.quoteComposable

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        loginComposable(
            onNavigateToQuote = screens.navigateToQuote,
            onNavigateToTermsConsent = { userProfileJson ->
                screens.navigateToTermsConsent(userProfileJson)
            }
        )

//        termsConsentComposable(
//            onNavigateToGenderAgeSelection = screens.navigateToGenderAgeSelection
//        )

        quoteComposable()
    }
}
