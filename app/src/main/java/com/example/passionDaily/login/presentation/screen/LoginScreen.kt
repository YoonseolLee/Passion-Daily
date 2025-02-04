

package com.example.passionDaily.login.presentation.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.OnSurface
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.login.state.AuthState
import com.example.passionDaily.login.presentation.viewmodel.SharedLogInViewModel

@Composable
fun LoginScreen(
    sharedLogInViewModel: SharedLogInViewModel,
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: (String) -> Unit
) {
    val authState by sharedLogInViewModel.authState.collectAsState()
    val userProfileJson by sharedLogInViewModel.userProfileJson.collectAsState()
    val isLoading by sharedLogInViewModel.isLoading.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                sharedLogInViewModel.signalLoginSuccess()
                onNavigateToQuote()
            }

            is AuthState.RequiresConsent -> {
                userProfileJson?.let { json ->
                    onNavigateToTermsConsent(json)
                }
            }

            is AuthState.Unauthenticated -> {
                Log.d("LoginScreen", "User is not authenticated")
            }
        }
    }

    LoginScreenContent(
        sharedLogInViewModel = sharedLogInViewModel,
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = PrimaryColor,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun LoginScreenContent(
    sharedLogInViewModel: SharedLogInViewModel,
) {
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(BlackBackground),
    ) {
        Column(
            modifier =
            Modifier
                .offset(x = 34.dp, y = 100.dp)
                .align(Alignment.TopStart),
        ) {
            LoginScreenHeaderTitle()
            LoginScreenHeaderSubtitle()
        }

        LoginScreenSplashScreenLogo(
            modifier = Modifier.align(Alignment.Center),
        )

        Column(
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
        ) {
            LoginScreenGoogleLoginButton(
                onGoogleLoginClick = {
                    sharedLogInViewModel.signInWithGoogle()
                }
            )
        }
    }
}

@Composable
fun LoginScreenSplashScreenLogo(modifier: Modifier = Modifier) {
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
fun LoginScreenHeaderTitle() {
    Text(
        text = stringResource(id = R.string.header_title_login_screen),
        style =
        TextStyle(
            fontSize = 30.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            fontWeight = FontWeight(400),
            color = GrayScaleWhite,
        ),
    )
}

@Composable
fun LoginScreenHeaderSubtitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 7.dp),
    ) {
        Text(
            text = stringResource(id = R.string.header_subtitle1_login_screen),
            style =
            TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = PrimaryColor,
            ),
        )
        Text(
            text = stringResource(id = R.string.header_subtitle2_login_screen),
            style =
            TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = OnSurface,
            ),
        )
    }
}

@Composable
fun LoginScreenGoogleLoginButton(
    onGoogleLoginClick: () -> Unit
) {
    Row(
        modifier =
        Modifier
            .width(345.dp)
            .height(54.dp)
            .background(color = GrayScaleWhite, shape = RoundedCornerShape(size = 10.dp))
            .padding(start = 17.dp)
            .clickable(onClick = onGoogleLoginClick),
        horizontalArrangement = Arrangement.spacedBy(79.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier =
            Modifier
                .width(24.dp)
                .height(24.dp)
                .background(color = GrayScaleWhite),
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = "google_icon",
            contentScale = ContentScale.None,
        )
        Text(
            text = stringResource(id = R.string.google_login),
            style =
            TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            ),
        )
    }
}