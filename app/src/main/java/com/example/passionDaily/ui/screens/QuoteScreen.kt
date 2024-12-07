package com.example.passionDaily.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun QuoteScreen(
) {
    Text(
        text = "WHOLE LOTTA RED",
        style = TextStyle(
            fontSize = 26.sp,
            fontWeight = FontWeight(750),
            color = Color.Red
        )
    )
}

