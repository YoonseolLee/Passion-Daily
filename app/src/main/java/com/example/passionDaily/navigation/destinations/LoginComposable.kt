package com.example.passionDaily.navigation.destinations

import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.LoginScreen
import com.example.passionDaily.ui.viewmodels.LoginViewModel

fun NavGraphBuilder.loginComposable(
    onNavigateToGenderAndAgeGroup: (Map<String, Any>) -> Unit,
    onNavigateToQuote: () -> Unit
) {
    composable(route = "login") {
        val loginViewModel: LoginViewModel = hiltViewModel()
        val pendingUserMap = loginViewModel.pendingUserMap.collectAsState().value

        LoginScreen(
            loginViewModel = loginViewModel,
            onNavigateToGenderAndAgeGroup = {
                pendingUserMap?.let { onNavigateToGenderAndAgeGroup(it) }
            },
            onNavigateToQuote = onNavigateToQuote
        )
    }
}