package com.example.passionDaily.ui.screens.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passionDaily.ui.theme.BlackBackground
import com.example.passionDaily.ui.theme.Passion_DailyTheme

@Composable
fun LoadingScreen(loading: Boolean) {
    if (!loading) return

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
        // 로딩 중 상태
        LoadingScreen(loading = true)
    }
}
