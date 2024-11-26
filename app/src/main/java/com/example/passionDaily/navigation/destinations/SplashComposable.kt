package com.example.passionDaily.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.passionDaily.navigation.NavAction
import com.example.passionDaily.navigation.NavDestinations
import com.example.passionDaily.ui.screens.splash.SplashScreen
import com.example.passionDaily.ui.viewmodels.splash.SplashViewModel
import com.example.passionDaily.util.Action
import com.example.passionDaily.util.Constants

@Composable
fun SplashComposable(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val navigationAction by viewModel.navigationAction.collectAsStateWithLifecycle()

    LaunchedEffect(navigationAction) {
        when (navigationAction) {
            is NavAction.NavigateToQuoteScreen -> {
                navController.navigate(NavDestinations.QUOTE_ROUTE) {
                    popUpTo(NavDestinations.SPLASH_ROUTE) { inclusive = true }
                }
                viewModel.clearNavigationAction()
            }
            is NavAction.NavigateToLoginScreen -> {
                navController.navigate(NavDestinations.LOGIN_ROUTE) {
                    popUpTo(NavDestinations.SPLASH_ROUTE) { inclusive = true }
                }
                viewModel.clearNavigationAction()
            }
            null -> { /* No action */ }
        }
    }

    SplashScreen()
}

