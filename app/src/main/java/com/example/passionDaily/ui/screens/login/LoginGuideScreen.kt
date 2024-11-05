package com.example.passionDaily.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.ui.screens.splash.SplashScreenLogo
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.GrayScaleWhite
import com.example.passionDaily.ui.theme.OnSurface
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.ui.theme.PrimaryColor
import com.example.passion_daily.R

@Composable
fun LoginGuideScreen() {
}

@Composable
fun LoginGuideScreenContent() {
    Box(
        // 전체 화면을 채우는 최상위 Box
        modifier =
            Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .border(width = 1.dp, color = Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .border(width = 1.dp, color = Color.Green)
                    .align(Alignment.TopStart) // Column을 왼쪽 상단에 배치
                    .padding(16.dp),
            // Column과 화면 가장자리 간격 추가
            verticalArrangement = Arrangement.spacedBy(4.dp), // 텍스트 간격 설정
        ) {
            Text(
                text = "간편로그인 후\n이용이 가능합니다.",
                style =
                    TextStyle(
                        fontSize = 30.sp,
                        fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                        fontWeight = FontWeight(400),
                        color = GrayScaleWhite,
                    ),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .border(width = 1.dp, color = Color.Green)
                        .padding(top = 4.dp), // 위쪽 간격 추가
            ) {
                Text(
                    text = "30초",
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                            fontWeight = FontWeight(500),
                            color = PrimaryColor, // 파란색
                        ),
                )
                Text(
                    text = "면 가입이 가능해요.",
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                            fontWeight = FontWeight(500),
                            color = OnSurface, // 회색
                        ),
                )
            }
        }

        SplashScreenLogo(
            modifier = Modifier.align(Alignment.Center), // 로고를 중앙에 고정
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewLoginGuideScreenContent() {
    Passion_DailyTheme {
        LoginGuideScreenContent()
    }
}
