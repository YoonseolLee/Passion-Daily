package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.quote.presentation.screen.QuoteScreen
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel

fun NavGraphBuilder.quoteComposable(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable(route = "quote") {
        QuoteScreen(
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel,
            quoteStateHolder = quoteViewModel.getStateHolder(),
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin,
            currentScreen = NavigationBarScreens.QUOTE,
        )
    }
}
