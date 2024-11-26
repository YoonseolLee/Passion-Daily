package com.example.passionDaily.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.navigation.NavAction
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.viewmodels.splash.SplashViewModel

@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = hiltViewModel(),
    navigateToNextScreen: (NavAction) -> Unit
) {
    val navigationAction by splashViewModel.navigationAction.collectAsState()

    LaunchedEffect(navigationAction) {
        navigationAction?.let { action ->
            navigateToNextScreen(action)
            splashViewModel.clearNavigationAction()
        }
    }

    SplashScreenContent()
}

@Composable
fun SplashScreenContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center,
    ) {
        SplashScreenLogo(modifier)
    }
}

@Composable
fun SplashScreenLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.passion_daily_icon),
            contentDescription = "passion_daily_icon",
        )
        Image(
            painter = painterResource(id = R.drawable.passion_daily_text),
            contentDescription = "passion_daily_text",
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SplashScreenContentPreview(modifier: Modifier = Modifier) {
    Passion_DailyTheme {
        SplashScreenContent()
    }
}
