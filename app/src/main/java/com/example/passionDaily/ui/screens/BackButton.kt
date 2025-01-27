package com.example.passionDaily.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.passionDaily.R

@Composable
fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onBack() }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.clarity_arrow_line),
            contentDescription = "Back",
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
    }
}