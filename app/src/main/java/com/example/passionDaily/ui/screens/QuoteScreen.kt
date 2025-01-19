package com.example.passionDaily.ui.screens

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.passionDaily.ui.components.BackgroundImage
import com.example.passionDaily.ui.components.Buttons
import com.example.passionDaily.ui.components.CategorySelectionButton
import com.example.passionDaily.ui.components.LeftArrow
import com.example.passionDaily.ui.components.QuoteAndPerson
import com.example.passionDaily.ui.components.RightArrow
import com.example.passionDaily.ui.components.toQuoteDisplay
import com.example.passionDaily.ui.viewmodels.FavoritesViewModel
import com.example.passionDaily.ui.viewmodels.QuoteViewModel
import com.example.passionDaily.util.CommonNavigationBar

@Composable
fun QuoteScreen(
    favoritesViewModel: FavoritesViewModel,
    quoteViewModel: QuoteViewModel,
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToQuote: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    currentScreen: NavigationBarScreens,
) {

    val selectedCategory by quoteViewModel.selectedQuoteCategory.collectAsState()
    val currentQuote by quoteViewModel.currentQuote.collectAsState()
    val quotes by quoteViewModel.quotes.collectAsState()
    val isQuoteLoading by quoteViewModel.isQuoteLoading.collectAsState()

    LaunchedEffect(selectedCategory) {
        if (quotes.isEmpty()) {
            quoteViewModel.loadInitialQuotes(selectedCategory)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(imageUrl = currentQuote?.imageUrl)

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .clickable { quoteViewModel.previousQuote() }
        ) {
            LeftArrow()
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .clickable { quoteViewModel.nextQuote() }
        ) {
            RightArrow()
        }

        Row(
            modifier = Modifier
                .offset(y = 110.dp)
                .align(Alignment.TopCenter)
        ) {
            CategorySelectionButton(
                onCategoryClicked = onNavigateToCategory,
                selectedCategory = selectedCategory,
            )
        }

        Column(
            modifier = Modifier
                .offset(y = 277.dp)
                .align(Alignment.TopCenter)
        ) {
            QuoteAndPerson(
                quote = currentQuote?.text ?: "",
                author = currentQuote?.person ?: ""
            )
        }

        if (isQuoteLoading) {
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
                currentQuote?.let { quote ->
                    Buttons(
                        quoteViewModel = quoteViewModel,
                        favoritesViewModel = favoritesViewModel,
                        currentQuoteId = quote.id,
                        category = selectedCategory,
                        onRequireLogin = onNavigateToLogin,
                        quoteDisplay = quote.toQuoteDisplay()
                    )
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
}