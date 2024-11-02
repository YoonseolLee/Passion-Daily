package com.example.passionDaily.ui.screens.loading

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.Passion_DailyTheme

@Composable
fun LoadingScreen(loading: Boolean) {
    if (!loading) return

    // MaterialTheme 컬러값들 확인
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryHex = String.format("#%06X", 0xFFFFFF and primaryColor.toArgb())
    Log.d("ColorDebug", "Primary color hex: $primaryHex") // #1A3C96가 나와야 정상

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BlackBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.primary,
//            trackColor = Color(0xFF1A3C96),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    Passion_DailyTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 로딩 중 상태
            LoadingScreen(loading = true)
        }
    }
}
