package com.example.passionDaily.navigation.destinations

import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.data.local.entity.QuoteEntity
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.ui.screens.FavoritesScreen
import com.example.passionDaily.ui.screens.NavigationBarScreens
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel

fun NavGraphBuilder.favoritesComposable(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    ) {
    composable(route = "favorites") {
        FavoritesScreen(
            favoritesViewModel = favoritesViewModel,
            quoteViewModel = quoteViewModel,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin,
            currentScreen = NavigationBarScreens.FAVORITES,
        )
    }
}