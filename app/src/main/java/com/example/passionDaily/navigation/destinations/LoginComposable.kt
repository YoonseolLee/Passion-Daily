package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.LoginScreen
import com.example.passionDaily.ui.viewmodels.LoginViewModel

fun NavGraphBuilder.loginComposable(
    onNavigateToQuote: () -> Unit
) {
    composable(route = "login") {
        val loginViewModel: LoginViewModel = hiltViewModel()

        LoginScreen(
            loginViewModel = loginViewModel,
            onNavigateToQuote = onNavigateToQuote
        )
    }
}