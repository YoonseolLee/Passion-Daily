package com.example.passionDaily.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.passion_daily.R

// 폰트 파일을 기반으로 FontFamily 생성
val InterFontFamily =
    FontFamily(
        Font(R.font.inter_24pt_regular, FontWeight.Normal),
    )

// Typography 설정
val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp, // Inter 24포인트 폰트를 사용하므로 폰트 크기를 24로 설정
                lineHeight = 32.sp, // 권장하는 줄 간격
                letterSpacing = 0.5.sp,
            ),
    )
