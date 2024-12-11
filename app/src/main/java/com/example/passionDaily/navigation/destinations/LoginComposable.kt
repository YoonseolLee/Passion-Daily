package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.LoginScreen
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

fun NavGraphBuilder.loginComposable(
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: (String) -> Unit
) {
    composable(route = "login") {
        val sharedSignInViewModel: SharedSignInViewModel = hiltViewModel()

        LoginScreen(
            sharedSignInViewModel = sharedSignInViewModel,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToTermsConsent = onNavigateToTermsConsent
        )
    }

    // TODO: JSON 전달과 함께 다음 화면 TermsConsent로 이동
}