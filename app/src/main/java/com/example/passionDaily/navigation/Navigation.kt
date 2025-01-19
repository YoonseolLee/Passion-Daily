package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.passionDaily.navigation.destinations.categoryComposable
import com.example.passionDaily.navigation.destinations.favoritesComposable
import com.example.passionDaily.navigation.destinations.loginComposable
import com.example.passionDaily.navigation.destinations.quoteComposable
import com.example.passionDaily.navigation.destinations.settingsComposable
import com.example.passionDaily.navigation.destinations.termsConsentComposable
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()

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
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel,
            onNavigateToCategory = { screens.navigateToCategory() },
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuoteFromNavBar() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
            onNavigateToLogin = { screens.navigateToLogin() }
        )
        categoryComposable(
            onNavigateToQuote = { screens.navigateToQuote() },
            quoteViewModel = quoteViewModel,
            onBack = { navController.popBackStack() }
        )
        favoritesComposable(
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel,
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
            onNavigateToLogin = { screens.navigateToLogin() },
        )
        settingsComposable(
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToLogin = { screens.navigateToLogin() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
            onBack = { navController.popBackStack() }
        )
    }
}
