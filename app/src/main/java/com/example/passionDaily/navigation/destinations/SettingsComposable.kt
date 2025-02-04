package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.settings.presentation.screen.SettingsScreen

fun NavGraphBuilder.settingsComposable(
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    composable(route = "settings") {
        SettingsScreen(
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin,
            currentScreen = NavigationBarScreens.SETTINGS,
            onBack = onBack
        )
    }
}