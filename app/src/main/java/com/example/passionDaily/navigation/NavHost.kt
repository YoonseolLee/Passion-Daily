package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.login.LoginScreen
import com.example.passionDaily.ui.screens.splash.SplashScreen

@Composable
fun SetupNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen { navAction ->
                when (navAction) {
                    is NavAction.NavigateToLoginScreen -> navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }

                    is NavAction.NavigateToQuoteScreen -> navController.navigate("quote") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("quote") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("quote") {
//            QuoteScreen()
        }
    }
}