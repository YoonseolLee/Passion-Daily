package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.QuoteScreen
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

fun NavGraphBuilder.quoteComposable(
    onNavigateToCategory: () -> Unit,
    sharedQuoteViewModel: SharedQuoteViewModel,
    //    onNavigateToFavorite: () -> Unit,
//    onNavigateToSettings: () -> Unit,
) {
    composable(route = "quote") {
        QuoteScreen(
            sharedQuoteViewModel = sharedQuoteViewModel,
            onNavigateToCategory = onNavigateToCategory
        )
    }
}