package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passionDaily.R
import com.example.passionDaily.ui.theme.BlackBackground

@Composable
fun SingUpCompleteScreen() {
    SingUpCompleteScreenContent()
}

@Composable
fun SingUpCompleteScreenContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(200.dp))

            SignUpCompleteTitleSection()

            Spacer(modifier = Modifier.weight(1f)) // 남은 공간을 채워 버튼을 아래로 밀어냄

            BottomActionSection()
        }
    }
}

@Composable
fun SignUpCompleteTitleSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_icon),
            contentDescription = "가입 완료 체크 아이콘",
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(41.dp))

        Text(
            text = "가입 완료!",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "닉네임님,\n환영합니다.",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BottomActionSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 80.dp)
    ) {
        NicknameGuideText()

        Spacer(modifier = Modifier.height(24.dp))

        StartServiceButton()
    }
}

@Composable
fun NicknameGuideText() {
    Text(
        text = "닉네임 변경은 설정에서 가능해요.",
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
            color = Color(0xFFABABAB)
        )
    )
}

@Composable
fun StartServiceButton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(345.dp)
            .height(57.dp)
            .background(color = Color.White, shape = RoundedCornerShape(size = 4.dp))
            .clickable { /* 시작하기 버튼 클릭 시 로직 */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "시작하기",
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.inter_24pt_regular)),
                color = Color.Black
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingUpCompleteScreenContentPreview() {
    SingUpCompleteScreenContent()
}

@Preview(showBackground = true)
@Composable
fun StartServiceButtonPreview(
) {
    StartServiceButton()
}


