package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.NavigationBarScreens
import com.example.passionDaily.ui.screens.QuoteScreen
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

fun NavGraphBuilder.quoteComposable(
    quoteViewModel: QuoteViewModel,
    favoritesViewModel: FavoritesViewModel,
    sharedSignInViewModel: SharedSignInViewModel,
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
            sharedSignInViewModel = sharedSignInViewModel,
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToLogin = onNavigateToLogin,
            currentScreen = NavigationBarScreens.QUOTE,
        )
    }
}