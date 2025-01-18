package com.example.passionDaily.ui.screens

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.passionDaily.R

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            painter = painterResource(id = R.drawable.clarity_arrow_line),
            contentDescription = "Back",
        )
    }
}