package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.CategoryScreen
import com.example.passionDaily.ui.state.QuoteStateHolder
import com.example.passionDaily.ui.viewmodels.QuoteViewModel

fun NavGraphBuilder.categoryComposable(
    onNavigateToQuote: () -> Unit,
    quoteViewModel: QuoteViewModel,
    quoteStateHolder: QuoteStateHolder,
    onBack: () -> Unit
) {
    composable(route = "category") {
        CategoryScreen(
            quoteViewModel = quoteViewModel,
            quoteStateHolder = quoteStateHolder,
            onNavigateToQuote = onNavigateToQuote,
            onBack = onBack
        )
    }
}