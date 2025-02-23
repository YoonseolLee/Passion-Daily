package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.favorites.presentation.screen.FavoritesScreen
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel

fun NavGraphBuilder.favoritesComposable(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    ) {
    composable(route = "favorites") {
        FavoritesScreen(
            favoritesViewModel = favoritesViewModel,
            quoteViewModel = quoteViewModel,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = NavigationBarScreens.FAVORITES,
        )
    }
}