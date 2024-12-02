package com.example.passionDaily.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.passionDaily.navigation.destinations.loginComposable
import com.example.passionDaily.navigation.destinations.selectGenderAndAgeGroupComposable

@Composable
fun SetupNavigation(
    navController: NavHostController,
) {
    val screens = remember(navController) {
        Screens(navController = navController)
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        loginComposable(
            onNavigateToGenderAndAgeGroup = screens.navigateToGenderAndAgeSelectionWithUserMap,
            onNavigateToQuote = screens.navigateToQuote
        )

        selectGenderAndAgeGroupComposable(
            onSkip = screens.navigateToSignUpComplete,
            onNextClicked = screens.navigateToSignUpComplete,
        )
    }
}

//fun NavGraphBuilder.signupCompleteComposable(
//    onNextClicked: () -> Unit
//) {
//    composable(route = "signup_complete") {
//        SignupCompleteScreen(
//            onNextClicked = onNextClicked
//        )
//    }
//}
