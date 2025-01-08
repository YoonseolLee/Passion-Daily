package com.example.passionDaily.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

@Composable
fun FavoritesScreen(
    sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel(),
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens
) {

    LaunchedEffect(key1 = Unit) {
        sharedQuoteViewModel.fetchFavoriteQuotes()
    }

    val favoriteQuotes by sharedQuoteViewModel.favoriteQuotes.collectAsState()
    val isFavoriteQuotesEmpty = favoriteQuotes.isEmpty()

    if (isFavoriteQuotesEmpty) {
        CommonQuoteScreen(
            viewModel = sharedQuoteViewModel,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = currentScreen,
            showCategorySelection = false,
            onNavigateToLogin = {},
            isFavoriteQuotesEmpty = true,
        )
    } else {
        CommonQuoteScreen(
            viewModel = sharedQuoteViewModel,
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToQuote = onNavigateToQuote,
            onNavigateToSettings = onNavigateToSettings,
            currentScreen = currentScreen,
            showCategorySelection = false,
            onNavigateToLogin = {},
            isFavoriteQuotesEmpty = false,
        )
    }
}

