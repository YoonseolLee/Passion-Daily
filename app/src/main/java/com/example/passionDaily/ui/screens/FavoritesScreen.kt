package com.example.passionDaily.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    CommonQuoteScreen(
        viewModel = sharedQuoteViewModel,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToQuote = onNavigateToQuote,
        onNavigateToSettings = onNavigateToSettings,
        currentScreen = currentScreen,
        showCategorySelection = false,
        onNavigateToLogin = {}
    )
}

