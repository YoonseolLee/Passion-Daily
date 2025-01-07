package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.passionDaily.data.repository.remote.RemoteQuoteRepositoryImpl
import com.example.passionDaily.navigation.destinations.categoryComposable
import com.example.passionDaily.navigation.destinations.favoritesComposable
import com.example.passionDaily.navigation.destinations.loginComposable
import com.example.passionDaily.navigation.destinations.quoteComposable
import com.example.passionDaily.navigation.destinations.settingsComposable
import com.example.passionDaily.navigation.destinations.termsConsentComposable
import com.example.passionDaily.ui.viewmodels.SharedQuoteViewModel

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    val sharedQuoteViewModel: SharedQuoteViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "quote"
    ) {
        loginComposable(
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToTermsConsent = { userProfileJson ->
                screens.navigateToTermsConsent(userProfileJson)
            }
        )
        termsConsentComposable(
            onNavigateToQuoteScreen = { screens.navigateToQuote() }
        )
        quoteComposable(
            sharedQuoteViewModel = sharedQuoteViewModel,
            onNavigateToCategory = { screens.navigateToCategory() },
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuoteFromNavBar() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
            onNavigateToLogin = { screens.navigateToLogin() }
        )
        categoryComposable(
            onNavigateToQuote = { screens.navigateToQuote() },
            sharedQuoteViewModel = sharedQuoteViewModel
        )
        favoritesComposable(
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
        )
        settingsComposable(
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToLogin = { screens.navigateToLogin() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() }
        )
    }
}
