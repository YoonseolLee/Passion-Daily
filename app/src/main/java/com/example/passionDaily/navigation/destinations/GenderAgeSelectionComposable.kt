package com.example.passionDaily.navigation.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passionDaily.ui.screens.GenderAgeSelectionScreen
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel

fun NavGraphBuilder.genderAgeSelectionComposable(
    onNextClicked: () -> Unit
) {
    composable(
        route = "genderAgeSelection/{userProfileJsonV2}",
        arguments = listOf(
            navArgument("userProfileJsonV2") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val userProfileJsonV2 = backStackEntry.arguments?.getString("userProfileJsonV2")
        val sharedSignInViewModel: SharedSignInViewModel = hiltViewModel()

        GenderAgeSelectionScreen(
            userProfileJsonV2 = userProfileJsonV2,
            sharedSignInViewModel = sharedSignInViewModel,
            onNextClicked = onNextClicked
        )
    }
}