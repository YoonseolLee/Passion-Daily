package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passionDaily.ui.screens.TermsConsentScreen
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

fun NavGraphBuilder.termsConsentComposable(
    onNavigateToGenderAgeSelection: (String) -> Unit
) {
    composable(
        route = "termsConsent/{userProfileJson}",
        arguments = listOf(
            navArgument("userProfileJson") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val userProfileJson = backStackEntry.arguments?.getString("userProfileJson")
        val sharedSignInViewModel: SharedSignInViewModel = hiltViewModel()

        TermsConsentScreen(
            userProfileJson = userProfileJson,
            sharedSignInViewModel = sharedSignInViewModel,
            onNavigateToGenderAgeSelection = onNavigateToGenderAgeSelection
        )
    }
}