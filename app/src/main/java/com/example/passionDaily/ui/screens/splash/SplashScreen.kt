package com.example.passionDaily.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.viewmodels.SplashViewModel
import com.example.passionDaily.util.Action
import com.example.passion_daily.R

@Composable
fun SplashScreen(
    navigateToQuote: (Action) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            navigateToQuote(Action.NO_ACTION)
        }
    }

    SplashScreenContent()
}

@Composable
fun SplashScreenContent() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BlackBackground),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(156.dp)
                    .height(125.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
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
    }
}

@Composable
@Preview(showBackground = true)
fun SplashScreenContentPreview() {
    SplashScreenContent()
}
