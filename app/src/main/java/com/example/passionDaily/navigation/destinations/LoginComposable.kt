package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.LoginScreen
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel

fun NavGraphBuilder.loginComposable(
    sharedLogInViewModel: SharedLogInViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: (String) -> Unit
) {
    composable(route = "login") {
        LoginScreen(
            sharedLogInViewModel = sharedLogInViewModel,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToTermsConsent = onNavigateToTermsConsent
        )
    }
}