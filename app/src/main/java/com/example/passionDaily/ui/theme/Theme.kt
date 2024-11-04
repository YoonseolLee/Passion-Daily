package com.example.passionDaily.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun Passion_DailyTheme(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        typography = Typography,
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(BlackBackground),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}
