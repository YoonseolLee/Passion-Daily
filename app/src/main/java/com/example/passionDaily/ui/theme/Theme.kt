package com.example.passionDaily.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 단일 다크 테마 팔레트 정의
val DarkColorPalette =
    darkColorScheme(
        primary = PrimaryColor, // #1A3C96
        onPrimary = Color.White,
        secondary = SecondaryColor, // #D9D9D9
        background = BlackBackground, // #000000
        surface = BlackBackground,
        onBackground = SecondaryColor,
        onSurface = OnSurface,
    )

@Composable
fun Passion_DailyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        typography = Typography,
        content = content,
    )
}
