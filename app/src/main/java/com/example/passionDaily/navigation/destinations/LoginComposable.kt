package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.LoginScreen
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

fun NavGraphBuilder.loginComposable(
    sharedSignInViewModel: SharedSignInViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: (String) -> Unit
) {
    composable(route = "login") {
        LoginScreen(
            sharedSignInViewModel = sharedSignInViewModel,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToTermsConsent = onNavigateToTermsConsent
        )
    }
}