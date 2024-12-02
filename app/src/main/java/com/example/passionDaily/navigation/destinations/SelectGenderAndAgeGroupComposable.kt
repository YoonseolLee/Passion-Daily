package com.example.passionDaily.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.passionDaily.ui.screens.SelectGenderAndAgeGroupScreen
import com.google.gson.Gson

fun NavGraphBuilder.selectGenderAndAgeGroupComposable(
    onSkip: () -> Unit,
    onNextClicked: () -> Unit
) {
    composable(
        route = "select_gender_age?pendingUserMap={pendingUserMapJson}",
        arguments = listOf(
            navArgument("pendingUserMap") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) { backStackEntry ->
        val json = backStackEntry.arguments?.getString("pendingUserMap")
        val pendingUserMap = if (json != null) {
            Gson().fromJson(json, Map::class.java) as Map<String, Any>
        } else {
            null
        }

        SelectGenderAndAgeGroupScreen(
            pendingUserMap = pendingUserMap,
            onSkip = onSkip,
            onNextClicked = onNextClicked
        )
    }
}

