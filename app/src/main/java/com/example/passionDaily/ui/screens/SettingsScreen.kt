package com.example.passionDaily.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.passionDaily.util.CommonNavigationBar

@Composable
fun SettingsScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    SettingsScreenContent(
        onFavoritesClicked = onNavigateToFavorites,
        onQuoteClicked = onNavigateToQuote,
        onSettingsClicked = onNavigateToSettings,
        currentScreen = currentScreen
    )
}

@Composable
fun SettingsScreenContent(
    onFavoritesClicked: () -> Unit,
    onQuoteClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            CommonNavigationBar(
                currentScreen = currentScreen,
                onNavigateToFavorites = onFavoritesClicked,
                onNavigateToQuote = onQuoteClicked,
                onNavigateToSettings = onSettingsClicked
            )
        }
    }
}
