
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
import com.example.passionDaily.navigation.destinations.categoryComposable
import com.example.passionDaily.navigation.destinations.favoritesComposable
import com.example.passionDaily.navigation.destinations.loginComposable
import com.example.passionDaily.navigation.destinations.quoteComposable
import com.example.passionDaily.navigation.destinations.settingsComposable
import com.example.passionDaily.navigation.destinations.termsConsentComposable
import com.example.passionDaily.ui.screens.NavigationBarScreens
import com.example.passionDaily.ui.screens.QuoteScreen
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    val quoteViewModel: QuoteViewModel = hiltViewModel()
    val favoritesViewModel: FavoritesViewModel = hiltViewModel()
    val sharedSignInViewModel: SharedSignInViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "quote"
    ) {
        loginComposable(
            sharedSignInViewModel = sharedSignInViewModel,
            onNavigateToQuote = { screens.navigateToQuote(popUpTo = true) },
            onNavigateToTermsConsent = { userProfileJson ->
                screens.navigateToTermsConsent(userProfileJson)
            }
        )

        termsConsentComposable(
            onNavigateToQuoteScreen = { screens.navigateToQuote(popUpTo = true) }
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

        composable(
            route = "quote/{category}/{quoteId}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("quoteId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "passiondaily://quote/{category}/{quoteId}"
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
                onNavigateToLogin = { screens.navigateToLogin() },
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
            onNavigateToLogin = { screens.navigateToLogin() }
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