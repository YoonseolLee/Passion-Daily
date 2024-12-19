package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.CategoryScreen
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

fun NavGraphBuilder.categoryComposable(
    onNavigateToQuote: () -> Unit,
) {
    composable(route = "category") {
        val sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel()

        CategoryScreen(
            sharedQuoteViewModel = sharedQuoteViewModel,
            onNavigateToQuote = onNavigateToQuote,
        )
    }
}