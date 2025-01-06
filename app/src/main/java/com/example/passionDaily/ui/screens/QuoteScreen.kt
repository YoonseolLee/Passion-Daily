package com.example.passionDaily.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

@Composable
fun QuoteScreen(
    sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel(),
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    CommonQuoteScreen(
        viewModel = sharedQuoteViewModel,
        onNavigateToCategory = onNavigateToCategory,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToQuote = onNavigateToQuote,
        onNavigateToSettings = onNavigateToSettings,
        currentScreen = currentScreen,
        onNavigateToLogin = onNavigateToLogin,
        showCategorySelection = true,
        isFavoriteQuotesEmpty = false
    )
}

