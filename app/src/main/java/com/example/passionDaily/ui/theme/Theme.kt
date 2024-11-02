package com.example.passionDaily.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 단일 다크 테마 팔레트 정의
private val DarkColorPalette =
    darkColorScheme(
        primary = PrimaryColor,
        onPrimary = Color.White,
        secondary = SecondaryColor,
        background = BlackBackground,
        surface = BlackBackground,
        onBackground = SecondaryColor,
        onSurface = SecondaryColor,
    )

@Composable
fun Passion_DailyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
