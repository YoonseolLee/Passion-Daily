package com.example.passionDaily.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.passionDaily.R
import com.example.passionDaily.ui.components.BackgroundImage
import com.example.passionDaily.ui.components.Buttons
import com.example.passionDaily.ui.components.LeftArrow
import com.example.passionDaily.ui.components.QuoteAndPerson
import com.example.passionDaily.ui.components.RightArrow
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.util.CommonNavigationBar
import com.example.passionDaily.util.QuoteCategory.Companion.toQuoteCategory

@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    quoteViewModel: QuoteViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens
) {
    val favoriteQuotes by favoritesViewModel.favoriteQuotes.collectAsState()
    val currentFavoriteQuote by favoritesViewModel.currentFavoriteQuote.collectAsState()
    val isFavoriteQuotesEmpty = favoriteQuotes.isEmpty()
    val isFavoriteLoading by favoritesViewModel.isFavoriteLoading.collectAsState()

    LaunchedEffect(currentScreen) {
        if (currentScreen == NavigationBarScreens.FAVORITES) {
            favoritesViewModel.loadFavorites()
        }
        Log.d("FavoritesScreen", "currentFavoriteQuote changed: $currentFavoriteQuote")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(imageUrl = currentFavoriteQuote?.imageUrl ?: "")

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .clickable { favoritesViewModel.previousQuote() }
        ) {
            LeftArrow()
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .clickable { favoritesViewModel.nextQuote() }
        ) {
            RightArrow()
        }

        if (isFavoriteQuotesEmpty) {
            Box(
                modifier = Modifier
                    .offset(y = 300.dp)
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sentiment_dissatisfied_icon),
                        contentDescription = "sentiment_dissatisfied_icon",
                        contentScale = ContentScale.None
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        text = "아직 즐겨찾기한 명언이 없어요.",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.inter_18pt_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFFFF),
                        )
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .offset(y = 277.dp)
                    .align(Alignment.TopCenter)
            ) {
                QuoteAndPerson(
                    quote = currentFavoriteQuote?.text ?: "",
                    author = currentFavoriteQuote?.person ?: ""
                )
            }
        }

        // 로딩 중이라면
        if (isFavoriteLoading) {
            Log.d("FavoritesScreen", "isFavoriteLoading: $isFavoriteLoading")
            Log.d("FavoritesScreen", "favoriteQuotes: $favoriteQuotes")
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        } else {
            Row(
                modifier = Modifier
                    .offset(y = -168.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentFavoriteQuote?.let { quote ->
                    Buttons(
                        quoteViewModel,
                        favoritesViewModel,
                        currentQuoteId = quote.quoteId,
                        category = quote.categoryId.toQuoteCategory(),
                        onRequireLogin = onNavigateToLogin,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            CommonNavigationBar(
                currentScreen = currentScreen,
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToQuote = onNavigateToQuote,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}
