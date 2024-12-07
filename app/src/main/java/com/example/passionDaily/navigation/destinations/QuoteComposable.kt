package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.passionDaily.ui.screens.QuoteScreen

fun NavGraphBuilder.quoteComposable() {
    composable(route = "quote") {
        QuoteScreen() // Quote 화면을 담당하는 Composable 함수
    }
}