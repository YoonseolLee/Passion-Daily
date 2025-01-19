package com.example.passionDaily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.example.passionDaily.ui.components.QuoteAndPerson

@Composable
fun ShareableQuoteImage(
    imageUrl: String?,
    quoteText: String,
    author: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1080.dp)
            .height(1920.dp)
            .background(Color.Black)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .allowHardware(false)
                    .crossfade(true)
                    .build(),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.5f },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            QuoteAndPerson(
                quote = quoteText,
                author = author
            )
        }
    }
}

