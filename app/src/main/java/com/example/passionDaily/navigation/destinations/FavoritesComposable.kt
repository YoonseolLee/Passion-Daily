package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.FavoritesScreen
import com.example.passionDaily.ui.screens.NavigationBarScreens

fun NavGraphBuilder.favoritesComposable(
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    composable(route = "favorites") {
        FavoritesScreen(
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = NavigationBarScreens.FAVORITES,
        )
    }
}