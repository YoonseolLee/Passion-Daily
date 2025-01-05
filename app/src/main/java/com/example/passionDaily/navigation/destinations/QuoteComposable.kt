package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.NavigationBarScreens
import com.example.passionDaily.ui.screens.QuoteScreen
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

fun NavGraphBuilder.quoteComposable(
    onNavigateToCategory: () -> Unit,
    sharedQuoteViewModel: SharedQuoteViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable(route = "quote") {
        QuoteScreen(
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            sharedQuoteViewModel = sharedQuoteViewModel,
            currentScreen = NavigationBarScreens.QUOTE,
            onNavigateToLogin = onNavigateToLogin,
        )
    }
}