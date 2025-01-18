package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.CategoryScreen
import com.example.passionDaily.ui.viewmodels.QuoteViewModel

fun NavGraphBuilder.categoryComposable(
    onNavigateToQuote: () -> Unit,
    quoteViewModel: QuoteViewModel,
    onBack: () -> Unit
) {
    composable(route = "category") {
        CategoryScreen(
            quoteViewModel = quoteViewModel,
            onNavigateToQuote = onNavigateToQuote,
            onBack = onBack
        )
    }
}