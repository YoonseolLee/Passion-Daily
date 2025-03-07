
package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.passionDaily.constants.AppDestinations
import com.example.passionDaily.navigation.destinations.categoryComposable
import com.example.passionDaily.navigation.destinations.favoritesComposable
import com.example.passionDaily.navigation.destinations.quoteComposable
import com.example.passionDaily.navigation.destinations.settingsComposable
import com.example.passionDaily.constants.NavigationBarScreens
import com.example.passionDaily.quote.presentation.screen.QuoteScreen
import com.example.passionDaily.favorites.presentation.viewmodel.FavoritesViewModel
import com.example.passionDaily.quote.presentation.viewmodel.QuoteViewModel
import com.example.passionDaily.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.QUOTE_ROUTE
    ) {

        quoteComposable(
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel,
            onNavigateToCategory = { screens.navigateToCategory() },
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuoteFromNavBar() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
        )

        composable(
            route = "${AppDestinations.QUOTE_ROUTE}/{category}/{quoteId}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("quoteId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = AppDestinations.QUOTE_DEEP_LINK
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            val quoteId = backStackEntry.arguments?.getString("quoteId")

            LaunchedEffect(category, quoteId) {
                if (category != null && quoteId != null) {
                    quoteViewModel.navigateToQuoteWithCategory(quoteId, category)
                }
            }

            QuoteScreen(
                quoteViewModel = quoteViewModel,
                favoritesViewModel = favoritesViewModel,
                quoteStateHolder = quoteViewModel.getStateHolder(),
                onNavigateToCategory = { screens.navigateToCategory() },
                onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
                onNavigateToQuote = { screens.navigateToQuoteFromNavBar() },
                onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
                currentScreen = NavigationBarScreens.QUOTE
            )
        }

        categoryComposable(
            quoteViewModel = quoteViewModel,
            quoteStateHolder = quoteViewModel.getStateHolder(),
            onNavigateToQuote = { screens.navigateToQuote() },
            onBack = { navController.popBackStack() }
        )

        favoritesComposable(
            quoteViewModel = quoteViewModel,
            favoritesViewModel = favoritesViewModel,
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
        )

        settingsComposable(
            settingsViewModel = settingsViewModel,
            onNavigateToFavorites = { screens.navigateToFavoritesFromNavBar() },
            onNavigateToQuote = { screens.navigateToQuote() },
            onNavigateToSettings = { screens.navigateToSettingsFromNavBar() },
            onBack = { navController.popBackStack() }
        )
    }
}