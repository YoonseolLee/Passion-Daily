package com.example.passionDaily.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.passionDaily.R

@Composable
fun BackButton() {
    Image(
        painter = painterResource(id = R.drawable.clarity_arrow_line),
        contentDescription = "back_button"
    )
}