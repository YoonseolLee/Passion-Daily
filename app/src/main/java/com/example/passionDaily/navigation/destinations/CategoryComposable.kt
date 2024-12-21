package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.CategoryScreen
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

fun NavGraphBuilder.categoryComposable(
    onNavigateToQuote: () -> Unit,
    sharedQuoteViewModel: SharedQuoteViewModel
) {
    composable(route = "category") {
        CategoryScreen(
            sharedQuoteViewModel = sharedQuoteViewModel,
            onNavigateToQuote = onNavigateToQuote,
        )
    }
}