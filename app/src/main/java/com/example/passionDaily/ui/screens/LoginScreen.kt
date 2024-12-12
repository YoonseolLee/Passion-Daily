package com.example.passionDaily.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.OnSurface
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.ui.viewmodels.AuthState
import com.example.passionDaily.ui.viewmodels.SharedSignInViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

@Composable
fun LoginScreen(
    sharedSignInViewModel: SharedSignInViewModel = hiltViewModel(),
    onNavigateToQuote: () -> Unit,
    onNavigateToTermsConsent: (String) -> Unit
) {
    val authState by sharedSignInViewModel.authState.collectAsState()
    val userProfileJson by sharedSignInViewModel.userProfileJson.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // 인증 성공 시 콜백 호출
                onNavigateToQuote()
            }

            is AuthState.RequiresConsent -> {
                userProfileJson?.let { json ->
                    onNavigateToTermsConsent(json)
                }
            }

            is AuthState.Error -> {
                // 에러 처리 로직 (필요한 경우)
                Log.e(
                    "LoginScreen",
                    "Authentication failed: ${(authState as AuthState.Error).message}"
                )
            }

            else -> {} // Loading 또는 Unauthenticated 상태
        }
    }

    LoginScreenContent(
        sharedSignInViewModel = sharedSignInViewModel
    )
}

@Composable
fun LoginScreenContent(
    sharedSignInViewModel: SharedSignInViewModel
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
                .offset(x = 34.dp, y = 99.dp)
                .align(Alignment.TopStart),
        ) {
            HeaderTitle()
            HeaderSubtitle()
        }

        SplashScreenLogo(
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
            GoogleLoginButton(
                onGoogleLoginClick = {
                    sharedSignInViewModel.signInWithGoogle()
                }
            )
        }
    }
}

/**
 * 나중에 SplashScreen 주석 해제시 없애야함. 임시방편임.
 */
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
fun HeaderTitle() {
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
fun HeaderSubtitle() {
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
fun GoogleLoginButton(
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

