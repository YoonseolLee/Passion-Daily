package com.example.passionDaily.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.screens.splash.SplashScreenLogo
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.OnSurface
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passionDaily.ui.viewmodels.LoginViewModel
import com.example.passionDaily.util.LoginState

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpRequired: () -> Unit,
) {
    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val user = (loginState as LoginState.Success).user
                if (user?.isMember == true) {
                    onLoginSuccess() // 회원이면 QuoteScreen으로 이동
                } else {
                    onSignUpRequired()
                }
                loginViewModel.clearLoginAction()
            }

        }
    }
}

@Composable
fun LoginScreenContent(
    loginState: LoginState,
    onGoogleSignInClick: () -> Unit
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
            KakaoLoginButton()
            GoogleLoginButton(
                isLoading = loginState is LoginState.Loading,
                onClick = onGoogleSignInClick
            )
        }

        if (loginState is LoginState.Error) {
            Toast.makeText(
                LocalContext.current,
                loginState.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
private fun HeaderTitle() {
    Text(
        text = stringResource(id = R.string.header_title),
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
private fun HeaderSubtitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 7.dp),
    ) {
        Text(
            text = stringResource(id = R.string.header_subtitle_30sec),
            style =
            TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                fontWeight = FontWeight(500),
                color = PrimaryColor,
            ),
        )
        Text(
            text = stringResource(id = R.string.header_subtitle_signin_eligible),
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
private fun KakaoLoginButton() {
    Row(
        modifier =
        Modifier
            .width(345.dp)
            .height(54.dp)
            .background(color = Color(0xFFFEE500), shape = RoundedCornerShape(size = 10.dp))
            .padding(start = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(59.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.kakaotalk_icon),
            contentDescription = "kakaotalk_icon",
            contentScale = ContentScale.Crop,
        )
        Text(
            text = stringResource(id = R.string.kakao_login),
            style =
            TextStyle(
                fontSize = 18.sp,
                lineHeight = 25.2.sp,
                fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun GoogleLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
        Modifier
            .width(345.dp)
            .height(54.dp)
            .background(color = GrayScaleWhite, shape = RoundedCornerShape(size = 10.dp))
            .padding(start = 17.dp),
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

@Composable
@Preview(showBackground = true)
fun PreviewLoginScreenContent() {
    Passion_DailyTheme {
        LoginScreenContent(
            loginState = LoginState.Success,
            onGoogleSignInClick = {}
        )
    }
}
