package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passionDaily.login.presentation.viewmodel.LoginViewModel
import com.example.passionDaily.termsConsent.presentation.screen.TermsConsentScreen

fun NavGraphBuilder.termsConsentComposable(
    loginViewModel: LoginViewModel,
    onNavigateToQuoteScreen: () -> Unit,
) {
    composable(route = "termsConsent") {
        TermsConsentScreen(
            loginViewModel =  loginViewModel,
            onNavigateToQuoteScreen = onNavigateToQuoteScreen,
        )
    }
}