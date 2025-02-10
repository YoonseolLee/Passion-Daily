package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passionDaily.termsConsent.presentation.screen.TermsConsentScreen
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel

fun NavGraphBuilder.termsConsentComposable(
    onNavigateToQuoteScreen: () -> Unit
) {
    composable(
        route = "termsConsent/{userProfileJson}",
        arguments = listOf(
            navArgument("userProfileJson") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val userProfileJson = backStackEntry.arguments?.getString("userProfileJson")
        val sharedLogInViewModel: SharedLogInViewModel = hiltViewModel()

        TermsConsentScreen(
            userProfileJson = userProfileJson,
            sharedLogInViewModel = sharedLogInViewModel,
            onNavigateToQuoteScreen = onNavigateToQuoteScreen
        )
    }
}