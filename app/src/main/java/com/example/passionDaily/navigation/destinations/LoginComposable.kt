package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.login.presentation.screen.LoginScreen
import com.example.passionDaily.login.presentation.viewmodel.LoginViewModel

fun NavGraphBuilder.loginComposable(
    loginViewModel: LoginViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: () -> Unit
) {
    composable(route = "login") {
        LoginScreen(
            loginViewModel,
            onNavigateToQuote,
            onNavigateToTermsConsent
        )
    }
}