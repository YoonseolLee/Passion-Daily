package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.CategoryScreen
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.quote.stateholder.QuoteStateHolder

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