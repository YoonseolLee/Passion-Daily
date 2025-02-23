package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.settings.presentation.screen.SettingsScreen
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel

fun NavGraphBuilder.settingsComposable(
    settingsViewModel: SettingsViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit
) {
    composable(route = "settings") {
        SettingsScreen(
            settingsViewModel = settingsViewModel,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = NavigationBarScreens.SETTINGS,
            onBack = onBack
        )
    }
}