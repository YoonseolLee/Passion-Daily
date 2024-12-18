package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.QuoteScreen
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

fun NavGraphBuilder.quoteComposable(
    onNavigateToCategory: () -> Unit,
//    onNavigateToFavorite: () -> Unit,
//    onNavigateToSettings: () -> Unit,
) {
    composable(route = "quote") {
        val sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel()

        QuoteScreen(
            sharedQuoteViewModel = sharedQuoteViewModel,
            onNavigateToCategory = onNavigateToCategory
        )
    }
}